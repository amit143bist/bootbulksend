package com.ds.proserv.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.BatchResultInformation;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportRunArgs;

public interface IPrepareData {

	boolean canHandleRequest(APICategoryType apiCategoryType);

	BatchResultInformation startPrepareDataProcess(PrepareDataAPI prepareAPI);

	CompletableFuture<String> callDSAPIForEachRecord(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs,
			String batchId, String accountId);

	void callAPIAndProcessConfiguredRules(String accountId, String batchId,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureAccountPageList, String userId,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList, PrepareDataAPI prepareAPI);
	
	String createBatchRecord(ReportRunArgs apiRunArgs, List<String> recordIds);

}