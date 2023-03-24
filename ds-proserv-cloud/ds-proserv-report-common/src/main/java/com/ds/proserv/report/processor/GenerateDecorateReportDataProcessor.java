package com.ds.proserv.report.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.cache.manager.DSCacheService;
import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.report.domain.CommonPathData;
import com.ds.proserv.feign.report.domain.DecorateOutput;
import com.ds.proserv.feign.report.domain.Filter;
import com.ds.proserv.feign.report.domain.OutputColumn;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.db.service.ReportJDBCService;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenerateDecorateReportDataProcessor {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ScriptEngineManager scriptEngineManager;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private DSCacheService dsCacheService;

	@Autowired
	private BatchDataService batchDataService;

	@Lazy
	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private PathValueProcessor pathValueProcessor;

	@Autowired
	private FilterDataProcessor filterDataProcessor;

	@Autowired
	private PrepareAPICallProcessor prepareAPICallProcessor;

	@Autowired
	private JavascriptFunctionProcessor javascriptFunctionProcessor;

	private Configuration docContextPathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	public List<List<ReportData>> generateAndSaveReportData(List<String> pathList, DocumentContext docContext,
			List<OutputColumn> outputColumnList, String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, TableColumnMetaData tableColumnMetaData, String batchId, String processId,
			String nextUri, PrepareDataAPI prepareAPI) {

		log.info(
				"generateAndSaveReportData called for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
				nextUri, accountId, batchId, processId);

		List<List<ReportData>> reportRowsList = null;
		if (null != pathList && !pathList.isEmpty()) {

			reportRowsList = new ArrayList<List<ReportData>>(pathList.size());

			try {

				createRowDataForEachPath(pathList, docContext, outputColumnList, accountId, inputParams,
						pathConfiguration, batchId, processId, nextUri, reportRowsList);

				if (null != reportRowsList && !reportRowsList.isEmpty()) {

					log.info(
							"reportRowsList is not empty for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {} ",
							nextUri, accountId, batchId, processId);

					String sendToReportDataQueue = dsCacheManager
							.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_REPORTDATA_BYQUEUE);

					if (StringUtils.isEmpty(sendToReportDataQueue)
							|| AppConstants.APP_FALSE.equalsIgnoreCase(sendToReportDataQueue)) {

						String primaryIds = reportJDBCService.saveReportData(reportRowsList, tableColumnMetaData,
								accountId, batchId, processId, nextUri, prepareAPI.getOutputApiPrimaryId());

						if (!StringUtils.isEmpty(primaryIds)) {

							log.info("Calling update for primaryIds -> {}", primaryIds);
							accountId = verifyAccountIdForDocuSign(accountId, prepareAPI);
							prepareAPICallProcessor.callPrepareAPI(prepareAPI, accountId, inputParams, null, nextUri,
									batchId, String.class, processId, primaryIds);
						}
					}
				} else {

					log.info(
							"reportRowsList is null or empty for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {} ",
							nextUri, accountId, batchId, processId);
				}

			} catch (ResourceNotSavedException exp) {

				log.error(
						"Exception {} occurred in saving generateAndSaveReportData in tableName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
						exp, tableColumnMetaData.getTableName(), reportRowsList.size(), accountId, batchId, processId,
						nextUri);
				exp.printStackTrace();
				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId, batchId, processId, exp, FailureCode.ERROR_105,
							FailureStep.SAVING_REPORT_DATA_IN_DB);

				} else {

					batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_105.toString(),
							exp.getMessage(), FailureStep.SAVING_REPORT_DATA_IN_DB.toString(), exp, processId);
				}

			}
		} else {

			log.info(
					"PathLists is empty, generateAndSaveReportData called for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					nextUri, accountId, batchId, processId);
		}

		return reportRowsList;

	}

	private void createRowDataForEachPath(List<String> pathList, DocumentContext docContext,
			List<OutputColumn> outputColumnList, String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, String batchId, String processId, String nextUri,
			List<List<ReportData>> reportRowsList) {

		for (String path : pathList) {// envelopes[0]....

			List<ReportData> reportColumnsDataList = new ArrayList<ReportData>(pathList.size());
			findColumnsDataForEachPath(docContext, outputColumnList, accountId, inputParams, pathConfiguration, batchId,
					processId, nextUri, path, reportColumnsDataList);

			reportRowsList.add(reportColumnsDataList);
		}
	}

	private void findColumnsDataForEachPath(DocumentContext docContext, List<OutputColumn> outputColumnList,
			String accountId, Map<String, Object> inputParams, Configuration pathConfiguration, String batchId,
			String processId, String nextUri, String path, List<ReportData> reportColumnsDataList) {

		for (OutputColumn outputColumn : outputColumnList) {

			log.debug(
					"In generateAndSaveReportData Path is {}, outputColumn name is {} and outputColumn path is {} for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					path, outputColumn.getColumnName(), outputColumn.getColumnPath(), nextUri, accountId, batchId,
					processId);

			CommonPathData commonPathData = new CommonPathData();
			commonPathData.setOuterPath(path);
			commonPathData.setColumnDataType(outputColumn.getColumnDataType());
			commonPathData.setColumnPath(outputColumn.getColumnPath());
			commonPathData.setColumnDataPattern(outputColumn.getColumnDataPattern());
			commonPathData.setArrayIndex(outputColumn.getColumnDataArrayIndex());
			commonPathData.setMapKey(outputColumn.getColumnDataMapKey());
			commonPathData.setOutputDataPattern(outputColumn.getColumnOutputDataPattern());
			commonPathData.setStartIndex(outputColumn.getStartIndex());
			commonPathData.setEndIndex(outputColumn.getEndIndex());
			commonPathData.setOutputDelimiter(outputColumn.getOutputDelimiter());
			commonPathData.setTimeZone(outputColumn.getTimeZone());

			Object pathValue = pathValueProcessor.evaluatePathValue(commonPathData, docContext, accountId, batchId,
					nextUri, inputParams);

			log.debug(
					"For columnName -> {}, calculated pathValue is -> {} for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					outputColumn.getColumnName(), pathValue, nextUri, accountId, batchId, processId);

			if (null != pathValue && null != outputColumn.getAssociatedData()
					&& !StringUtils.isEmpty(outputColumn.getAssociatedData().getApiUri())) {

				fetchAndCacheAssociatedData(accountId, inputParams, pathConfiguration, batchId, processId, nextUri,
						reportColumnsDataList, outputColumn, pathValue);

			} else {

				decorateAndCreateReportData(reportColumnsDataList, outputColumn, pathValue, accountId, batchId,
						processId, inputParams, nextUri);
			}

		}
	}

	private void fetchAndCacheAssociatedData(String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, String batchId, String processId, String nextUri,
			List<ReportData> reportColumnsDataList, OutputColumn outputColumn, Object pathValue) {

		Object associatedDataValue = null;
		if (null != inputParams
				&& AppConstants.APP_TRUE.equalsIgnoreCase((String) inputParams.get(AppConstants.REFRESH_DATA_BASE))) {

			log.debug(
					"RefreshDataBase value is true for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
					nextUri, accountId, batchId, processId);

			associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
					pathValue, batchId, processId, nextUri, reportColumnsDataList);

			dsCacheService.deleteByCacheKeyAndCacheReference((String) pathValue, outputColumn.getColumnName());

			if (null != associatedDataValue) {

				saveCacheData(pathValue, associatedDataValue, accountId, batchId, processId,
						outputColumn.getColumnName());
			}

		} else {

			try {

				if (AppConstants.APP_TRUE.equalsIgnoreCase(outputColumn.getAssociatedData().getSaveDataInCache())) {

					associatedDataValue = dsCacheService
							.findByCacheKeyAndCacheReference((String) pathValue, outputColumn.getColumnName()).getBody()
							.getCacheValue();

					log.debug(
							"Fetched {} from cache for key {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
							associatedDataValue, pathValue, nextUri, accountId, batchId, processId);

				} else {

					associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
							pathValue, batchId, processId, nextUri, reportColumnsDataList);
				}

			} catch (ResourceNotFoundException exception) {

				log.info(
						"No cacheData exists for cacheKey -> {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
						pathValue, nextUri, accountId, batchId, processId);

				associatedDataValue = extractAssociatedData(accountId, inputParams, pathConfiguration, outputColumn,
						pathValue, batchId, processId, nextUri, reportColumnsDataList);

				try {

					CacheLogDefinition cacheLogDefinitionFromValue = dsCacheService.findByCacheValueAndCacheReference(
							(String) associatedDataValue, outputColumn.getColumnName()).getBody();

					cacheLogDefinitionFromValue.setCacheKey((String) pathValue);
					cacheLogDefinitionFromValue.setCacheReference(outputColumn.getColumnName());
					dsCacheService.updateCache(cacheLogDefinitionFromValue.getCacheId(), cacheLogDefinitionFromValue);

				} catch (ResourceNotFoundException innerException) {

					log.info(
							"No cacheData exists for cacheValue -> {} for  nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
							associatedDataValue, nextUri, accountId, batchId, processId);

					if (null != associatedDataValue) {

						saveCacheData(pathValue, associatedDataValue, accountId, batchId, processId,
								outputColumn.getColumnName());
					}
				}

			}
		}

		decorateAndCreateReportData(reportColumnsDataList, outputColumn, associatedDataValue, accountId, batchId,
				processId, inputParams, nextUri);
	}

	private void saveCacheData(Object pathValue, Object associatedDataValue, String accountId, String batchId,
			String processId, String cachereference) {

		log.debug(
				"SaveCacheData called for cacheKey -> {} and cacheValue -> {} for accountId -> {}, batchId -> {} and processId -> {}",
				pathValue, associatedDataValue, accountId, batchId, processId);

		CacheLogDefinition cacheLogDefinition = null;
		cacheLogDefinition = new CacheLogDefinition();
		cacheLogDefinition.setCacheKey((String) pathValue);
		cacheLogDefinition.setCacheValue((String) associatedDataValue);
		cacheLogDefinition.setCacheReference(cachereference);

		dsCacheService.saveCache(cacheLogDefinition);
	}

	private ReportData decorateAndCreateReportData(List<ReportData> reportColumnsData, OutputColumn outputColumn,
			Object associatedDataValue, String accountId, String batchId, String processId,
			Map<String, Object> inputParams, String nextUri) {

		if (null != outputColumn.getDecorateOutput()) {

			return decorateOutput(outputColumn, associatedDataValue, reportColumnsData, accountId, batchId, processId,
					inputParams, nextUri);
		} else {
			
			log.info("OutputColumn is {} and pathValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
					outputColumn.getColumnName(), associatedDataValue, accountId, batchId, processId);

			ReportData reportData = new ReportData();

			reportData.setReportColumnName(outputColumn.getColumnName());

			if (null != associatedDataValue) {

				reportData.setReportColumnValue(associatedDataValue);
			} else {
				reportData.setReportColumnValue(null);
			}

			log.debug(
					"ReportColumnName is {} and ReportColumnValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
					reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);

			if (!StringUtils.isEmpty(reportData.getReportColumnName())) {

				reportColumnsData.add(reportData);
			} else {

				log.warn(
						"ReportColumnName() {} is null for value -> {} for accountId -> {}, batchId -> {} and processId -> {}",
						reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId,
						processId);
			}

			return reportData;
		}

	}

	private Object extractAssociatedData(String accountId, Map<String, Object> inputParams,
			Configuration pathConfiguration, OutputColumn outputColumn, Object pathValue, String batchId,
			String processId, String nextUri, List<ReportData> reportColumnsData) {

		Object associatedValue = null;
		PrepareDataAPI dataPrepareAPI = outputColumn.getAssociatedData();
		try {

			accountId = verifyAccountIdForDocuSign(accountId, dataPrepareAPI);

			String json = prepareAPICallProcessor.callPrepareAPI(dataPrepareAPI, accountId, inputParams, pathValue,
					batchId, String.class, processId);

			if (!StringUtils.isEmpty(json)) {

				List<String> assocAPIDataPathList = null;
				DocumentContext associatedDocContext = JsonPath.using(docContextPathConfiguration).parse(json);
				List<Filter> commonFilters = dataPrepareAPI.getCommonFilters();

				if (null != commonFilters && !commonFilters.isEmpty()) {

					for (int i = 0; i < commonFilters.size(); i++) {

						if (i == 0 && null == assocAPIDataPathList) {

							assocAPIDataPathList = filterDataProcessor.createPathList(commonFilters.get(i),
									pathConfiguration, json, inputParams);
						} else {

							associatedValue = filterDataProcessor.processFilterData(pathConfiguration,
									assocAPIDataPathList, commonFilters.get(i), json, inputParams, associatedDocContext,
									accountId, batchId, nextUri, reportColumnsData, processId);
						}
					}
				}

				// Test below Code for more columns from AssociatedData, for instance tabData
				if (null != assocAPIDataPathList && !assocAPIDataPathList.isEmpty()
						&& null != dataPrepareAPI.getOutputColumns() && !dataPrepareAPI.getOutputColumns().isEmpty()) {

					for (String path : assocAPIDataPathList) {

						findColumnsDataForEachPath(associatedDocContext, dataPrepareAPI.getOutputColumns(), accountId,
								inputParams, pathConfiguration, batchId, processId, nextUri, path, reportColumnsData);
					}
				}

				log.warn(
						"AssociatedValue {} is extracted from extractAssociatedData for nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {}",
						associatedValue, nextUri, accountId, batchId, processId);
			}

		} catch (Exception exp) {

			log.error(
					"Exception {} occurred in extracting associatedData for pathValue -> {}, nextUri -> {}, accountId -> {}, batchId -> {} and processId -> {} from {}",
					exp, pathValue, nextUri, accountId, batchId, processId, dataPrepareAPI.getApiUri());
			throw exp;
		}

		return associatedValue;
	}

	private String verifyAccountIdForDocuSign(String accountId, PrepareDataAPI dataPrepareAPI) {
		APICategoryType apiCategoryType = ReportAppUtil.getAPICategoryType(dataPrepareAPI.getApiCategory());

		if ((StringUtils.isEmpty(accountId) || AppConstants.NOT_AVAILABLE_CONST.equalsIgnoreCase(accountId))
				&& (apiCategoryType == APICategoryType.ESIGNAPI || apiCategoryType == APICategoryType.CLICKAPI
						|| apiCategoryType == APICategoryType.ROOMSAPI)) {

			PathParam accountIdParam = ReportAppUtil.findPathParam(dataPrepareAPI.getApiParams(),
					AppConstants.DS_ACCOUNT_ID);

			if (null != accountIdParam && !StringUtils.isEmpty(accountIdParam.getParamValue())) {

				accountId = accountIdParam.getParamValue();
			} else {

				log.error("AccountId is not properly set for apiId -> {} and apiCategory -> {}",
						dataPrepareAPI.getApiId(), dataPrepareAPI.getApiCategory());
				throw new InvalidInputException("AccountId is not properly set for apiId -> "
						+ dataPrepareAPI.getApiId() + " for apiCategory -> " + dataPrepareAPI.getApiCategory());
			}
		}
		return accountId;
	}

	private ReportData decorateOutput(OutputColumn outputColumn, Object pathValue, List<ReportData> reportColumnsData,
			String accountId, String batchId, String processId, Map<String, Object> inputParams, String nextUri) {

		ReportData reportData = new ReportData();

		log.debug(
				"In decorateOutput OutputColumnName is {} for pathValue -> {} for accountId -> {}, batchId -> {} and processId -> {}",
				outputColumn.getColumnName(), pathValue, accountId, batchId, processId);

		DecorateOutput decorateOutput = outputColumn.getDecorateOutput();
		String functionExpression = decorateOutput.getOutputPatternExpression();

		ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");

		Object columnDecoratedValue = javascriptFunctionProcessor.evaluateJSFunctionExpression(inputParams,
				decorateOutput.getPathParams(), null, engine, functionExpression, pathValue, accountId, batchId,
				nextUri, reportColumnsData, processId);

		reportData.setReportColumnName(outputColumn.getColumnName());
		reportData.setReportColumnValue(columnDecoratedValue);

		log.debug(
				"In decorateOutput ReportColumnName is {} and ReportColumnValue is {} for accountId -> {}, batchId -> {} and processId -> {}",
				reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);

		if (!StringUtils.isEmpty(reportData.getReportColumnName())) {

			reportColumnsData.add(reportData);
		} else {

			log.warn(
					"In decorateOutput ReportColumnName() {} is null for value -> {} for accountId -> {}, batchId -> {} and processId -> {}",
					reportData.getReportColumnName(), reportData.getReportColumnValue(), accountId, batchId, processId);
		}

		return reportData;
	}

}