package com.ds.proserv.report.prepare.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AccountFetchAPITypes;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.common.exception.AsyncInterruptedException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.BatchResultInformation;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.ConcurrentProcessMessageDefinition;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportRunArgs;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.auth.domain.JWTParams;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.db.service.ReportJDBCService;
import com.ds.proserv.report.dsapi.service.DSAccountService;
import com.ds.proserv.report.dsapi.service.OrgAdminService;
import com.ds.proserv.report.processor.PrepareAPICallProcessor;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.ds.proserv.report.service.ReportRunParamService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDSData implements IPrepareData {

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private OrgAdminService orgAdminService;

	@Autowired
	private BatchDataService batchDataService;

	@Lazy
	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private CommonServiceData commonServiceData;

	@Autowired
	private PrepareAPICallProcessor prepareAPICallProcessor;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	DSCacheManager dsCacheManager;

	@Autowired
	DSAccountService dsAccountService;

	@Autowired
	ReportRunParamService reportRunParamService;

	private Configuration docContextPathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	private Configuration pathConfiguration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS,
			Option.AS_PATH_LIST, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	public List<String> getAllAccountIds(ReportRunArgs apiRunArgs, String apiCategory, Integer apiId) {

		log.info("Fetching AllAccountIds to be processed by this batch");

		List<String> toProcessAccountIdList = null;

		PathParam filterAccountIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.FILTER_ACCOUNT_IDS);

		JWTParams jwtParams = new JWTParams(apiRunArgs);

		if (null != filterAccountIdsParam && !StringUtils.isEmpty(filterAccountIdsParam.getParamValue())) {

			String filterAccountIds = filterAccountIdsParam.getParamValue();

			List<String> filterAccountIdList = Stream.of(filterAccountIds.split(",")).map(String::trim)
					.collect(Collectors.toList());

			String accountsFetchAPIToUse = ReportAppUtil
					.findPathParam(apiRunArgs.getPathParams(), AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE)
					.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				String inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID)
						.getParamValue();

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ORG_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ORGADMINAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);
				toProcessAccountIdList = orgAdminService.getAllAccountIds(inputOrgId, authenticationRequest).stream()
						.filter(accountId -> !filterAccountIdList.contains(accountId)).collect(Collectors.toList());

				return toProcessAccountIdList;
			}

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ACCOUNT_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ESIGNAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

				toProcessAccountIdList = dsAccountService.getAllAccountIds(authenticationRequest).stream()
						.filter(accountId -> !filterAccountIdList.contains(accountId)).collect(Collectors.toList());

				return toProcessAccountIdList;
			}
		} else {

			PathParam selectAccountIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.SELECT_ACCOUNT_IDS);

			if (null != selectAccountIdsParam && !StringUtils.isEmpty(selectAccountIdsParam.getParamValue())) {

				toProcessAccountIdList = Stream.of(selectAccountIdsParam.getParamValue().split(","))
						.collect(Collectors.toList());

				return toProcessAccountIdList;
			}

			String accountsFetchAPIToUse = ReportAppUtil
					.findPathParam(apiRunArgs.getPathParams(), AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE)
					.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				String inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID)
						.getParamValue();

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ORG_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ORGADMINAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);
				toProcessAccountIdList = orgAdminService.getAllAccountIds(inputOrgId, authenticationRequest);

				return toProcessAccountIdList;
			}

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				AuthenticationRequest authenticationRequest = new AuthenticationRequest(AppConstants.ACCOUNT_ADMIN,
						jwtParams.getJwtScopes(), jwtParams.getJwtUserId(), APICategoryType.ESIGNAPI.toString(),
						jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(),
						jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

				toProcessAccountIdList = dsAccountService.getAllAccountIds(authenticationRequest);

				return toProcessAccountIdList;
			}
		}

		return toProcessAccountIdList;
	}

	@Override
	public BatchResultInformation startPrepareDataProcess(PrepareDataAPI prepareAPI) {

		List<CompletableFuture<String>> reportDataFutureAccountList = new ArrayList<CompletableFuture<String>>();

		String batchId = null;
		try {

			batchId = startPrepareDataProcessAsync(prepareAPI, reportDataFutureAccountList);

			log.info("Size of reportDataFutureAccountList in startPrepareDataProcess is {}",
					reportDataFutureAccountList.size());
			CompletableFuture.allOf(
					reportDataFutureAccountList.toArray(new CompletableFuture[reportDataFutureAccountList.size()]))
					.get();

			log.info(
					" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ All main threads (at batch level) of size {} covering all accounts in batchId -> {} are completed ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
					reportDataFutureAccountList.size(), batchId);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_START_BYQUEUE))) {

				if (batchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

					log.info(
							"######################### All Messages queued in prepare flow for batchId -> {} #########################",
							batchId);

					BatchResultInformation batchResultInformation = new BatchResultInformation();
					batchResultInformation.setBatchId(batchId);
					batchResultInformation.setBatchStatus(ValidationResult.QUEUED.toString());

					return batchResultInformation;
				} else {

					return closeNoDataAvailableBatch(batchId);

				}
			} else {
				if (batchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

					return batchDataService.finishBatchProcessData(prepareAPI.getApiRunArgs(),
							prepareAPI.getApiRunArgs().getBatchType(), batchId);
				} else {

					return closeNoDataAvailableBatch(batchId);
				}
			}

		} catch (InterruptedException exp) {

			log.error("InterruptedException {} occurred in AbstractDSData.startPrepareDataProcess for batchId {}", exp,
					batchId);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(batchId, batchId, AppConstants.NOT_AVAILABLE_CONST, exp,
						FailureCode.ERROR_101, FailureStep.JOINING_ALL_ACCOUNT_FUTURE);

			} else {

				batchDataService.createFailureRecord(batchId, batchId, FailureCode.ERROR_101.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_ACCOUNT_FUTURE.toString(), exp,
						AppConstants.NOT_AVAILABLE_CONST);
			}

			exp.printStackTrace();

			throw new AsyncInterruptedException(
					"InterruptedException " + exp + " occurred in AbstractDSData.startPrepareDataProcess for batchId "
							+ batchId + " message " + exp.getMessage());
		} catch (ExecutionException exp) {

			log.error(
					"ExecutionException {} occurred in AbstractDSData.startPrepareDataProcess for batchId {} and the cause is {}",
					exp, batchId, exp);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(batchId, batchId, AppConstants.NOT_AVAILABLE_CONST, exp,
						FailureCode.ERROR_102, FailureStep.JOINING_ALL_ACCOUNT_FUTURE);

			} else {

				batchDataService.createFailureRecord(batchId, batchId, FailureCode.ERROR_102.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_ACCOUNT_FUTURE.toString(), exp,
						AppConstants.NOT_AVAILABLE_CONST);
			}

			exp.printStackTrace();

			throw new AsyncInterruptedException(
					"ExecutionException " + exp + " occurred in AbstractDSData.startPrepareDataProcess for batchId "
							+ batchId + " message " + exp.getMessage() + " cause is " + exp);
		}
	}

	private BatchResultInformation closeNoDataAvailableBatch(String batchId) {

		log.warn("No worker threads available in prepare flow for batchId -> {}, so closing the batch", batchId);

		batchDataService.finishBatchProcess(batchId, 0L);
		BatchResultInformation batchResultInformation = new BatchResultInformation();
		batchResultInformation.setBatchId(batchId);
		batchResultInformation.setBatchStatus(ValidationResult.NODATAAVAILABLE.toString());

		return batchResultInformation;
	}

	abstract String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList);

	@Override
	public CompletableFuture<String> callDSAPIForEachRecord(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs,
			String batchId, String accountId) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			try {

				List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList = new ArrayList<ConcurrentProcessLogDefinition>();
				List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList = new ArrayList<CompletableFuture<ConcurrentProcessLogDefinition>>();

				callDSAPIForEachRecordAsync(prepareAPI, apiRunArgs, batchId, accountId, reportDataFutureAccountPageList,
						concurrentProcessLogDefinitionList);

				CompletableFuture.allOf(reportDataFutureAccountPageList
						.toArray(new CompletableFuture[reportDataFutureAccountPageList.size()])).get();

				if (StringUtils.isEmpty(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_START_BYQUEUE))) {

					concurrentProcessLogDefinitionList.forEach(concurrentProcessLogDefinition -> {

						batchDataService
								.finishConcurrentProcessWithTotalRecords(concurrentProcessLogDefinition.getProcessId());

						log.info(
								" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Envelope Load Completed for all threads of size {} and processId {} for accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
								reportDataFutureAccountPageList.size(), concurrentProcessLogDefinition.getProcessId(),
								accountId, batchId);
					});
				}

			} catch (InterruptedException exp) {

				log.error(
						"InterruptedException {} occurred in AbstractDSData.callDSAPIForEachRecord for accountId {} and batchId {}",
						exp, accountId, batchId);

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId, batchId, AppConstants.NOT_AVAILABLE_CONST, exp,
							FailureCode.ERROR_101, FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE);

				} else {

					batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_101.toString(),
							exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp,
							AppConstants.NOT_AVAILABLE_CONST);
				}

				exp.printStackTrace();

				throw new AsyncInterruptedException("InterruptedException " + exp
						+ " occurred in AbstractDSData.callDSAPIForEachRecord for accountId " + accountId
						+ " and batchId {} " + batchId + " message " + exp.getMessage());
			} catch (ExecutionException exp) {

				log.error(
						"ExecutionException {} occurred in AbstractDSData.callDSAPIForEachRecord for accountId {} and batchId {}, and the cause is {}",
						exp, accountId, batchId, exp);

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_102.toString(),
						exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp,
						AppConstants.NOT_AVAILABLE_CONST);

				exp.printStackTrace();

				throw new AsyncInterruptedException("ExecutionException " + exp
						+ " occurred in AbstractDSData.callDSAPIForEachRecord for accountId " + accountId
						+ " and batchId " + batchId + " message " + exp.getMessage() + " cause " + exp);
			}
			return "success";
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside AbstractDSData.callDSAPIForEachRecord and exp is {} for accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, accountId, batchId);

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId, batchId, null, exp, FailureCode.ERROR_107,
							FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE);

				} else {

					batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_107.toString(),
							exp.getMessage(), FailureStep.JOINING_ALL_PAGES_ACCOUNT_FUTURE.toString(), exp,
							AppConstants.NOT_AVAILABLE_CONST);
				}

				exp.printStackTrace();

				return "exception";
			}

			return asyncStatus;
		});
	}

	abstract void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList);

	@Override
	public void callAPIAndProcessConfiguredRules(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI) {

		Map<String, Object> inputParams = reportRunParamService.prepareInputParams(prepareAPI.getApiRunArgs());

		if (!StringUtils.isEmpty(accountId)) {

			inputParams.put("inputAccountId", accountId);
		}

		if (!StringUtils.isEmpty(userId)) {

			inputParams.put("inputUserId", userId);
		}

		log.info(
				"Calling API asynchronously -> {} with inputParams -> {} for userId -> {}, accountId -> {} and batchId -> {}",
				prepareAPI.getApiUri(), inputParams, userId, accountId, batchId);

		TableColumnMetaData tableColumnMetaData = reportJDBCService.getTableColumns(prepareAPI.getApiDataTableName());

		String json = prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, inputParams, batchId, null,
				String.class, AppConstants.NOT_AVAILABLE_CONST);

		log.debug("json before parsing {} for userId -> {}, accountId -> {} and batchId -> {}", json, userId, accountId,
				batchId);

		if (!StringUtils.isEmpty(json)) {

			DocumentContext docContext = JsonPath.using(docContextPathConfiguration).parse(json);

			Object resultSetObj = docContext.read("$" + prepareAPI.getApiResultSetSizePath());

			Integer resultSetSize = 0;

			if (null != resultSetObj) {

				String resultSetSizeAsString = resultSetObj.toString();
				if (!StringUtils.isEmpty(resultSetSizeAsString)) {

					resultSetSize = Integer.parseInt(resultSetSizeAsString);
				}
			}

			if (resultSetSize > 0) {

				// Creating below concurrentProcessLogDefinition for each AccountId entry where
				// accountId is the groupId
				ConcurrentProcessLogDefinition concurrentProcessLogDefinition = batchDataService
						.createConcurrentProcess(Long.valueOf(1), batchId, accountId, null, userId);

				if (!StringUtils.isEmpty(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_START_BYQUEUE))) {

					createParallelProcessAndSendToQueue(accountId, batchId, userId, concurrentProcessLogDefinitionList,
							prepareAPI, inputParams, tableColumnMetaData, json,
							concurrentProcessLogDefinition.getProcessId(), docContext);
				} else {

					if (!StringUtils.isEmpty(prepareAPI.getApiNextPaginationPath())) {

						String nextUri = docContext.read("$" + prepareAPI.getApiNextPaginationPath());

						reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId,
								inputParams, prepareAPI, json, pathConfiguration, docContext, tableColumnMetaData,
								batchId, nextUri, concurrentProcessLogDefinition.getProcessId(), userId));

						while (!StringUtils.isEmpty(nextUri)) {

							String paginationJson = prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId,
									nextUri, batchId, String.class, null);

							DocumentContext paginationDocContext = JsonPath.using(docContextPathConfiguration)
									.parse(paginationJson);
							nextUri = paginationDocContext.read("$" + prepareAPI.getApiNextPaginationPath());

							reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId,
									inputParams, prepareAPI, paginationJson, pathConfiguration, paginationDocContext,
									tableColumnMetaData, batchId, nextUri,
									concurrentProcessLogDefinition.getProcessId(), userId));

						}

					} else {

						reportDataFutureAccountPageList.add(filterProcessedPagedDataAndTriggerSave(accountId,
								inputParams, prepareAPI, json, pathConfiguration, docContext, tableColumnMetaData, null,
								null, concurrentProcessLogDefinition.getProcessId(), userId));
					}
				}

				concurrentProcessLogDefinitionList.add(concurrentProcessLogDefinition);
			} else {

				log.warn("No Records to process in json -> {} for userId -> {}, accountId -> {} and batchId -> {}",
						json, userId, accountId, batchId);
			}

		} else {

			log.warn("JSON Empty for userId -> {}, accountId -> {} and batchId -> {}", json, userId, accountId,
					batchId);
		}
	}

	private void createParallelProcessAndSendToQueue(String accountId, String batchId, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI,
			Map<String, Object> inputParams, TableColumnMetaData tableColumnMetaData, String json, String parentGroupId,
			DocumentContext docContext) {

		if (!StringUtils.isEmpty(prepareAPI.getApiNextPaginationPath())) {

			String nextUri = docContext.read("$" + prepareAPI.getApiNextPaginationPath());

			String resultSetSizeAsString = null;
			Object resultSetObj = docContext.read("$" + prepareAPI.getApiResultSetSizePath());

			if (null != resultSetObj) {

				resultSetSizeAsString = resultSetObj.toString();

			}

			sendToQueue(accountId, batchId, userId, prepareAPI, inputParams, tableColumnMetaData, json, parentGroupId,
					nextUri, createParallelProcessLogDefinition(accountId, batchId, userId, parentGroupId,
							resultSetSizeAsString));

			while (!StringUtils.isEmpty(nextUri)) {

				String processId = createParallelProcessLogDefinition(accountId, batchId, userId, parentGroupId,
						resultSetSizeAsString);

				String paginationJson = prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, nextUri, batchId,
						String.class, processId);

				DocumentContext paginationDocContext = JsonPath.using(docContextPathConfiguration)
						.parse(paginationJson);
				nextUri = paginationDocContext.read("$" + prepareAPI.getApiNextPaginationPath());

				resultSetObj = paginationDocContext.read("$" + prepareAPI.getApiResultSetSizePath());
				if (null != resultSetObj) {

					resultSetSizeAsString = resultSetObj.toString();
				}

				sendToQueue(accountId, batchId, userId, prepareAPI, inputParams, tableColumnMetaData, paginationJson,
						parentGroupId, nextUri, processId);

			}

		} else {

			String resultSetSizeAsString = null;
			Object resultSetObj = docContext.read("$" + prepareAPI.getApiResultSetSizePath());

			if (null != resultSetObj) {

				resultSetSizeAsString = resultSetObj.toString();

			}
			sendToQueue(accountId, batchId, userId, prepareAPI, inputParams, tableColumnMetaData, json, parentGroupId,
					null, createParallelProcessLogDefinition(accountId, batchId, userId, parentGroupId,
							resultSetSizeAsString));

			/*
			 * reportDataFutureAccountPageList
			 * .add(filterProcessedPagedDataAndTriggerSave(accountId, inputParams,
			 * prepareAPI, json, pathConfiguration, docContext, tableColumnMetaData, null,
			 * null, parentGroupId, userId));
			 */
		}
	}

	private String createParallelProcessLogDefinition(String accountId, String batchId, String userId,
			String parentGroupId, String resultSetSizeAsString) {

		ConcurrentProcessLogDefinition childProcessConcurrentProcessLogDefinition = null;
		if (!StringUtils.isEmpty(resultSetSizeAsString)) {

			childProcessConcurrentProcessLogDefinition = batchDataService.createConcurrentProcess(
					Long.valueOf(resultSetSizeAsString), batchId, parentGroupId, accountId, userId);
		} else {

			childProcessConcurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(-1),
					batchId, parentGroupId, accountId, userId);
		}
		return childProcessConcurrentProcessLogDefinition.getProcessId();
	}

	private void sendToQueue(String accountId, String batchId, String userId, PrepareDataAPI prepareAPI,
			Map<String, Object> inputParams, TableColumnMetaData tableColumnMetaData, String json, String parentGroupId,
			String nextUri, String processId) {

		ConcurrentProcessMessageDefinition concurrentProcessMessageDefinition = new ConcurrentProcessMessageDefinition();

		concurrentProcessMessageDefinition.setAccountId(accountId);
		concurrentProcessMessageDefinition.setBatchId(batchId);
		concurrentProcessMessageDefinition.setInputParams(inputParams);
		concurrentProcessMessageDefinition.setNextUri(nextUri);
		concurrentProcessMessageDefinition.setPaginationJson(json);
		concurrentProcessMessageDefinition.setParentGroupId(parentGroupId);
		concurrentProcessMessageDefinition.setPrepareAPI(prepareAPI);
		concurrentProcessMessageDefinition.setProcessId(processId);
		concurrentProcessMessageDefinition.setTableColumnMetaData(tableColumnMetaData);
		concurrentProcessMessageDefinition.setUserId(userId);

		queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_START_QUEUE_NAME, processId, batchId,
				concurrentProcessMessageDefinition);
	}

	private CompletableFuture<ConcurrentProcessLogDefinition> filterProcessedPagedDataAndTriggerSave(String accountId,
			Map<String, Object> inputParams, PrepareDataAPI prepareAPI, String json, Configuration pathConfiguration,
			DocumentContext docContext, TableColumnMetaData tableColumnMetaData, String batchId, String nextUri,
			String parentGroupId, String userId) {

		log.info(
				"About to call processFilterPrepareJsonData asynchronously for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {}",
				nextUri, parentGroupId, accountId, batchId);

		return CompletableFuture.supplyAsync((Supplier<ConcurrentProcessLogDefinition>) () -> {

			log.info(
					" $$$$$$$$$$$$$$$$$$$$ Inside processFilterPrepareJsonData.supplyAsync $$$$$$$$$$$$$$$$$$$$ for nextUri -> {}, parentGroupId -> {}, accountId -> {}, batchId -> {} and inputParams is {}",
					nextUri, parentGroupId, accountId, batchId, inputParams);

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = commonServiceData
					.processEachPageJSONResponse(accountId, inputParams, prepareAPI, json, pathConfiguration,
							docContext, tableColumnMetaData, batchId, nextUri, parentGroupId, userId, null);

			return concurrentProcessLogDefinition;
		}, recordTaskExecutor).thenApplyAsync(concurrentProcessLogDefinition -> {

			log.info(
					" %%%%%%%%%%%%%%%%%%%% Inside processFilterPrepareJsonData.applyAsync %%%%%%%%%%%%%%%%%%%% for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {}  and processId -> {}",
					nextUri, parentGroupId, accountId, batchId, concurrentProcessLogDefinition.getProcessId());

			batchDataService.finishConcurrentProcess(concurrentProcessLogDefinition.getProcessId());

			return concurrentProcessLogDefinition;

		}, recordTaskExecutor).handle((concurrentProcessLogDefinition, exp) -> {

			log.info(
					" ^^^^^^^^^^^^^^^^^^^^ Inside processFilterPrepareJsonData.handleAsync ^^^^^^^^^^^^^^^^^^^^ for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {}",
					nextUri, parentGroupId, accountId, batchId);

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside processFilterPrepareJsonData.handleAsync and exp is {} for nextUri -> {}, parentGroupId -> {}, accountId -> {} and batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, nextUri, parentGroupId, accountId, batchId);

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId + "_" + nextUri, batchId,
							AppConstants.NOT_AVAILABLE_CONST, exp, FailureCode.ERROR_107,
							FailureStep.OUTER_JSON_FILTER_PROCESSING);

				} else {

					batchDataService.createFailureRecord(accountId + "_" + nextUri, batchId,
							FailureCode.ERROR_107.toString(), exp.getMessage(),
							FailureStep.OUTER_JSON_FILTER_PROCESSING.toString(), exp, AppConstants.NOT_AVAILABLE_CONST);
				}

				exp.printStackTrace();
			} else if (null != concurrentProcessLogDefinition) {

				log.info(
						"No Exception occurred in processFilterPrepareJsonData.handleAsync in processing data for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {} and processId -> {}",
						parentGroupId, accountId, batchId, concurrentProcessLogDefinition.getProcessId());

			} else {

				log.warn(
						" !!!!!!!!!!!!!!!!!!!! Something went wrong, No Exception occurred in processFilterPrepareJsonData.handleAsync nor there are any processes created !!!!!!!!!!!!!!!!!!!!");
			}

			return concurrentProcessLogDefinition;
		});
	}

	@Override
	public String createBatchRecord(ReportRunArgs apiRunArgs, List<String> recordIds) {

		String batchType = apiRunArgs.getBatchType();

		Map<String, Object> inputParams = reportRunParamService.prepareInputParams(apiRunArgs);

		BatchStartParams batchStartParams = new BatchStartParams();

		if (null != inputParams.get(AppConstants.INPUT_FROM_DATE)) {

			batchStartParams.setBeginDateTime((String) inputParams.get(AppConstants.INPUT_FROM_DATE));
		}

		if (null != inputParams.get(AppConstants.INPUT_TO_DATE)) {

			batchStartParams.setEndDateTime((String) inputParams.get(AppConstants.INPUT_TO_DATE));
		}

		String recordIdsCommaSeparated = null;
		if (null != recordIds && !recordIds.isEmpty()) {

			batchStartParams.setTotalRecordIds(recordIds.size());

			recordIdsCommaSeparated = recordIds.stream().collect(Collectors.joining(","));
		}

		String batchId = batchDataService.createBatchJob(batchType, batchStartParams, null);

		log.info(
				" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Running batchId -> {} for inputStartDate -> {}, inputToDate -> {} and accountIds -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ",
				batchId, inputParams.get(AppConstants.INPUT_FROM_DATE), inputParams.get(AppConstants.INPUT_TO_DATE),
				recordIdsCommaSeparated);
		return batchId;
	}

}