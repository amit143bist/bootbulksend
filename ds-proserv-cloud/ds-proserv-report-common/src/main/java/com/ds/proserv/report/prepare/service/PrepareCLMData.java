package com.ds.proserv.report.prepare.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportRunArgs;

@Component
public class PrepareCLMData extends AbstractDSData {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.CLMAPI == apiCategoryType;
	}

	@Override
	public String startPrepareDataProcessAsync(PrepareDataAPI prepareAPI,
			List<CompletableFuture<String>> reportDataFutureAccountList) {

		return null;
	}

	@Override
	void callDSAPIForEachRecordAsync(PrepareDataAPI prepareAPI, ReportRunArgs apiRunArgs, String batchId,
			String accountId, List<CompletableFuture<ConcurrentProcessLogDefinition>> reportDataFutureRecordPageList,
			List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList) {

	}

}