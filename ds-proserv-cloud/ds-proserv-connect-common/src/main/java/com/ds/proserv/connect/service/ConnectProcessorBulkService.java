package com.ds.proserv.connect.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.batch.common.service.BatchQueueService;
import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.NewVersionExistException;
import com.ds.proserv.common.util.PreparePageUtil;
import com.ds.proserv.connect.client.DSEnvelopeClient;
import com.ds.proserv.connect.client.DSExceptionClient;
import com.ds.proserv.connect.domain.ConnectCacheData;
import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionInformation;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionMessageDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectProcessorBulkService {

	@Autowired
	private JAXBContext jaxbContext;

	@Autowired
	private TaskExecutor xmlTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private DSEnvelopeClient dsEnvelopeClient;

	@Autowired
	private DSExceptionClient dsExceptionClient;

	@Autowired
	private BatchQueueService queueService;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private ConnectEnvelopeService connectEnvelopeService;

	public List<DSExceptionDefinition> fetchAndPrepareDSExceptionDefinitionList(List<String> exceptionIds,
			String batchId, int pageNumber) {

		String exceptionIdsCommaSeparated = String.join(AppConstants.COMMA_DELIMITER, exceptionIds);

		log.info(
				"For pageNumber -> {} and batchId -> {}, exceptionIdsCommaSeparated are {} in fetchAndPrepareDSExceptionDefinitionList",
				pageNumber, batchId, exceptionIdsCommaSeparated);

		int recordsPerPage = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_RECORDS_PERPAGE, PropertyCacheConstants.CONNECT_REFERENCE_NAME));

		DSExceptionInformation dsExceptionInformation = dsExceptionClient
				.findExceptionByIds(
						PreparePageUtil.prepareExceptionPageInformation(0, exceptionIdsCommaSeparated, recordsPerPage))
				.getBody();
		List<DSExceptionDefinition> dsExceptionDefinitionList = dsExceptionInformation.getDsExceptionDefinitions();
		return dsExceptionDefinitionList;
	}

	public List<DSEnvelopeDefinition> processBulkSaveWithCounter(int backsOffCounter,
			List<DSExceptionDefinition> dsExceptionDefinitionList, String batchId, String processId,
			ConnectCacheData connectCacheData) {

		List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList = null;
		try {

			toProcessDSEnvelopeDefinitionList = saveBulkEnvelopeData(dsExceptionDefinitionList, processId, batchId);
		} catch (Throwable exp) {

			exp.printStackTrace();

			log.error("{} occured with error message -> {} occurred in processBulkSaveWithCounter for processId -> {}",
					exp, exp.getMessage(), processId);

			try {

				if (backsOffCounter <= connectCacheData.getBacksOffLimit()) {

					log.info(
							"Sending thread to sleep in processBulkSaveWithCounter for {} milliseconds with backsOffCounter -> {} for processId -> {}",
							(connectCacheData.getBacksOffInterval() * backsOffCounter), backsOffCounter, processId);

					Thread.sleep(connectCacheData.getBacksOffInterval() * backsOffCounter);
					processBulkSaveWithCounter(++backsOffCounter, dsExceptionDefinitionList, batchId, processId,
							connectCacheData);
				} else {

					if (saveFailureByQueue()) {

						queueService.createFailureMessageAndSend(processId, batchId, processId, exp,
								FailureCode.ERROR_107, FailureStep.BULK_CONNECT_SAVE);
					} else {

						coreBatchDataService.createFailureProcess(processId, FailureCode.ERROR_107.toString(),
								exp.getMessage(), FailureStep.BULK_CONNECT_SAVE.toString(), processId);
					}

				}
			} catch (InterruptedException e) {

				log.error(
						"InterruptedException occurred with {} error message in processBulkSaveWithCounter for processId -> {}",
						exp.getMessage(), processId);
				e.printStackTrace();
			}
		}

		return toProcessDSEnvelopeDefinitionList;
	}

	public void bulkSaveExceptionAfterProcessing(int backsOffCounter,
			List<DSExceptionDefinition> dsExceptionDefinitionList, String batchId, String processId,
			ConnectCacheData connectCacheData) {

		try {

			log.info("Bulk DSException save for processId -> {} and batchId -> {}", processId, batchId);

			RetryStatus[] retryStatusArr = RetryStatus.values();
			for (RetryStatus retryStatus : retryStatusArr) {

				updateExceptionByRetryStatus(retryStatus, batchId, processId, dsExceptionDefinitionList);
			}

		} catch (Throwable exp) {

			exp.printStackTrace();

			log.error(
					"{} occured with error message {} occurred in bulkSaveExceptionAfterProcessing for processId -> {}",
					exp, exp.getMessage(), processId);

			try {

				if (backsOffCounter <= connectCacheData.getBacksOffLimit()) {

					log.info(
							"Sending thread to sleep in bulkSaveExceptionAfterProcessing for {} milliseconds with backsOffCounter -> {} for processId -> {}",
							(connectCacheData.getBacksOffInterval() * backsOffCounter), backsOffCounter, processId);

					Thread.sleep(connectCacheData.getBacksOffInterval() * backsOffCounter);
					bulkSaveExceptionAfterProcessing(++backsOffCounter, dsExceptionDefinitionList, batchId, processId,
							connectCacheData);
				} else {

					if (saveFailureByQueue()) {

						queueService.createFailureMessageAndSend(processId, batchId, processId, exp,
								FailureCode.ERROR_107, FailureStep.BULK_EXCEPTION_SAVE);
					} else {

						coreBatchDataService.createFailureProcess(processId, FailureCode.ERROR_107.toString(),
								exp.getMessage(), FailureStep.BULK_EXCEPTION_SAVE.toString(), processId);
					}
				}

			} catch (InterruptedException e) {

				log.error(
						"InterruptedException occurred with {} error message in bulkSaveExceptionAfterProcessing for processId -> {}",
						exp.getMessage(), processId);
				e.printStackTrace();
			}

		}

	}

	private void updateExceptionByRetryStatus(RetryStatus retryStatus, String batchId, String processId,
			List<DSExceptionDefinition> dsExceptionDefinitionList) {

		log.info("updateExceptionByRetryStatus save called for retryStatus -> {}, processId -> {} and batchId -> {}",
				retryStatus, processId, batchId);

		List<String> failureExceptionIds = dsExceptionDefinitionList.stream()
				.filter(dsException -> retryStatus.toString().equalsIgnoreCase(dsException.getRetryStatus()))
				.map(DSExceptionDefinition::getId).collect(Collectors.toList());

		if (null != failureExceptionIds && !failureExceptionIds.isEmpty()) {

			if (saveExceptionByQueue()) {

				DSExceptionMessageDefinition dsExceptionMessageDefinition = new DSExceptionMessageDefinition();
				dsExceptionMessageDefinition.setRecordIds(failureExceptionIds);
				dsExceptionMessageDefinition.setBatchId(batchId);
				dsExceptionMessageDefinition.setProcessId(processId);
				dsExceptionMessageDefinition.setRetryStatus(retryStatus.toString());

				queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_EXCEPTION_QUEUE_NAME, processId,
						batchId, dsExceptionMessageDefinition);
			} else {

				String exceptionIdsCommaSeparated = String.join(AppConstants.COMMA_DELIMITER, failureExceptionIds);

				dsExceptionClient.updateExceptionRetryStatus(PreparePageUtil.prepareExceptionPageInformation(
						retryStatus.toString(), processId, exceptionIdsCommaSeparated));
			}
		}
	}

	public List<DSEnvelopeDefinition> saveBulkEnvelopeData(List<DSExceptionDefinition> dsExceptionDefinitionList,
			String processId, String batchId) throws InterruptedException, ExecutionException {

		log.info("saveBulkEnvelopeData called for processId -> {}", processId);
		DSEnvelopeInformation dsEnvelopeInformation = fetchEnvelopeInfoByEnvelopeIds(dsExceptionDefinitionList,
				processId);

		List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList = Collections
				.synchronizedList(new ArrayList<DSEnvelopeDefinition>());

		List<DSEnvelopeDefinition> dsEnvelopeDefinitionsList = dsEnvelopeInformation.getDsEnvelopeDefinitions();

		List<CompletableFuture<String>> futureProcessEnvelopeList = new ArrayList<CompletableFuture<String>>();

		for (DSExceptionDefinition dsException : dsExceptionDefinitionList) {

			if (null != dsEnvelopeDefinitionsList && !dsEnvelopeDefinitionsList.isEmpty()) {

				DSEnvelopeDefinition dsEnvelopeDefinitionFound = isEnvelopeInDB(dsEnvelopeDefinitionsList, dsException);

				// Envelope Already available
				if (null != dsEnvelopeDefinitionFound
						&& !StringUtils.isEmpty(dsEnvelopeDefinitionFound.getEnvelopeId())) {

					log.info("EnvelopeId -> {} found in db for processId -> {}",
							dsEnvelopeDefinitionFound.getEnvelopeId(), processId);
					createAsyncEnvelopeProcesses(dsException, processId, dsEnvelopeDefinitionFound,
							toProcessDSEnvelopeDefinitionList, futureProcessEnvelopeList, batchId);
				} else {// Envelope Not Available

					log.info("EnvelopeId -> {} not found in db for processId -> {}", dsException.getEnvelopeId(),
							processId);

					createAsyncEnvelopeProcesses(dsException, processId, null, toProcessDSEnvelopeDefinitionList,
							futureProcessEnvelopeList, batchId);
				}
			} else {

				log.info("EnvelopeId -> {} not found in db for any envelope in processId -> {}",
						dsException.getEnvelopeId(), processId);
				createAsyncEnvelopeProcesses(dsException, processId, null, toProcessDSEnvelopeDefinitionList,
						futureProcessEnvelopeList, batchId);

			}

		}

		if (null != futureProcessEnvelopeList && !futureProcessEnvelopeList.isEmpty()) {

			log.info("Waiting for all Async job to complete for processId -> {}", processId);
			CompletableFuture
					.allOf(futureProcessEnvelopeList.toArray(new CompletableFuture[futureProcessEnvelopeList.size()]))
					.get();
		}

		sendDataToSaveUpdate(toProcessDSEnvelopeDefinitionList, processId);

		return toProcessDSEnvelopeDefinitionList;
	}

	private void sendDataToSaveUpdate(List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String processId) {

		log.info("Calling bulk Save for Envelope and DrawApplication for processId -> {}", processId);

		try {

			if (null != toProcessDSEnvelopeDefinitionList && !toProcessDSEnvelopeDefinitionList.isEmpty()) {

				DSEnvelopeInformation toProcessDSEnvelopeInformation = new DSEnvelopeInformation();
				toProcessDSEnvelopeInformation.setProcessId(processId);
				toProcessDSEnvelopeInformation.setDsEnvelopeDefinitions(toProcessDSEnvelopeDefinitionList);

				log.info("Calling bulk Save for Envelope processId -> {}", processId);
				dsEnvelopeClient.bulkUpdateSaveEnvelopeDataV2(toProcessDSEnvelopeInformation);
			}

		} catch (Exception exp) {

			exp.printStackTrace();
			throw exp;
		}

	}

	private void createAsyncEnvelopeProcesses(DSExceptionDefinition dsException, String processId,
			DSEnvelopeDefinition dsEnvelopeDefinitionFound,
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList,
			List<CompletableFuture<String>> futureProcessEnvelopeList, String batchId) throws InterruptedException {

		log.info("createAsyncEnvelopeProcesses(): Creating CompletableFuture process for processId -> {}", processId);

		futureProcessEnvelopeList.add(processEnvelopeAsync(dsException, processId, dsEnvelopeDefinitionFound,
				toProcessDSEnvelopeDefinitionList, batchId));
	}

	private DSEnvelopeDefinition isEnvelopeInDB(List<DSEnvelopeDefinition> dsEnvelopeDefinitionsList,
			DSExceptionDefinition dsException) {

		for (DSEnvelopeDefinition dsEnvelopeDefinition : dsEnvelopeDefinitionsList) {

			if (dsException.getEnvelopeId().equalsIgnoreCase(dsEnvelopeDefinition.getEnvelopeId())) {

				return dsEnvelopeDefinition;
			}
		}

		return null;
	}

	private DSEnvelopeInformation fetchEnvelopeInfoByEnvelopeIds(List<DSExceptionDefinition> dsExceptionDefinitionList,
			String processId) {

		log.info("fetchEnvelopeInfoByEnvelopeIds called for processId -> {}", processId);
		List<String> dsExceptionIds = dsExceptionDefinitionList.stream().map(DSExceptionDefinition::getEnvelopeId)
				.collect(Collectors.toList());

		String dsExceptionIdAsString = String.join(",", dsExceptionIds);

		log.info("Fetching Bulk EnvelopeInfo for envelopeIds -> {}", dsExceptionIdAsString);
		DSEnvelopeInformation dsEnvelopeInformation = getSavedDSEnvelopeInformation(dsExceptionIdAsString);
		return dsEnvelopeInformation;
	}

	public DSEnvelopeInformation getSavedDSEnvelopeInformation(String dsEnvelopeIdAsString) {

		DSEnvelopeInformation dsEnvelopeInformation = dsEnvelopeClient
				.findEnvelopesByEnvelopeIds(preparePageInformation(0, dsEnvelopeIdAsString)).getBody();
		return dsEnvelopeInformation;
	}

	private CompletableFuture<String> processEnvelopeAsync(DSExceptionDefinition dsException, String processId,
			DSEnvelopeDefinition dsEnvelopeDefinitionFound,
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String batchId) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;

			String envelopeId = dsException.getEnvelopeId();
			String connectXML = dsException.getEnvelopeXml();

			log.info("Processing Data for envelopeId -> {} and processId -> {}", envelopeId, processId);
			processConnectXML(dsException, processId, dsEnvelopeDefinitionFound, toProcessDSEnvelopeDefinitionList,
					envelopeId, connectXML, batchId);

			updateRetryRecord(dsException);

			return asyncStatus;
		}, xmlTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != exp) {

				log.info("Async processing got exception in processEnvelope->handle");

				exp.printStackTrace();

			} else {

				if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

					log.info(
							" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed in processEnvelope $$$$$$$$$$$$$$$$$$$$$$$$$ ");
				} else {

					log.warn("Result is NOT success, it is {}, check logs for more information", asyncStatus);
				}

			}

			return asyncStatus;
		});

	}

	private void processConnectXML(DSExceptionDefinition dsException, String processId,
			DSEnvelopeDefinition dsEnvelopeDefinitionFound,
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String envelopeId, String connectXML,
			String batchId) {

		try {
			// convert xml string to DocuSignEnvelopeInformation object
			StreamSource streamSource = new StreamSource(new StringReader(connectXML));

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			DocuSignEnvelopeInformation docuSignEnvelopeInformation = jaxbUnmarshaller
					.unmarshal(streamSource, DocuSignEnvelopeInformation.class).getValue();

			if (null != docuSignEnvelopeInformation && null != docuSignEnvelopeInformation.getEnvelopeStatus()) {

				log.info("processEnvelope(): Unmarshalled XML successfully for EnvelopeId -> {}",
						docuSignEnvelopeInformation.getEnvelopeStatus().getEnvelopeID());

				try {

					// if we got here then no errors so now check if retryLImit exceeded
					if (!checkRetryLimitExceeded(dsException)) {

						if (null != dsEnvelopeDefinitionFound
								&& !StringUtils.isEmpty(dsEnvelopeDefinitionFound.getEnvelopeId())) {// Envelope
																										// Update
																										// scenario

							if (canProcessConnectMessage(docuSignEnvelopeInformation, envelopeId,
									dsEnvelopeDefinitionFound)) {

								log.info("canProcessConnectMessage envelopeId -> {} in processId -> {}", envelopeId,
										processId);
								DSEnvelopeDefinition newDsEnvelopeDefinition = connectEnvelopeService
										.prepareEnvelopeDefinition(docuSignEnvelopeInformation);

								toProcessDSEnvelopeDefinitionList.add(newDsEnvelopeDefinition);
							} else {

								log.info("New version of envelopeId -> {} exist in processId -> {}", envelopeId,
										processId);
								throw new NewVersionExistException(envelopeId + " already moved to new version");
							}
						} else {

							DSEnvelopeDefinition newDsEnvelopeDefinition = connectEnvelopeService
									.prepareEnvelopeDefinition(docuSignEnvelopeInformation);

							toProcessDSEnvelopeDefinitionList.add(newDsEnvelopeDefinition);
						}

						dsException.setRetryStatus(RetryStatus.S.toString());
					}

				} catch (NewVersionExistException exp) {

					logErrorMessages("processEnvelope(): NewVersionExistException", envelopeId);

					exp.printStackTrace();

					dsException.setRetryStatus(RetryStatus.I.toString());
					dsException.setExceptionStep(FailureStep.NEW_ENVELOPE_VERSION_EXIST.toString());

					log.info("processEnvelope(): Retry Ignoring exception -> {}, envelopeId -> {} already processed",
							dsException.getId(), envelopeId);

				} catch (Throwable exp) {

					logErrorMessages("processEnvelope(): Unknown Exception", envelopeId);

					exp.printStackTrace();

					dsException.setExceptionCode(FailureCode.ERROR_214.getFailureCodeDescription());
					dsException.setExceptionReason(exp.getMessage());
					dsException.setExceptionStep(FailureStep.PREPARING_CONNECT_DATA.toString());
					updateRetryFailure(dsException);

					if (saveFailureByQueue()) {

						queueService.createFailureMessageAndSend(envelopeId, batchId, processId, exp,
								FailureCode.ERROR_214, FailureStep.PREPARING_CONNECT_DATA);
					} else {

						coreBatchDataService.createFailureProcess(envelopeId, FailureCode.ERROR_214.toString(),
								exp.getMessage(), FailureStep.PREPARING_CONNECT_DATA.toString(), processId);
					}

				}

			} else {

				logErrorMessages("processEnvelope(): Invalid Connect Message", envelopeId);

				dsException.setRetryStatus(RetryStatus.M.toString());
				dsException.setExceptionCode(FailureCode.ERROR_203.getFailureCodeDescription());
				dsException.setExceptionReason("No EnvelopeStatus in the ConnectXML");
				dsException.setExceptionStep(FailureStep.UNMARSHALL_CONNECT_XML.toString());

				if (saveFailureByQueue()) {

					queueService.createFailureMessageAndSend(envelopeId, batchId, processId,
							new InvalidInputException("Unmarshall Connect XML"), FailureCode.ERROR_203,
							FailureStep.UNMARSHALL_CONNECT_XML);
				} else {

					coreBatchDataService.createFailureProcess(envelopeId, FailureCode.ERROR_203.toString(),
							"Unmarshall Connect XML", FailureStep.UNMARSHALL_CONNECT_XML.toString(), processId);
				}

			}
		} catch (JAXBException exp) {

			logErrorMessages("processEnvelope(): JAXBException", envelopeId);

			exp.printStackTrace();

			dsException.setRetryStatus(RetryStatus.J.toString());
			dsException.setExceptionCode(FailureCode.ERROR_202.getFailureCodeDescription());
			dsException.setExceptionReason("JAXBException");
			dsException.setExceptionStep(FailureStep.UNMARSHALL_CONNECT_XML.toString());

			if (saveFailureByQueue()) {

				queueService.createFailureMessageAndSend(envelopeId, batchId, processId, exp, FailureCode.ERROR_202,
						FailureStep.UNMARSHALL_CONNECT_XML);
			} else {

				coreBatchDataService.createFailureProcess(envelopeId, FailureCode.ERROR_202.toString(), "JAXBException",
						FailureStep.UNMARSHALL_CONNECT_XML.toString(), processId);
			}
		} catch (Exception exp) {

			logErrorMessages("processEnvelope(): UnknownException", envelopeId);
			exp.printStackTrace();

			dsException.setExceptionCode(FailureCode.ERROR_107.getFailureCodeDescription());
			dsException.setExceptionReason("UnknownException");
			dsException.setExceptionStep(FailureStep.UNMARSHALL_CONNECT_XML.toString());
			updateRetryFailure(dsException);

			if (saveFailureByQueue()) {

				queueService.createFailureMessageAndSend(envelopeId, batchId, processId, exp, FailureCode.ERROR_107,
						FailureStep.UNMARSHALL_CONNECT_XML);
			} else {

				coreBatchDataService.createFailureProcess(envelopeId, FailureCode.ERROR_107.toString(),
						"UnknownException", FailureStep.UNMARSHALL_CONNECT_XML.toString(), processId);
			}
		}
	}

	public boolean canProcessConnectMessage(DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			DSEnvelopeDefinition savedDSEnvelopeDefinition) {

		log.info("canProcessConnectMessage called for envelopeId -> {}", envelopeId);
		LocalDateTime timeGeneratedTimeSaved = LocalDateTime.parse(savedDSEnvelopeDefinition.getTimeGenerated());

		XMLGregorianCalendar utcTimeGenerated = docuSignEnvelopeInformation.getEnvelopeStatus().getTimeGenerated();
		utcTimeGenerated.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());

		LocalDateTime timeGeneratedCurrentXML = utcTimeGenerated.toGregorianCalendar().toZonedDateTime()
				.toLocalDateTime();

		log.info("timeGeneratedTimeSaved -> {} and timeGeneratedCurrentXML -> {} for envelopeId -> {}",
				timeGeneratedTimeSaved, timeGeneratedCurrentXML, envelopeId);

		if (timeGeneratedTimeSaved.isBefore(timeGeneratedCurrentXML)) {

			return true;

		} else {

			log.warn("envelopeId -> {} data is latest in the DB and will not process current XML", envelopeId);
			return false;
		}
	}

	private boolean checkRetryLimitExceeded(DSExceptionDefinition dsException) {

		Long retryLimit = Long.parseLong(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_RETRY_LIMIT, PropertyCacheConstants.CONNECT_REFERENCE_NAME));

		if (dsException.getRetryCount() != null && dsException.getRetryCount() >= retryLimit) {

			dsException.setRetryStatus(RetryStatus.I.toString());
			dsException.setExceptionStep(FailureStep.RETRY_LIMIT_EXCEEDED.toString());
			log.info("checkRetryLimitExceeded(); retrycount-> {} exceeded limit -> {}", dsException.getRetryCount(),
					retryLimit);
			return true;
		} else {
			return false;
		}
	}

	public PageInformation preparePageInformation(int pageNumber, String envelopeIds) {

		Integer recordsPerPage = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_RECORDS_PERPAGE, PropertyCacheConstants.CONNECT_REFERENCE_NAME));

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(pageNumber);
		pageInformation.setRecordsPerPage(recordsPerPage);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(envelopeIds);

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
	}

	private DSExceptionDefinition updateRetryRecord(DSExceptionDefinition dsException) {

		Long retryCount = null;

		if (null != dsException.getRetryCount()) {

			retryCount = dsException.getRetryCount() + 1;
		} else {

			retryCount = 1L;
		}

		dsException.setRetryCount(retryCount);
		dsException.setRetryDateTime(LocalDateTime.now().toString());

		return dsException;
	}

	private void updateRetryFailure(DSExceptionDefinition dsException) {

		dsException.setRetryStatus(RetryStatus.F.toString());
		dsException.setExceptionDateTime(LocalDateTime.now().toString());

		log.info("Retry Failed for dsException -> {} for envelopeId -> {}", dsException.getId(),
				dsException.getEnvelopeId());
	}

	public boolean saveFailureByQueue() {

		if (StringUtils.isEmpty(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE, PropertyCacheConstants.CONNECT_REFERENCE_NAME))) {

			return false;
		} else {

			return Boolean.parseBoolean(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE, PropertyCacheConstants.CONNECT_REFERENCE_NAME));
		}
	}

	public boolean saveExceptionByQueue() {

		if (StringUtils.isEmpty(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_EXCEPTION_BYQUEUE, PropertyCacheConstants.CONNECT_REFERENCE_NAME))) {

			return false;
		} else {

			return Boolean.parseBoolean(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_EXCEPTION_BYQUEUE, PropertyCacheConstants.CONNECT_REFERENCE_NAME));
		}
	}

	private void logErrorMessages(String message, String envelopeId) {

		log.error(" ------------------------- ERROR: {} for EnvelopeId -> {} ------------------------- ", message,
				envelopeId);
	}
}