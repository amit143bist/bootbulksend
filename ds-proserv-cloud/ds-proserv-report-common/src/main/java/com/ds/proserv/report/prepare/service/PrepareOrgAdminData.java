package com.ds.proserv.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportRunArgs;

@Component
public class PrepareOrgAdminData extends AbstractDSData {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ORGADMINAPI == apiCategoryType;
	}

	@Override
	public String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList) {

		String batchId = createBatchRecord(prepareAPI.getApiRunArgs(), null);

		reportDataFutureAccountList
				.add(callDSAPIForEachRecord(prepareAPI, prepareAPI.getApiRunArgs(), batchId, "OrgAdmin"));

		return batchId;
	}

	@Override
	void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList) {

		callAPIAndProcessConfiguredRules(accountId, batchId, reportDataFutureRecordPageList,
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_USERID),
				concurrentProcessLogDefinitionList, prepareAPI);
	}

}