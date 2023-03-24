package com.ds.proserv.notification.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DecorateOutputType;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdResponse;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataDefinition;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataInformation;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataRequest;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataResponse;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.domain.Accumulator;
import com.ds.proserv.feign.domain.Pair;
import com.ds.proserv.feign.report.domain.DecorateOutput;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.feign.util.FileUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.notification.client.CustomEnvelopeDataClient;
import com.ds.proserv.notification.client.MigrationDataClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateCSVService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private PrepareReportDefinition prepareReportDefinition;

	@Autowired
	private MigrationDataClient migrationDataClient;

	@Autowired
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	private PageInformation preparePageInformation(String bucketName, int pageNumber, int paginationLimit) {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.BUCKET_PARAM_NAME);
		pageQueryParam.setParamValue(bucketName);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGENUMBER_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(pageNumber));

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGINATIONLIMIT_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(paginationLimit));

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}

	private int getEnvelopesPaginationLimit() {

		String paginationLimit = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.ENVELOPES_PAGINATION_LIMIT);

		if (!StringUtils.isEmpty(paginationLimit)) {

			return Integer.parseInt(paginationLimit);
		}

		return 1000;
	}

	public int createCSV(String bucketName, String batchId) throws IOException {

		int fileCount = 0;
		for (ManageDataAPI manageDataAPI : prepareReportDefinition.getManageDataAPIs()) {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = coreBatchDataService
					.createConcurrentProcess(0L, batchId);

			List<DecorateOutput> decorateOutputList = manageDataAPI.getDecorateOutput();

			MigrationReportDataRequest migrationReportDataRequest = new MigrationReportDataRequest();
			migrationReportDataRequest.setProcessId(concurrentProcessLogDefinition.getProcessId());
			migrationReportDataRequest.setBatchId(batchId);
			migrationReportDataRequest.setSelectSql(manageDataAPI.getSelectSql());
			migrationReportDataRequest.setCsvColumns(manageDataAPI.getCsvColumns());

			Integer csvRowLimitValue = checkCSVRowLimit(manageDataAPI.getExportRunArgs().getPathParams());
			PathParam csvPathParam = extractCSVFilePath(manageDataAPI.getExportRunArgs().getPathParams());

			int totalRecordsInProcess = 0;
			int pageNumber = 0;

			MigrationReportDataResponse migrationReportDataResponse = migrationDataClient
					.createHeaderList(manageDataAPI.getTableName(), migrationReportDataRequest).getBody();
			Map<String, String> columnNameHeaderMap = migrationReportDataResponse.getCsvHeaderMap();

			PathParam countColumnNamePathParam = ReportAppUtil.findPathParam(
					manageDataAPI.getExportRunArgs().getPathParams(), AppConstants.CSV_FETCH_COUNT_COLUMN_NAME);

			String countColumnValue = null;
			if (null != countColumnNamePathParam) {

				countColumnValue = countColumnNamePathParam.getParamValue();
			}

			CustomEnvelopeDataIdResponse customEnvelopeDataIdResponse = customEnvelopeDataClient
					.findAllByDownloadBucketName(
							preparePageInformation(bucketName, pageNumber, getEnvelopesPaginationLimit()))
					.getBody();

			if (null != customEnvelopeDataIdResponse && null != customEnvelopeDataIdResponse.getTotalRecords()
					&& customEnvelopeDataIdResponse.getTotalRecords() > 0) {

				List<String> envelopeIds = customEnvelopeDataIdResponse.getEnvelopeIds();

				log.info("envelopeIds -> {} to migrate for processId -> {}", envelopeIds,
						concurrentProcessLogDefinition.getProcessId());

				MigrationReportDataDefinition migrationReportDataDefinition = new MigrationReportDataDefinition();

				Map<String, Object> inputParams = new HashMap<String, Object>();
				List<PathParam> sqlParamList = manageDataAPI.getSqlParams();

				sqlParamList.forEach(sqlParam -> {

					inputParams.put(sqlParam.getParamName(), envelopeIds);
				});

				migrationReportDataDefinition.setColumnNameHeaderMap(columnNameHeaderMap);
				migrationReportDataDefinition.setCsvReportDataExport(manageDataAPI);
				migrationReportDataDefinition.setInputParams(inputParams);
				migrationReportDataDefinition.setBatchId(batchId);
				migrationReportDataDefinition.setProcessId(concurrentProcessLogDefinition.getProcessId());

				MigrationReportDataInformation migrationReportDataInformation = migrationDataClient
						.readReportData(migrationReportDataDefinition).getBody();

				if (null != migrationReportDataInformation && null != migrationReportDataInformation
						&& migrationReportDataInformation.getTotalRecords() > 0) {

					int rowSize = 0;
					AtomicInteger counter = new AtomicInteger(1);

					String currDateTime = LocalDateTime.now()
							.format(DateTimeFormatter.ofPattern(DateTimeUtil.FILE_DATE_PATTERN));

					ReportAppUtil.createDirectoryNIO(
							csvPathParam.getParamValue() + File.separator + bucketName + File.separator + "manifest");

					List<Map<String, Object>> reportDataList = migrationReportDataInformation.getReportDataList();

					log.info("reportDataList size -> {} to migrate for processId -> {}", reportDataList.size(),
							concurrentProcessLogDefinition.getProcessId());

					if (null != decorateOutputList && !decorateOutputList.isEmpty()) {

						updateColumnData(decorateOutputList, reportDataList, bucketName);
					}

					Accumulator accumulator = new Accumulator(countColumnValue, csvRowLimitValue);

					rowSize = reportDataList.size();

					while (null != reportDataList && !reportDataList.isEmpty()) {

						if (rowSize >= csvRowLimitValue) {

							List<Pair<Integer, List<Map<String, Object>>>> fullList = reportDataList.stream().collect(
									ArrayList<Pair<Integer, List<Map<String, Object>>>>::new, accumulator::accept,
									(x, y) -> {
									});

							for (Pair<Integer, List<Map<String, Object>>> childPair : fullList) {

								totalRecordsInProcess = totalRecordsInProcess + childPair.getRight().size();

								log.info(
										"ChildPair right size is {} for bucketName -> {}, processId -> {} and batchId -> {}",
										childPair.getRight().size(), bucketName,
										concurrentProcessLogDefinition.getProcessId(), batchId);

								String fullCSVFilePath = generateFullCSVFilePathWithBucket(manageDataAPI, csvPathParam,
										bucketName, counter, currDateTime);

								log.info(
										"Creating new CSV FileName with path running on {} at {} for bucketName -> {}, processId -> {} and batchId -> {}",
										fullCSVFilePath, currDateTime, bucketName,
										concurrentProcessLogDefinition.getProcessId(), batchId);

								FileUtil.writeCSV(childPair.getRight(), columnNameHeaderMap.values(), fullCSVFilePath,
										false, null);

								fileCount = fileCount + 1;
								log.info("Writing file to csvFilePath -> {} with header", fullCSVFilePath);
							}

							rowSize = 0;
							reportDataList.clear();
						}

						if (customEnvelopeDataIdResponse.getNextAvailable()) {

							pageNumber = pageNumber + 1;
							customEnvelopeDataIdResponse = customEnvelopeDataClient.findAllByDownloadBucketName(
									preparePageInformation(bucketName, pageNumber, getEnvelopesPaginationLimit()))
									.getBody();

							if (null != customEnvelopeDataIdResponse
									&& null != customEnvelopeDataIdResponse.getTotalRecords()
									&& customEnvelopeDataIdResponse.getTotalRecords() > 0) {

								inputParams.put(sqlParamList.get(0).getParamName(),
										customEnvelopeDataIdResponse.getEnvelopeIds());
								migrationReportDataDefinition.setInputParams(inputParams);
							}
							migrationReportDataInformation = migrationDataClient
									.readReportData(migrationReportDataDefinition).getBody();
						} else {

							migrationReportDataInformation = null;
						}

						if (null != migrationReportDataInformation && null != migrationReportDataInformation
								&& migrationReportDataInformation.getTotalRecords() > 0) {

							log.info(
									"More data fetched from more pages for bucketName -> {} in processId -> {} and batchId -> {}",
									bucketName, concurrentProcessLogDefinition.getProcessId(), batchId);

							if (rowSize == 0) {

								log.info(
										"Previous reportData written in csv so creating new reportdatalist for bucketName -> {} in processId -> {} and batchId -> {}",
										bucketName, concurrentProcessLogDefinition.getProcessId(), batchId);
								reportDataList = migrationReportDataInformation.getReportDataList();
								rowSize = reportDataList.size();
							} else {

								log.info(
										"Adding more to existing reportdatalist for bucketName -> {} in processId -> {} and batchId -> {}",
										bucketName, concurrentProcessLogDefinition.getProcessId(), batchId);
								reportDataList.addAll(migrationReportDataInformation.getReportDataList());

								rowSize = rowSize + reportDataList.size();
							}

							if (null != decorateOutputList && !decorateOutputList.isEmpty()) {

								updateColumnData(decorateOutputList, reportDataList, bucketName);
							}

						} else {

							if (null != reportDataList && !reportDataList.isEmpty() && rowSize > 0
									&& rowSize < csvRowLimitValue) {

								String fullCSVFilePath = generateFullCSVFilePathWithBucket(manageDataAPI, csvPathParam,
										bucketName, counter, currDateTime);

								log.info(
										"rowSize -> {} < csvRowLimitValue -> {} so creating last CSV FileName with path running on {} at {} for bucketName -> {}, processId -> {} and batchId -> {}",
										rowSize, csvRowLimitValue, fullCSVFilePath, currDateTime, bucketName,
										concurrentProcessLogDefinition.getProcessId(), batchId);

								totalRecordsInProcess = totalRecordsInProcess + reportDataList.size();
								FileUtil.writeCSV(reportDataList, columnNameHeaderMap.values(), fullCSVFilePath, false,
										null);

								log.info("Writing file to csvFilePath -> {} with header", fullCSVFilePath);

								fileCount = fileCount + 1;
								reportDataList = null;
							}

						}

					}
				}
			}

			coreBatchDataService.closeConcurrentProcess(batchId, concurrentProcessLogDefinition.getProcessId(),
					ProcessStatus.COMPLETED.toString(), Long.valueOf(totalRecordsInProcess));
		}

		return fileCount;
	}

	private String generateFullCSVFilePathWithBucket(ManageDataAPI manageDataAPI, PathParam csvPathParam,
			String bucketName, AtomicInteger counter, String currDateTime) {

		ReportAppUtil.createDirectoryNIO(
				csvPathParam.getParamValue() + File.separator + bucketName + File.separator + "manifest");

		String fullCSVFilePath = csvPathParam.getParamValue() + File.separator + bucketName + File.separator
				+ "manifest" + File.separator + manageDataAPI.getTableName()
				+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + currDateTime
				+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + counter.getAndIncrement() + ".csv";
		return fullCSVFilePath;
	}

	private PathParam extractCSVFilePath(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList, AppConstants.CSV_DOWNLOAD_FOLDER_PATH);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			log.info("csvFilePathParam param is missing or is null");
			return null;

		}

		log.info("CSV Directory Path will be {}", csvFilePathParam.getParamValue());
		return csvFilePathParam;
	}

	private Integer checkCSVRowLimit(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.CSV_DOWNLOAD_ROWS_LIMT_PER_FILE);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			log.info(AppConstants.CSV_DOWNLOAD_ROWS_LIMT_PER_FILE + " param value is missing or is null");
			return null;

		} else {

			return Integer.valueOf(csvFilePathParam.getParamValue());
		}

	}

	private static List<Map<String, Object>> updateColumnData(List<DecorateOutput> decorateOutputList,
			List<Map<String, Object>> rowList, String bucketName) {

		if (null != decorateOutputList && !decorateOutputList.isEmpty() && null != rowList && !rowList.isEmpty()) {

			List<DecorateOutput> decorateOutputFilterList = decorateOutputList.stream().filter(
					output -> DecorateOutputType.REPLACECOLVALUE.toString().equalsIgnoreCase(output.getOutputType()))
					.collect(Collectors.toList());

			if (null != decorateOutputFilterList && !decorateOutputFilterList.isEmpty()) {

				for (DecorateOutput decorateOutput : decorateOutputFilterList) {

					log.info("For decorateOutput Col Name -> {}, rowList size before listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
					ListIterator<Map<String, Object>> rowListIterator = rowList.listIterator();
					while (rowListIterator.hasNext()) {

						Map<String, Object> rowColKeyValueMap = rowListIterator.next();

						rowColKeyValueMap.forEach((columnName, dbValue) -> {

							if (columnName.equalsIgnoreCase(decorateOutput.getDbColumnName()) && null != dbValue) {

								String columnValue = dbValue.toString();

								log.debug("Updating value -> {} in rowColKeyValueMap", columnValue);
								rowColKeyValueMap.put(columnName,
										columnValue.replace(decorateOutput.getParamValue(), bucketName));
							}
						});

					}

					log.info("For decorateOutput Col Name -> {}, rowList size after listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
				}

			}
		}

		return rowList;
	}

}