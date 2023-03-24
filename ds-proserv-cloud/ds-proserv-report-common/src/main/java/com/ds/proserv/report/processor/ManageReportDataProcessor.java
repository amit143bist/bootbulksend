package com.ds.proserv.report.processor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DocDownloadDestination;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.domain.Accumulator;
import com.ds.proserv.feign.domain.Pair;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.feign.util.FileUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.db.service.ReportJDBCService;
import com.ds.proserv.report.file.service.CSVFileWriterService;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.ds.proserv.report.service.ReportRunParamService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManageReportDataProcessor {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	@Lazy
	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private CSVFileWriterService csvFileWriterService;

	@Autowired
	private ReportRunParamService prepareReportDataService;

	@SuppressWarnings("unchecked")
	public void callAPIWithInputParams(PrepareReportDefinition prepareReportDefinition, HttpServletResponse response,
			Map<String, String> headers) throws Exception {

		for (ManageDataAPI manageDataAPI : prepareReportDefinition.getManageDataAPIs()) {

			String batchId = null;
			boolean writeToStream = false;

			try {

				String batchType = manageDataAPI.getExportRunArgs().getBatchType();

				Map<String, Object> inputParams = prepareReportDataService
						.prepareInputParams(manageDataAPI.getExportRunArgs());

				batchId = createBatch(inputParams, batchType);

				String csvFilePath = findCSVFilePath(manageDataAPI.getExportRunArgs().getPathParams());

				if (!StringUtils.isEmpty(csvFilePath)) {

					if (null != headers && null != headers.get("accept") && null != response) {

						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING CSVGENERATION IN ONLINE MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");

						writeToStream = true;
						response.setContentType("text/csv");
						// creates mock data
						String headerKey = "Content-Disposition";
						String headerValue = String.format("attachment; filename=\"%s\"",
								csvFilePath.substring(csvFilePath.lastIndexOf(File.separator) + 1));
						response.setHeader(headerKey, headerValue);
					} else {

						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING CSVGENERATION IN BATCH MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
						log.info(
								" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
					}

					TableColumnMetaData tableColumnMetaData = reportJDBCService
							.getTableColumns(manageDataAPI.getTableName());

					Map<String, String> columnNameHeaderMap = csvFileWriterService.createHeaderList(tableColumnMetaData,
							manageDataAPI.getSelectSql());

					Map<String, String> deepCopyColumnNameHeaderMap = (Map<String, String>) SerializationUtils
							.deserialize(SerializationUtils.serialize(columnNameHeaderMap));

					if (log.isDebugEnabled()) {

						log.debug("Original columnNameHeaderMap is {}", columnNameHeaderMap);
						log.debug("deepCopyColumnNameHeaderMap is {}", deepCopyColumnNameHeaderMap);
					}

					Integer pageNumber = 1;
					Integer paginationLimit = paginationLimit(manageDataAPI.getExportRunArgs().getPathParams());

					List<Map<String, Object>> rowList = reportJDBCService.readReportData(columnNameHeaderMap,
							deepCopyColumnNameHeaderMap, inputParams, manageDataAPI, pageNumber, paginationLimit);

					int rowSize = 0;
					ConcurrentProcessLogDefinition concurrentProcessLogDefinition = null;
					if (null != rowList) {

						rowSize = rowList.size();
						log.info("Fetched rowData of size {} for pageNumber -> {} and paginationLimit -> {}", rowSize,
								pageNumber, paginationLimit);
						concurrentProcessLogDefinition = batchDataService.createConcurrentProcess(Long.valueOf(rowSize),
								batchId, batchType + "CSVGROUP", null,
								dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_USERID));
					}

					Integer csvRowLimitValue = checkCSVRowLimit(manageDataAPI.getExportRunArgs().getPathParams());

					AtomicInteger counter = new AtomicInteger(1);
					PathParam countColumnNamePathParam = ReportAppUtil.findPathParam(
							manageDataAPI.getExportRunArgs().getPathParams(), AppConstants.CSV_FETCH_COUNT_COLUMN_NAME);

					String countColumnValue = null;
					if (null != countColumnNamePathParam) {

						countColumnValue = countColumnNamePathParam.getParamValue();
					}
					log.info("CountColumnValue is {} and csvRowLimitValue is {}", countColumnValue, csvRowLimitValue);

					Accumulator accumulator = new Accumulator(countColumnValue, csvRowLimitValue);
					writeToCSVsByRowLimit(response, manageDataAPI, batchId, writeToStream, inputParams, csvFilePath,
							columnNameHeaderMap, pageNumber, paginationLimit, rowList, rowSize, counter,
							csvRowLimitValue, accumulator, deepCopyColumnNameHeaderMap);

					if (null != concurrentProcessLogDefinition
							&& !StringUtils.isEmpty(concurrentProcessLogDefinition.getProcessId())) {

						batchDataService.finishConcurrentProcess(concurrentProcessLogDefinition.getProcessId());
					}
				} else {

					log.warn("-------------------- CSV FilePath is NULL --------------------");
				}

				prepareDataForDocDownload(manageDataAPI, batchId, batchType, inputParams);

				String docDownloadByQueue = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_BYQUEUE);
				if (StringUtils.isEmpty(docDownloadByQueue)
						|| AppConstants.APP_FALSE.equalsIgnoreCase(docDownloadByQueue)) {

					if (batchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

						batchDataService.finishBatchProcessData(manageDataAPI.getExportRunArgs(),
								manageDataAPI.getExportRunArgs().getBatchType(), batchId);
					} else {

						log.warn("No worker threads available for batchId -> {}, so closing the batch", batchId);
						batchDataService.finishBatchProcess(batchId, 0L);
					}
				} else {

					if (!batchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

						log.warn("No worker threads available when Queue is ON for batchId -> {}, so closing the batch",
								batchId);
						batchDataService.finishBatchProcess(batchId, 0L);
					} else {

						log.info(
								"######################### All Messages queued in manage flow for batchId -> {} #########################",
								batchId);
					}

				}

			} catch (Exception exp) {

				exp.printStackTrace();
				log.info(
						" ------------------------------ CSVGENERATION batch with batchId -> {} NOT Completed successfully ------------------------------ ",
						batchId);
				log.error("Exception {} occurred in writing to a CSV with message {}", exp, exp.getMessage());

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend("WriteCSV", batchId, AppConstants.NOT_AVAILABLE_CONST, exp,
							FailureCode.ERROR_107, FailureStep.COPYING_DB_DATA_TO_CSV);

				} else {

					batchDataService.createFailureRecord("WriteCSV", batchId, FailureCode.ERROR_107.toString(),
							exp.getMessage(), FailureStep.COPYING_DB_DATA_TO_CSV.toString(), exp,
							AppConstants.NOT_AVAILABLE_CONST);
				}

				throw exp;
			}

		}

	}

	private void writeToCSVsByRowLimit(HttpServletResponse response, ManageDataAPI manageDataAPI, String batchId,
			boolean writeToStream, Map<String, Object> inputParams, String csvFilePath,
			Map<String, String> columnNameHeaderMap, Integer pageNumber, Integer paginationLimit,
			List<Map<String, Object>> rowList, int rowSize, AtomicInteger counter, Integer csvRowLimitValue,
			Accumulator accumulator, Map<String, String> deepCopyColumnNameHeaderMap) throws IOException {

		while (null != rowList && !rowList.isEmpty()) {

			if (rowSize >= csvRowLimitValue) {

				log.info("Splitting and writing to csvs");
				writeToMultipleCSVs(csvFilePath, csvRowLimitValue, rowList, columnNameHeaderMap.values(), writeToStream,
						response, batchId, manageDataAPI.getTableName(), counter, accumulator);

				rowSize = 0;
				rowList.clear();
			}

			pageNumber = pageNumber + 1;
			List<Map<String, Object>> moreRowList = reportJDBCService.readReportData(columnNameHeaderMap,
					deepCopyColumnNameHeaderMap, inputParams, manageDataAPI, pageNumber, paginationLimit);

			if (null != moreRowList && !moreRowList.isEmpty()) {

				if (rowSize == 0) {

					rowList = moreRowList;
					rowSize = moreRowList.size();
				} else {

					rowList.addAll(moreRowList);
					rowSize = rowSize + moreRowList.size();
				}
				log.info(
						"Fetched rowData of size {} for pageNumber -> {} and paginationLimit -> {}, and new size is {}",
						moreRowList.size(), pageNumber, paginationLimit, rowSize);
			} else {

				if (null != rowList && !rowList.isEmpty() && rowSize > 0 && rowSize < csvRowLimitValue) {

					log.info(
							"No new rowData for pageNumber -> {} and paginationLimit -> {}, and old size is {}, hence writing to a csv file",
							pageNumber, paginationLimit, rowSize);

					String currDateTime = LocalDateTime.now()
							.format(DateTimeFormatter.ofPattern(DateTimeUtil.FILE_DATE_PATTERN));

					String fullCSVFilePath = csvFilePath + File.separator + manageDataAPI.getTableName()
							+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + currDateTime
							+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + counter.getAndIncrement() + ".csv";

					FileUtil.writeCSV(rowList, columnNameHeaderMap.values(), fullCSVFilePath, writeToStream, response);

					rowList = null;
				}
			}

		}
	}

	private Integer paginationLimit(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.REPORT_DATA_PAGINATION_LIMIT);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			return 10000;

		} else {

			return Integer.valueOf(csvFilePathParam.getParamValue());
		}

	}

	private void prepareDataForDocDownload(ManageDataAPI manageDataAPI, String batchId, String batchType,
			Map<String, Object> inputParams) throws IOException {

		if (null != manageDataAPI.getDownloadDocs() && null != manageDataAPI.getDownloadDocs().getAssociatedData()
				&& null != manageDataAPI.getDownloadDocs().getDownloadParams()) {

			String downloadFolderPath = findDownloadFolderPath(manageDataAPI.getDownloadDocs().getDownloadParams());

			String downloadDocDestination = findDownloadDocDestination(
					manageDataAPI.getDownloadDocs().getDownloadParams());

			if (!StringUtils.isEmpty(downloadFolderPath) && !StringUtils.isEmpty(downloadDocDestination)
					&& DocDownloadDestination.DISK.toString().equalsIgnoreCase(downloadDocDestination)) {

				log.info(
						" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ Write envelope PDFs in batch mode only $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");

				Integer pageNumber = 1;
				Integer paginationLimit = paginationLimit(manageDataAPI.getExportRunArgs().getPathParams());

				List<Map<String, Object>> envelopeDataList = reportJDBCService
						.readEnvelopeDataForDownload(manageDataAPI, inputParams, pageNumber, paginationLimit);

				if (null != envelopeDataList) {

					log.info(
							"Fetched envelopeDataList for doc download of size {} for pageNumber -> {} and paginationLimit -> {}",
							envelopeDataList.size(), pageNumber, paginationLimit);
				}

				while (null != envelopeDataList && !envelopeDataList.isEmpty()) {

					csvFileWriterService.writeEnvelopeDocumentsToDisk(envelopeDataList, downloadFolderPath,
							manageDataAPI.getDownloadDocs(), batchId, manageDataAPI, batchType);

					pageNumber = pageNumber + 1;
					envelopeDataList = reportJDBCService.readEnvelopeDataForDownload(manageDataAPI, inputParams,
							pageNumber, paginationLimit);

					if (null != envelopeDataList) {

						log.info(
								"Fetched envelopeDataList for doc download of size {} for pageNumber -> {} and paginationLimit -> {}",
								envelopeDataList.size(), pageNumber, paginationLimit);
					}
				}
			}

		}
	}

	private void writeToMultipleCSVs(String csvFilePath, Integer rowLimitValue, List<Map<String, Object>> rowList,
			Collection<String> headerList, boolean writeToStream, HttpServletResponse response, String batchId,
			String tableName, AtomicInteger counter, Accumulator accumulator) {

		List<Pair<Integer, List<Map<String, Object>>>> fullList = rowList.stream()
				.collect(ArrayList<Pair<Integer, List<Map<String, Object>>>>::new, accumulator::accept, (x, y) -> {
				});

		String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateTimeUtil.FILE_DATE_PATTERN));
		fullList.forEach(childPair -> {

			String fullCSVFilePath = csvFilePath + File.separator + tableName
					+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + currDateTime
					+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + counter.getAndIncrement() + ".csv";

			log.info("CSV FileName with path running on {}-> {}", fullCSVFilePath, currDateTime);
			try {

				FileUtil.writeCSV(childPair.getRight(), headerList, fullCSVFilePath, writeToStream, response);
			} catch (IOException exp) {
				exp.printStackTrace();

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend("SplitAndWriteCSV", batchId,
							AppConstants.NOT_AVAILABLE_CONST, exp, FailureCode.ERROR_109,
							FailureStep.SPLITTING_WRITING_TO_CSV);

				} else {

					batchDataService.createFailureRecord("SplitAndWriteCSV", batchId, FailureCode.ERROR_109.toString(),
							exp.getMessage(), FailureStep.SPLITTING_WRITING_TO_CSV.toString(), exp,
							AppConstants.NOT_AVAILABLE_CONST);
				}
			}

		});

	}

	private Integer checkCSVRowLimit(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.CSV_DOWNLOAD_ROWS_LIMT_PER_FILE);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			log.info(AppConstants.CSV_DOWNLOAD_ROWS_LIMT_PER_FILE + " param value is missing or is null");
			return 100000;

		} else {

			return Integer.valueOf(csvFilePathParam.getParamValue());
		}

	}

	private String findDownloadDocDestination(List<PathParam> pathParamList) {// Sharepoint, Disk, CloudSaaS

		PathParam docDownloadDestinationPathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DOC_DOWNLOAD_DESTINATION);

		if (null == docDownloadDestinationPathParam
				|| (StringUtils.isEmpty(docDownloadDestinationPathParam.getParamValue()))) {

			log.info("docDownloadDestinationPathParam param is missing or is null");
			throw new InvalidInputException("docDownloadDestinationPathParam cannot be empty or null");

		}

		return docDownloadDestinationPathParam.getParamValue();
	}

	private String findCSVFilePath(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList, AppConstants.CSV_DOWNLOAD_FOLDER_PATH);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			log.info("csvFilePathParam param is missing or is null");
			return null;

		}

		log.info("CSV File Path is {}", csvFilePathParam.getParamValue());
		return modifyCSVPathV2(csvFilePathParam.getParamValue(), pathParamList);
	}

	private String modifyCSVPathV2(String csvPathDirectory, List<PathParam> pathParamList) {

		log.info("Modifying the CSV Folder Path V2");

		PathParam disableCsvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DISABLE_CURR_DATE_IN_CSV_FOLDER_PATH_FLAG);

		String fullCSVPath = null;
		if (null != disableCsvFilePathParam && "true".equalsIgnoreCase(disableCsvFilePathParam.getParamValue())) {

			ReportAppUtil.createDirectoryNIO(csvPathDirectory);
			fullCSVPath = csvPathDirectory;
		} else {

			String directoryPath = csvPathDirectory + File.separator
					+ DateTimeUtil.currentDateInString(TimeZone.getDefault().getID());

			ReportAppUtil.createDirectoryNIO(directoryPath);

			fullCSVPath = directoryPath;
		}

		return fullCSVPath;
	}

	PathParam extractCSVFilePath(List<PathParam> pathParamList) {

		PathParam csvFilePathParam = ReportAppUtil.findPathParam(pathParamList, AppConstants.CSV_DOWNLOAD_FOLDER_PATH);

		if (null == csvFilePathParam || (StringUtils.isEmpty(csvFilePathParam.getParamValue()))) {

			log.info("csvFilePathParam param is missing or is null");
			return null;

		}

		log.info("CSV File Path is {}", csvFilePathParam.getParamValue());
		return csvFilePathParam;
	}

	String modifyCSVPath(String csvPathDirectory, List<PathParam> pathParamList) {

		log.info("Modifying the CSV Folder Path");
		String startDate = DateTimeUtil
				.convertToLocalDateFromEpochTimeInSecs(DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
						ReportAppUtil.findPathParam(pathParamList, AppConstants.INPUT_FROM_DATE).getParamValue(),
						DateTimeUtil.DATE_TIME_PATTERN_NANO));
		String endDate = DateTimeUtil
				.convertToLocalDateFromEpochTimeInSecs(DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
						ReportAppUtil.findPathParam(pathParamList, AppConstants.INPUT_TO_DATE).getParamValue(),
						DateTimeUtil.DATE_TIME_PATTERN_NANO));

		PathParam disableCsvFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DISABLE_CURR_DATE_IN_CSV_FOLDER_PATH_FLAG);

		String fullCSVPath = null;
		if (null != disableCsvFilePathParam
				&& AppConstants.APP_TRUE.equalsIgnoreCase(disableCsvFilePathParam.getParamValue())) {

			fullCSVPath = csvPathDirectory + File.separator + startDate + "_" + endDate + "_"
					+ DateTimeUtil.currentEpochTime() + ".csv";
		} else {

			String directoryPath = csvPathDirectory + File.separator + DateTimeUtil.currentLocalDateInString();

			ReportAppUtil.createDirectory(directoryPath);

			fullCSVPath = directoryPath + File.separator + startDate + "_" + endDate + "_"
					+ DateTimeUtil.currentEpochTime() + ".csv";
		}

		return fullCSVPath;
	}

	private String findDownloadFolderPath(List<PathParam> pathParamList) {

		PathParam downloadFolderPathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DOC_DOWNLOAD_FOLDER_PATH);

		if (null == downloadFolderPathParam || (StringUtils.isEmpty(downloadFolderPathParam.getParamValue()))) {

			log.info("downloadFolderPathParam param is missing or is null");
			return null;

		}

		log.info("Download Folder Path is {}", downloadFolderPathParam.getParamValue());
		return modifyDownloadFolderPath(downloadFolderPathParam.getParamValue(), pathParamList);
	}

	private String modifyDownloadFolderPath(String downloadFolderPath, List<PathParam> pathParamList) {

		log.info("Modifying the Doc Downlod Folder Path");
		PathParam disableDocFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DISABLE_CURR_DATE_IN_DOC_FOLDER_PATH_FLAG);

		if (null != disableDocFilePathParam && "true".equalsIgnoreCase(disableDocFilePathParam.getParamValue())) {

			return downloadFolderPath;
		} else {

			String directoryPath = downloadFolderPath + File.separator + DateTimeUtil.currentLocalDateInString();

			ReportAppUtil.createDirectory(directoryPath);

			return directoryPath;
		}
	}

	private String createBatch(Map<String, Object> inputParams, String batchType) {

		BatchStartParams batchStartParams = new BatchStartParams();

		if (null != inputParams) {

			if (null != inputParams.get(AppConstants.INPUT_FROM_DATE)) {

				batchStartParams.setBeginDateTime((String) inputParams.get(AppConstants.INPUT_FROM_DATE));
			}

			if (null != inputParams.get(AppConstants.INPUT_TO_DATE)) {

				batchStartParams.setEndDateTime((String) inputParams.get(AppConstants.INPUT_TO_DATE));
			}

		}

		return batchDataService.createBatchJob(batchType, batchStartParams, null);
	}

}