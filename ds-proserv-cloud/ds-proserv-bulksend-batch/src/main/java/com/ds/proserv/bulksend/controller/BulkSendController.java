package com.ds.proserv.bulksend.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.batch.common.service.BatchQueueService;
import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.bulksend.common.client.BulkSendDataSourceClient;
import com.ds.proserv.bulksend.common.client.BulkSendFailureLogClient;
import com.ds.proserv.bulksend.common.client.BulkSendProcessLogClient;
import com.ds.proserv.bulksend.common.client.BulkSendRecordLogClient;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchItem;
import com.ds.proserv.bulksend.common.service.BulkSendService;
import com.ds.proserv.bulksend.common.service.DSEnvelopeService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.BatchStatus;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.NoDataProcessingException;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.bulksend.sourcedata.domain.BulkSendPrepareDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendMessageDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogInformation;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.BatchTriggerInformation;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.ds.proserv.send.common.helper.SendQueryHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendController {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private BulkSendService bulkSendService;

	@Autowired
	private AccountService dsAccountService;

	@Autowired
	private DSEnvelopeService dsEnvelopeService;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private SendQueryHelper bulkSendQueryHelper;

	@Autowired
	private BulkSendDataSourceClient bulkSendDataSourceClient;

	@Autowired
	private BulkSendProcessLogClient bulkSendProcessLogClient;

	@Autowired
	private BulkSendFailureLogClient bulkSendFailureLogClient;

	@Autowired
	private BulkSendRecordLogClient bulkSendRecordLogClient;

	@GetMapping("/docusign/bulksend/manualretry")
	public void retryExceptionsManually() {

		triggerExceptionAsync();
	}

	private CompletableFuture<String> triggerExceptionAsync() {

		CompletableFuture<String> completableFuture = new CompletableFuture<>();

		Executors.newCachedThreadPool().submit(() -> {

			log.info("Triggered BulkSend batch manually");
			triggerBatchJob();
			return null;
		});

		return completableFuture;

	}

	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	public void triggerBatchJob() throws Exception {

		String batchId = null;
		try {

			boolean useTrackIds = bulkSendQueryHelper.useTrackIdsFlow(PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			boolean useTrackIdsWithToDate = bulkSendQueryHelper
					.useTrackIdsWithToDateFlow(PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

			log.info("useTrackIds -> {} and useTrackIdsWithToDate -> {}", useTrackIds, useTrackIdsWithToDate);
			int totalRowsPerProcess = bulkSendQueryHelper.getBatchSize(PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			List<String> queryIdentifiers = bulkSendQueryHelper
					.getQueryIdentifiers(PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

			BatchTriggerInformation batchTriggerInformation = coreBatchDataService.prepareBatchTriggerInformation(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DS_JOB_BATCHTYPE),
					bulkSendQueryHelper.getProgramStartDateTime(PropertyCacheConstants.BULKSEND_REFERENCE_NAME),
					useTrackIds, useTrackIdsWithToDate);

			batchId = batchTriggerInformation.getBatchId();
			int totalBatchSize = 0;
			List<CompletableFuture<String>> reportDataFutureBulkSendList = new ArrayList<CompletableFuture<String>>();

			log.info("QueryIdentifiers pulled from cachedatalog is {} for batchId -> {}", queryIdentifiers, batchId);
			for (String queryIdentifier : queryIdentifiers) {

				try {

					totalBatchSize = prepareBulkSendMessageRequest(totalBatchSize, totalRowsPerProcess,
							batchTriggerInformation, queryIdentifier, reportDataFutureBulkSendList);

				} catch (Exception exp) {

					log.error("An exception -> exp occurred for batchId -> {}", batchId);
					coreBatchDataService.createFailureProcess(queryIdentifier + "_" + batchId, batchId, null, exp,
							FailureCode.ERROR_107, FailureStep.ASYNC_BULKSEND_PROCESSING);
				}
			}

			if (!bulkSendQueryHelper.isBulkSendByQueue(PropertyCacheConstants.BULKSEND_REFERENCE_NAME)
					&& !StringUtils.isEmpty(batchId) && null != reportDataFutureBulkSendList
					&& !reportDataFutureBulkSendList.isEmpty()) {

				log.info("Waiting for all BulkSend Async job to complete for batchId -> {}", batchId);
				CompletableFuture.allOf(reportDataFutureBulkSendList
						.toArray(new CompletableFuture[reportDataFutureBulkSendList.size()])).get();
			}

			log.info("All batched processed for batchId -> {}", batchId);

			if (bulkSendQueryHelper.isBulkSendByQueue(PropertyCacheConstants.BULKSEND_REFERENCE_NAME)) {

				if (coreBatchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

					log.info(
							"######################### All Messages queued in process BulkSend flow for batchId -> {} #########################",
							batchId);
				} else {

					coreBatchDataService.finishNewBatch(batchId, Long.valueOf(totalBatchSize));
				}
			} else {

				coreBatchDataService.finishNewBatch(batchId, Long.valueOf(totalBatchSize));
			}

		} catch (Exception exp) {

			exp.printStackTrace();

			throw exp;
		}

	}

	private String getDraftEnvelopeOrTemplateId(String queryIdentifier, String baseUri, String accountGuid)
			throws FileNotFoundException {

		String userId = bulkSendQueryHelper.getUserId(queryIdentifier, PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		String draftEnvelopeIdOrTemplateId = null;
		boolean useTemplate = bulkSendQueryHelper.useTemplate(queryIdentifier,
				PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
		if (useTemplate) {

			draftEnvelopeIdOrTemplateId = bulkSendQueryHelper.getTemplateId(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
		} else {

			String draftEnvelopeFilePath = bulkSendQueryHelper.getDraftEnvelopeFilePath(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			draftEnvelopeIdOrTemplateId = dsEnvelopeService.createEnvelopeTemplate(baseUri, draftEnvelopeFilePath,
					accountGuid, userId);
		}
		return draftEnvelopeIdOrTemplateId;
	}

	private int prepareBulkSendMessageRequest(int totalBatchSize, int totalRowsPerProcess,
			BatchTriggerInformation batchTriggerInformation, String queryIdentifier,
			List<CompletableFuture<String>> reportDataFutureBulkSendList)
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		int pageNumber = 1;
		int paginationLimit = paginationLimit();
		PageInformation pageInformation = preparePageInformation(queryIdentifier, batchTriggerInformation, pageNumber,
				paginationLimit);

		log.info(
				"Fetching first set of records at {} for pageNumber -> {}, paginationLimit -> {} and queryIdentifier -> {}",
				LocalDateTime.now(), pageNumber, paginationLimit, queryIdentifier);

		BulkSendPrepareDefinition bulkSendPrepareDefinition = bulkSendDataSourceClient
				.findBulkSendRecordIds(pageInformation).getBody();

		if (null != bulkSendPrepareDefinition && null != bulkSendPrepareDefinition.getTotalRecords()
				&& bulkSendPrepareDefinition.getTotalRecords() > 0) {

			String accountGuid = bulkSendQueryHelper.getAccountId(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			AuthenticationRequest authenticationRequest = createAuthenticationRequest(queryIdentifier);
			AccountDefinition accountDefinition = dsAccountService.getAccount(accountGuid, authenticationRequest);

			String draftEnvelopeIdOrTemplateId = getDraftEnvelopeOrTemplateId(queryIdentifier,
					accountDefinition.getBaseUri(), accountGuid);

			List<Object> recordIds = bulkSendPrepareDefinition.getRecordIds();

			log.info(
					"First set of records at {} for pageNumber -> {}, paginationLimit -> {} and queryIdentifier -> {} is {}",
					LocalDateTime.now(), pageNumber, paginationLimit, queryIdentifier, recordIds);

			while (null != recordIds && !recordIds.isEmpty()) {

				final AtomicInteger groupByCounter = new AtomicInteger(0);
				Collection<List<Object>> groupByTotalRowsPerProcessColl = recordIds.stream()
						.collect(Collectors.groupingBy(it -> groupByCounter.getAndIncrement() / totalRowsPerProcess))
						.values();

				BulkSendParam bulkSendParam = new BulkSendParam(queryIdentifier, bulkSendQueryHelper);

				RuleEngineDefinition ruleEngineDefinition = objectMapper
						.readValue(new FileReader(bulkSendParam.getRuleEnginePath()), RuleEngineDefinition.class);

				for (List<Object> groupByTotalRowsPerProcess : groupByTotalRowsPerProcessColl) {

					List<String> toProcessIds = prepareBulkSendMessage(batchTriggerInformation, bulkSendParam,
							accountGuid, draftEnvelopeIdOrTemplateId, groupByTotalRowsPerProcess,
							reportDataFutureBulkSendList, ruleEngineDefinition, accountDefinition.getBaseUri());

					if (null != toProcessIds && !toProcessIds.isEmpty()) {

						totalBatchSize = totalBatchSize + toProcessIds.size();
					}
				}

				pageNumber = pageNumber + 1;
				pageInformation = preparePageInformation(queryIdentifier, batchTriggerInformation, pageNumber,
						paginationLimit);
				bulkSendPrepareDefinition = bulkSendDataSourceClient.findBulkSendRecordIds(pageInformation).getBody();

				if (null != bulkSendPrepareDefinition && null != bulkSendPrepareDefinition.getTotalRecords()
						&& bulkSendPrepareDefinition.getTotalRecords() > 0) {

					log.info(
							"Found more records at {} for pageNumber -> {}, paginationLimit -> {} and queryIdentifier -> {}",
							LocalDateTime.now(), pageNumber, paginationLimit, queryIdentifier);
					recordIds = bulkSendPrepareDefinition.getRecordIds();
				} else {

					log.info(
							"No more records available at {} for pageNumber -> {}, paginationLimit -> {} and queryIdentifier -> {}",
							LocalDateTime.now(), pageNumber, paginationLimit, queryIdentifier);
					recordIds = null;
				}
			}
		} else {

			log.warn("No Records at {} to process for queryIdentifier -> {} in batchId -> {}", LocalDateTime.now(),
					queryIdentifier, batchTriggerInformation.getBatchId());
		}

		return totalBatchSize;
	}

	private Integer paginationLimit() {

		String limitStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_PAGINATION_LIMIT, PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		if (!StringUtils.isEmpty(limitStr)) {

			return Integer.parseInt(limitStr);
		} else {

			return 10000;
		}

	}

	private List<String> prepareBulkSendMessage(BatchTriggerInformation batchTriggerInformation,
			BulkSendParam bulkSendParam, String accountGuid, String draftEnvelopeIdOrTemplateId,
			List<Object> groupByTotalRowsPerProcess, List<CompletableFuture<String>> reportDataFutureBulkSendList,
			RuleEngineDefinition ruleEngineDefinition, String baseUri) {

		List<String> toProcessRecordIds = groupByTotalRowsPerProcess.stream().map(Object::toString)
				.collect(Collectors.toList());

		if (coreBatchDataService.reprocessingPastRecords()) {

			log.info("************************* REPROCESSING PAST RECORD FLAG IS ENABLED *************************");
			PageInformation checkExistingPageInformation = bulkSendService
					.preparePageInformationForRecordValidation(groupByTotalRowsPerProcess);

			// Checking if records were processed earlier or not
			BulkSendRecordLogInformation bulkSendRecordLogInformation = bulkSendRecordLogClient
					.bulkFindAllBulkSendRecordLogs(bulkSendParam.getQueryIdentifier(), checkExistingPageInformation)
					.getBody();

			if (null != bulkSendRecordLogInformation && bulkSendRecordLogInformation.getTotalRecords() > 0L
					&& null != bulkSendRecordLogInformation.getBulkSendRecordLogDefinitions()
					&& !bulkSendRecordLogInformation.getBulkSendRecordLogDefinitions().isEmpty()) {

				List<String> alreadySavedApplicationIds = bulkSendRecordLogInformation.getBulkSendRecordLogDefinitions()
						.stream().map(BulkSendRecordLogDefinition::getRecordId).collect(Collectors.toList());

				log.debug("alreadySavedApplicationIds are {} in batchId -> {}", alreadySavedApplicationIds,
						batchTriggerInformation.getBatchId());

				log.info(
						"About to remove alreadySavedApplicationIds size {} from toProcessRecordIds (aka messageids) size of {} in batchId -> {}",
						alreadySavedApplicationIds.size(), toProcessRecordIds.size(),
						batchTriggerInformation.getBatchId());

				toProcessRecordIds.removeAll(alreadySavedApplicationIds);

				log.info("After removal toProcessRecordIds (aka messageids) size is {} in batchId -> {}",
						toProcessRecordIds.size(), batchTriggerInformation.getBatchId());

				if (null == toProcessRecordIds || toProcessRecordIds.isEmpty()) {

					log.warn("filtered toProcessRecordIds (aka messageids) is empty forbatchId -> {}",
							batchTriggerInformation.getBatchId());
					return null;
				}
			}
		}

		if (null != toProcessRecordIds && !toProcessRecordIds.isEmpty()) {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = coreBatchDataService
					.createConcurrentProcess(Long.valueOf(groupByTotalRowsPerProcess.size()),
							batchTriggerInformation.getBatchId());

			log.info("Creating new BulkSend thread for processId -> {}", concurrentProcessLogDefinition.getProcessId());

			BulkSendMessageDefinition bulkSendMessageDefinition = new BulkSendMessageDefinition();

			bulkSendMessageDefinition.setAccountId(accountGuid);
			bulkSendMessageDefinition.setBatchId(batchTriggerInformation.getBatchId());
			bulkSendMessageDefinition.setDraftEnvelopeIdOrTemplateId(draftEnvelopeIdOrTemplateId);
			bulkSendMessageDefinition.setProcessId(concurrentProcessLogDefinition.getProcessId());
			bulkSendMessageDefinition.setQueryIdentifier(bulkSendParam.getQueryIdentifier());

			bulkSendMessageDefinition.setRecordIds(toProcessRecordIds);
			bulkSendMessageDefinition.setSelectRecordDataQueryType(bulkSendParam.getSelectRecordDataQueryType());
			bulkSendMessageDefinition.setSelectRecordDataQueryTypePrimaryKeyName(
					bulkSendParam.getSelectRecordDataQueryTypePrimaryKeyName());
			bulkSendMessageDefinition.setUpdateRecordDataQueryType(bulkSendParam.getUpdateRecordDataQueryType());
			bulkSendMessageDefinition.setUpdateRecordDataQueryTypePrimaryKeyName(
					bulkSendParam.getUpdateRecordDataQueryTypePrimaryKeyName());
			bulkSendMessageDefinition.setUserId(bulkSendParam.getUserId());
			bulkSendMessageDefinition.setUseTemplate(bulkSendParam.isUseTemplate());
			bulkSendMessageDefinition.setRuleEngineDefinition(ruleEngineDefinition);
			bulkSendMessageDefinition.setBaseUri(baseUri);
			bulkSendMessageDefinition.setStartDateTime(batchTriggerInformation.getBatchStartDateTime());
			bulkSendMessageDefinition.setEndDateTime(batchTriggerInformation.getBatchEndDateTime());

			if (bulkSendQueryHelper.isBulkSendByQueue(PropertyCacheConstants.BULKSEND_REFERENCE_NAME)) {

				batchQueueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_BULKSEND_QUEUE_NAME,
						concurrentProcessLogDefinition.getProcessId(), batchTriggerInformation.getBatchId(),
						bulkSendMessageDefinition);
			} else {

				reportDataFutureBulkSendList.add(processBulkSendMessageAsync(bulkSendMessageDefinition));
			}
		} else {

			log.warn("No record present at this stage to be sent for BulkSend processing for batchId -> {}",
					batchTriggerInformation.getBatchId());
		}

		return toProcessRecordIds;
	}

	@Data
	static class BulkSendParam {

		private String selectRecordDataQueryType;
		private String selectRecordDataQueryTypePrimaryKeyName;
		private String updateRecordDataQueryType;
		private String updateRecordDataQueryTypePrimaryKeyName;
		private String ruleEnginePath;
		private boolean useTemplate;
		private String userId;

		private String queryIdentifier;

		public BulkSendParam(String queryIdentifier, SendQueryHelper bulkSendQueryHelper) {
			super();
			this.queryIdentifier = queryIdentifier;

			this.selectRecordDataQueryType = bulkSendQueryHelper.getSelectRecordDataQueryType(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			this.selectRecordDataQueryTypePrimaryKeyName = bulkSendQueryHelper
					.getSelectRecordDataQueryTypePrimaryKeyParamName(queryIdentifier,
							PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

			this.updateRecordDataQueryType = bulkSendQueryHelper.getUpdateRecordDataQueryType(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			this.updateRecordDataQueryTypePrimaryKeyName = bulkSendQueryHelper
					.getUpdateRecordDataQueryTypePrimaryKeyParamName(queryIdentifier,
							PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

			this.ruleEnginePath = bulkSendQueryHelper.getRuleEnginePath(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

			this.userId = bulkSendQueryHelper.getUserId(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
			this.useTemplate = bulkSendQueryHelper.useTemplate(queryIdentifier,
					PropertyCacheConstants.BULKSEND_REFERENCE_NAME);
		}

	}

	private CompletableFuture<String> processBulkSendMessageAsync(BulkSendMessageDefinition bulkSendMessageDefinition) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;
			try {

				EnvelopeBatchItem envelopeBatchProcessed = bulkSendService
						.processBulkSendMessage(bulkSendMessageDefinition);
				if (envelopeBatchProcessed.getSuccess()) {

					saveBulkSendLogAndBulkSendRecordLog(bulkSendMessageDefinition, envelopeBatchProcessed);

				} else {

					saveBulkSendFailureLog(bulkSendMessageDefinition, envelopeBatchProcessed);
				}

			} catch (NoDataProcessingException exp) {

				coreBatchDataService.finishConcurrentProcess(bulkSendMessageDefinition.getProcessId(),
						ProcessStatus.COMPLETEDWITHNORECORDS.toString());
			} catch (Throwable exp) {

				log.error("Some error {} occurred in processConnectExceptionAsync", exp);
				asyncStatus = AppConstants.FAILURE_VALUE;

				coreBatchDataService.createFailureProcess(
						bulkSendMessageDefinition.getQueryIdentifier() + "_"
								+ bulkSendMessageDefinition.getDraftEnvelopeIdOrTemplateId() + "_"
								+ bulkSendMessageDefinition.getProcessId(),
						FailureCode.ERROR_107.toString(), "UnknownExceptionByProcess",
						FailureStep.ASYNC_BULKSEND_PROCESSING.toString(), bulkSendMessageDefinition.getProcessId());
				exp.printStackTrace();

			}
			return asyncStatus;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != exp) {

				log.info("Async processing got error in handle, check failure table and/or logs for more details");

				exp.printStackTrace();

			} else {

				if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

					log.info(" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed $$$$$$$$$$$$$$$$$$$$$$$$$ ");
				} else {

					log.warn("Result is NOT success, it is {}, check logs for more information", asyncStatus);
				}

			}

			return asyncStatus;
		});
	}

	private void saveBulkSendFailureLog(BulkSendMessageDefinition bulkSendMessageDefinition,
			EnvelopeBatchItem envelopeBatchProcessed) {

		String processId = bulkSendMessageDefinition.getProcessId();

		log.info("Saving failure for processId -> {}", processId);

		BulkSendFailureLogDefinition bulkSendFailureLogDefinition = new BulkSendFailureLogDefinition();
		bulkSendFailureLogDefinition.setBatchSize(Long.valueOf(bulkSendMessageDefinition.getRecordIds().size()));

		if (null != envelopeBatchProcessed.getErrorDetails() && !envelopeBatchProcessed.getErrorDetails().isEmpty()) {

			String errorDetails = String.join(AppConstants.COMMA_DELIMITER, envelopeBatchProcessed.getErrorDetails());
			bulkSendFailureLogDefinition.setErrorMessage(errorDetails);
		}

		String commaSeparatedStr = bulkSendMessageDefinition.getRecordIds().stream().map(Object::toString)
				.collect(Collectors.joining(AppConstants.COMMA_DELIMITER));
		bulkSendFailureLogDefinition.setApplicationIds(commaSeparatedStr);

		bulkSendFailureLogDefinition.setBatchFailureDateTime(LocalDateTime.now().toString());

		bulkSendFailureLogClient.saveBulkSendFailure(bulkSendFailureLogDefinition);

		coreBatchDataService.createFailureProcess(processId, FailureCode.ERROR_112.toString(),
				FailureCode.ERROR_112.getFailureCodeDescription(), FailureStep.BULK_SEND_RECORDS.toString(), processId);

		coreBatchDataService.finishConcurrentProcess(processId, ProcessStatus.FAILED.toString());

	}

	private void saveBulkSendLogAndBulkSendRecordLog(BulkSendMessageDefinition bulkSendMessageDefinition,
			EnvelopeBatchItem envelopeBatchProcessed) {

		log.info("Saving success for processId -> {}", bulkSendMessageDefinition.getProcessId());

		List<String> recordIds = bulkSendMessageDefinition.getRecordIds();

		BulkSendProcessLogDefinition bulkSendProcessLogDefinition = new BulkSendProcessLogDefinition();
		bulkSendProcessLogDefinition.setMailingListId(envelopeBatchProcessed.getListId());
		bulkSendProcessLogDefinition.setBatchId(envelopeBatchProcessed.getBatchId());
		bulkSendProcessLogDefinition.setBatchName(envelopeBatchProcessed.getBatchName());
		bulkSendProcessLogDefinition.setBatchSize(Long.valueOf(recordIds.size()));

		if (!StringUtils.isEmpty(envelopeBatchProcessed.getTotalQueued())) {

			bulkSendProcessLogDefinition.setQueueSize(Long.valueOf(envelopeBatchProcessed.getTotalQueued()));
		}

		if (!StringUtils.isEmpty(envelopeBatchProcessed.getTotalFailed())) {

			bulkSendProcessLogDefinition.setFailedSize(Long.valueOf(envelopeBatchProcessed.getTotalFailed()));
		}

		bulkSendProcessLogDefinition.setSuccessSize(Long.valueOf(envelopeBatchProcessed.getTotalSent()));
		bulkSendProcessLogDefinition.setBatchSubmittedDateTime(LocalDateTime.now().toString());
		bulkSendProcessLogDefinition.setBatchStatus(BatchStatus.SUBMITTED.toString());

		bulkSendProcessLogClient.saveBulkSendProcessLog(bulkSendProcessLogDefinition);

		List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>(
				recordIds.size());
		recordIds.forEach(recordId -> {

			BulkSendRecordLogDefinition bulkSendRecordLogDefinition = new BulkSendRecordLogDefinition();
			bulkSendRecordLogDefinition.setBulkBatchId(envelopeBatchProcessed.getBatchId());
			bulkSendRecordLogDefinition.setStartDateTime(bulkSendMessageDefinition.getStartDateTime());
			bulkSendRecordLogDefinition.setEndDateTime(bulkSendMessageDefinition.getEndDateTime());
			bulkSendRecordLogDefinition.setRecordId(recordId.toString());
			bulkSendRecordLogDefinition.setRecordType(bulkSendMessageDefinition.getQueryIdentifier());

			bulkSendRecordLogDefinitions.add(bulkSendRecordLogDefinition);

		});

		BulkSendRecordLogInformation bulkSendRecordLogInformation = new BulkSendRecordLogInformation();
		bulkSendRecordLogInformation.setBulkSendRecordLogDefinitions(bulkSendRecordLogDefinitions);

		bulkSendRecordLogClient.bulkSaveBulkSendRecordLogs(bulkSendRecordLogInformation);

		coreBatchDataService.finishConcurrentProcess(bulkSendMessageDefinition.getProcessId(),
				ProcessStatus.COMPLETED.toString());
	}

	private AuthenticationRequest createAuthenticationRequest(String queryIdentifier) {

		String dsBulkSendUserId = bulkSendQueryHelper.getUserId(queryIdentifier,
				PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		String dsBulkSendScopes = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_SCOPES, PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		if (!StringUtils.isEmpty(dsBulkSendUserId)) {

			AuthenticationRequest authenticationRequest = new AuthenticationRequest();
			authenticationRequest.setUser(dsBulkSendUserId);

			if (!StringUtils.isEmpty(dsBulkSendScopes)) {

				authenticationRequest.setScopes(dsBulkSendScopes);
			} else {

				authenticationRequest.setScopes(AppConstants.ESIGN_SCOPES);
			}

			return authenticationRequest;
		}

		return null;
	}

	private PageInformation preparePageInformation(String queryIdentifier,
			BatchTriggerInformation batchTriggerInformation, Integer pageNumber, Integer paginationLimit) {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue(queryIdentifier);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue(bulkSendQueryHelper.getSelectRecordIdQueryType(queryIdentifier,
				PropertyCacheConstants.BULKSEND_REFERENCE_NAME));

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGENUMBER_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(pageNumber));

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGINATIONLIMIT_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(paginationLimit));

		pageQueryParams.add(pageQueryParam);

		if (bulkSendQueryHelper.getSelectRecordIdByDateRange(queryIdentifier,
				PropertyCacheConstants.BULKSEND_REFERENCE_NAME)) {

			if (!StringUtils.isEmpty(batchTriggerInformation.getBatchStartDateTime())) {

				pageQueryParam = new PageQueryParam();
				pageQueryParam.setParamName("inputFromDate");
				pageQueryParam.setParamValue(batchTriggerInformation.getBatchStartDateTime());

				pageQueryParams.add(pageQueryParam);
			}

			if (!StringUtils.isEmpty(batchTriggerInformation.getBatchEndDateTime())) {

				pageQueryParam = new PageQueryParam();
				pageQueryParam.setParamName("inputToDate");
				pageQueryParam.setParamValue(batchTriggerInformation.getBatchEndDateTime());

				pageQueryParams.add(pageQueryParam);
			}
		}

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}

}