package com.ds.proserv.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportRunArgs;
import com.ds.proserv.feign.util.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrepareESignData extends AbstractDSData {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ESIGNAPI == apiCategoryType;
	}

	@Override
	public String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList) {

		ReportRunArgs apiRunArgs = prepareAPI.getApiRunArgs();
		reportRunParamService.validateReportRunArgs(apiRunArgs);
		List<String> accountIds = getAllAccountIds(apiRunArgs, prepareAPI.getApiCategory(), prepareAPI.getApiId());

		String batchId = createBatchRecord(apiRunArgs, accountIds);

		accountIds.forEach(accountId -> {

			reportDataFutureAccountList.add(callDSAPIForEachRecord(prepareAPI, apiRunArgs, batchId, accountId));
		});

		return batchId;
	}

	@Override
	void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList) {

		PathParam processAllUserPathParam = ReportAppUtil.findPathParam(prepareAPI.getApiRunArgs().getPathParams(),
				AppConstants.PROCESS_ALL_USERS_FLAG);

		if (null != processAllUserPathParam && !StringUtils.isEmpty(processAllUserPathParam.getParamValue())
				&& AppConstants.APP_TRUE.equalsIgnoreCase(processAllUserPathParam.getParamValue())) {// If true, process
																										// API
			// call for
			// each user

			List<String> accountUserIdList = dsAccountService.getAllUserIds(apiRunArgs, accountId,
					prepareAPI.getApiCategory(), prepareAPI.getApiId());

			accountUserIdList.forEach(accountUserId -> {

				filterAndTriggerAPICall(accountId, batchId, reportDataFutureRecordPageList, accountUserId,
						concurrentProcessLogDefinitionList, prepareAPI);
			});

		} else {

			filterAndTriggerAPICall(accountId, batchId, reportDataFutureRecordPageList,
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_USERID),
					concurrentProcessLogDefinitionList, prepareAPI);
		}

	}

	private void filterAndTriggerAPICall(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI) {

		log.debug("Inside callDSAPI for accountId -> {}, batchId -> {} and userId -> {}", accountId, batchId, userId);
		// Do not process the DataPrepareAPI if accountId is one of the filterAccountIds

		List<String> filterAccountIdList = null;
		String filterAccountIds = prepareAPI.getFilterAccountIds();
		if (!StringUtils.isEmpty(filterAccountIds)) {

			filterAccountIdList = Stream.of(filterAccountIds.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
					.collect(Collectors.toList());
		}

		if (null != filterAccountIdList && !filterAccountIdList.isEmpty() && filterAccountIdList.contains(accountId)) {

			return;
		}
		// Do not process the DataPrepareAPI if accountId is one of the filterAccountIds

		callAPIAndProcessConfiguredRules(accountId, batchId, reportDataFutureAccountPageList, userId,
				concurrentProcessLogDefinitionList, prepareAPI);
	}

}