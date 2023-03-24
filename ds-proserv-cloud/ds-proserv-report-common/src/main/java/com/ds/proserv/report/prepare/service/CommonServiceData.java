package com.ds.proserv.report.prepare.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.Filter;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.processor.FilterDataProcessor;
import com.ds.proserv.report.processor.GenerateDecorateReportDataProcessor;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommonServiceData {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private FilterDataProcessor filterDataProcessor;

	@Autowired
	private GenerateDecorateReportDataProcessor generateReportDataProcessor;

	public ConcurrentProcessLogDefinition processEachPageJSONResponse(String accountId, Map<String, Object> inputParams,
			PrepareDataAPI prepareAPI, String json, Configuration pathConfiguration, DocumentContext docContext,
			TableColumnMetaData tableColumnMetaData, String batchId, String nextUri, String parentGroupId,
			String userId, String processId) {

		long batchSize = 0;
		List<String> pathList = null;
		List<Filter> commonFilters = prepareAPI.getCommonFilters();

		String resultSetSizeAsString = null;
		Object resultSetObj = docContext.read("$" + prepareAPI.getApiResultSetSizePath());

		Integer resultSetSize = 0;
		if (null != resultSetObj) {

			resultSetSizeAsString = resultSetObj.toString();

			if (!StringUtils.isEmpty(resultSetSizeAsString)) {

				resultSetSize = Integer.parseInt(resultSetSizeAsString);
			}
		}

		try {

			if (null != resultSetSize && resultSetSize > 0 && null != commonFilters && !commonFilters.isEmpty()) {

				for (int i = 0; i < commonFilters.size(); i++) {

					if (i == 0 && null == pathList) {

						pathList = filterDataProcessor.createPathList(commonFilters.get(i), pathConfiguration, json,
								inputParams);
					} else {

						filterDataProcessor.processFilterData(pathConfiguration, pathList, commonFilters.get(i), json,
								inputParams, docContext, accountId, batchId, nextUri, null, processId);
					}
				}

			}

		} catch (Exception exp) {

			if (!StringUtils.isEmpty(exp.getMessage()) && exp.getMessage().contains("No results")) {

				log.warn(
						" ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Exception {} occurred in processFilterPrepareJsonData, so No results will be generated for json -> {} nextUri -> {}, parentGroupId ->{}, accountId -> {} and batchId -> {} ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ",
						exp, json, nextUri, parentGroupId, accountId, batchId);
				batchSize = 0;
			} else {

				batchSize = -1;

				if (StringUtils.isEmpty(nextUri)) {

					log.info("NextUri is empty or null at this point for processId -> {}", processId);
					if (!StringUtils.isEmpty(dsCacheManager
							.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

						queueService.createFailureMessageAndSend(accountId, batchId, processId, exp,
								FailureCode.ERROR_107, FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC);

					} else {

						batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_107.toString(),
								exp.getMessage(), FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC.toString(), exp,
								processId);
					}

				} else {

					if (!StringUtils.isEmpty(dsCacheManager
							.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

						queueService.createFailureMessageAndSend(accountId, batchId, processId, exp,
								FailureCode.ERROR_107, FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC);

					} else {

						batchDataService.createFailureRecord(accountId + "_" + nextUri, batchId,
								FailureCode.ERROR_107.toString(), exp.getMessage(),
								FailureStep.OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC.toString(), exp, processId);
					}
				}

				log.error(
						" ------------------------------ Exception {} occurred in processFilterPrepareJsonData, so no Report will be generated for nextUri -> {}, parentGroupId ->{}, accountId -> {} and batchId -> {} ------------------------------ ",
						exp, nextUri, parentGroupId, accountId, batchId);
				exp.printStackTrace();
			}

		}

		// Above code block generates final pathList from outerJSON processing to be
		// sent for column data processing

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = null;

		if (null != pathList && !pathList.isEmpty()) {

			if (StringUtils.isEmpty(processId)) {

				concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(pathList.size()),
						batchId, parentGroupId, accountId, userId);
				processId = concurrentProcessLogDefinition.getProcessId();
			} else {

				concurrentProcessLogDefinition = createConcurrentProcessLogDefinition(batchId, processId,
						Long.valueOf(pathList.size()));
			}

			concurrentProcessLogDefinition.setReportRowsList(generateReportDataProcessor.generateAndSaveReportData(
					pathList, docContext, prepareAPI.getOutputColumns(), accountId, inputParams, pathConfiguration,
					tableColumnMetaData, batchId, processId, nextUri, prepareAPI));
		} else {

			if (StringUtils.isEmpty(processId)) {

				concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(batchSize),
						batchId, parentGroupId, accountId, userId);
				processId = concurrentProcessLogDefinition.getProcessId();
			} else {

				concurrentProcessLogDefinition = createConcurrentProcessLogDefinition(batchId, processId, batchSize);
			}

			log.warn(
					" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ PathList {} is empty, so no Report will be generated for nextUri -> {}, parentGroupId -> {}, accountId -> {} in batchId -> {} and processId -> {} @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ",
					pathList, nextUri, parentGroupId, accountId, batchId, processId);
		}
		return concurrentProcessLogDefinition;
	}

	private ConcurrentProcessLogDefinition createConcurrentProcessLogDefinition(String batchId, String processId,
			long batchSize) {

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessId(processId);
		concurrentProcessLogDefinition.setTotalRecordsInProcess(Long.valueOf(batchSize));
		return concurrentProcessLogDefinition;
	}

}