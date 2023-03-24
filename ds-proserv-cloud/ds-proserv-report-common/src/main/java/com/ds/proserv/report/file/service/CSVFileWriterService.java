package com.ds.proserv.report.file.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.AsyncInterruptedException;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceConditionFailedException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.ConcurrentDocDownloadDataMessageDefinition;
import com.ds.proserv.feign.report.domain.DownloadDataMessage;
import com.ds.proserv.feign.report.domain.DownloadDocs;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.feign.util.FileUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.ds.proserv.report.validator.CSVFileWriterValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CSVFileWriterService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	@Autowired
	private FileWriterService fileWriterService;

	@Autowired
	private ScriptEngineManager scriptEngineManager;

	@Autowired
	private CSVFileWriterValidator csvFileWriterValidator;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	public Map<String, String> createHeaderList(TableColumnMetaData tableColumnMetaData, String selectSql) {

		log.info("Creating headers for the CSV for selectSql -> {}", selectSql);

		List<String> selectColumnList = extractSelectColumnList(tableColumnMetaData, selectSql);

		log.info("selectColumnList is {}", selectColumnList);
		if (null == selectColumnList || selectColumnList.isEmpty()) {

			throw new ResourceConditionFailedException(
					"Columns list cannot be empty or null, please check the select query");
		}

		Map<String, String> columnNameHeaderMap = new LinkedHashMap<String, String>();

		selectColumnList.forEach(column -> {

			try {

				String keyValue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(column,
						PropertyCacheConstants.CSV_COLUMN_HEADER_REFERENCE);
				columnNameHeaderMap.put(column, keyValue);

			} catch (ResourceNotFoundException exp) {

				log.warn("No cache value exists for key (columnName) -> {}", column);

				try {
					String keyValue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(column.toUpperCase(),
							PropertyCacheConstants.CSV_COLUMN_HEADER_REFERENCE);
					columnNameHeaderMap.put(column.toLowerCase(), keyValue);
				} catch (ResourceNotFoundException upperExp) {

					log.warn("No cache value exists for key (columnName) with uppercase -> {}", column);
				}
			}

		});

		return columnNameHeaderMap;

	}

	private List<String> extractSelectColumnList(TableColumnMetaData tableColumnMetaData, String selectSql) {

		selectSql = selectSql.trim().replaceAll("\\s{2,}", " ").toLowerCase();
		if (selectSql.indexOf("select ") == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after select");
		}

		if (selectSql.indexOf(" from " + tableColumnMetaData.getTableName()) == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after before and after from");
		}

		String[] selectColumns = selectSql.split("select ");
		String sqlColumns = selectColumns[1].split(" from " + tableColumnMetaData.getTableName())[0];

		return Stream.of(sqlColumns.split(AppConstants.COMMA_DELIMITER)).map(String::trim).collect(Collectors.toList());
	}

	public void writeEnvelopeDocumentsToDisk(List<Map<String, Object>> envelopeDataList, String downloadFolderPath,
			DownloadDocs downloadDocs, String batchId, ManageDataAPI csvReportDataExport, String batchType)
			throws IOException {

		Path parentDirectory = Paths.get(downloadFolderPath);
		log.info("ParentDirectory in writeEnvelopeDocuments is {}", parentDirectory.toString());

		List<PathParam> downloadParams = downloadDocs.getDownloadParams();

		csvFileWriterValidator.validateDownloadDocs(downloadDocs, downloadParams);

		String fileSaveFormat = ReportAppUtil.findPathParam(downloadParams, AppConstants.FILE_SAVE_FORMAT)
				.getParamValue();

		APICategoryType apiCategoryType = ReportAppUtil
				.getAPICategoryType(downloadDocs.getAssociatedData().getApiCategory());

		if (apiCategoryType == APICategoryType.ESIGNAPI) {

			csvFileWriterValidator.validateFileSaveFormatEnum(fileSaveFormat);

			csvFileWriterValidator.validateFileSaveFormatWithAPIUri(fileSaveFormat,
					downloadDocs.getAssociatedData().getApiUri());
		}

		processReportEnvelopeDataToWriteFiles(envelopeDataList, downloadDocs, batchId, parentDirectory, downloadParams,
				fileSaveFormat, csvReportDataExport, batchType);

	}

	private void processReportEnvelopeDataToWriteFiles(List<Map<String, Object>> envelopeDataList,
			DownloadDocs downloadDocs, String batchId, Path parentDirectory, List<PathParam> downloadParams,
			String fileSaveFormat, ManageDataAPI csvReportDataExport, String batchType) {

		ScriptEngine engine = scriptEngineManager.getEngineByName(AppConstants.SCRIPT_ENGINE_NAME);

		String downloadFileName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FILE_NAME);
		String downloadFolderName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FOLDER_NAME);

		log.info("downloadFileName is {}, downloadFolderName is {} and downloadParams are {}", downloadFileName,
				downloadFolderName, downloadParams);

		// Below code partitions all envelopeDataList into chunks with each chunk having
		// totalRowsPerProcess records
		// Below code to test first
		final AtomicReference<String> groupbyString = new AtomicReference<String>();
		List<CompletableFuture<ConcurrentProcessLogDefinition>> reportEnvelopeDataList = new ArrayList<CompletableFuture<ConcurrentProcessLogDefinition>>();

		if (!StringUtils.isEmpty(csvReportDataExport.getGroupByColumn())) {

			groupbyString.set(csvReportDataExport.getGroupByColumn());
		} else if (!StringUtils.isEmpty(csvReportDataExport.getOrderByClause())) {

			String orderByClause = csvReportDataExport.getOrderByClause().toLowerCase();
			groupbyString.set(orderByClause.split(" ")[2].split(",")[0].trim());
		}

		if (!StringUtils.isEmpty(groupbyString.get())) {

			log.info(
					"About to group the data set by {}, it might take some time based on the records return from the DB",
					groupbyString.get());
			Collection<List<Map<String, Object>>> groupByString = envelopeDataList.stream()
					.collect(Collectors.groupingBy(it -> it.get(groupbyString.get()))).values();

			log.info("groupByString size is {}", groupByString.size());
			log.debug("groupByString is {}", groupByString);

			partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(downloadDocs, batchId, parentDirectory,
					downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName,
					reportEnvelopeDataList, groupByString, batchType);

		} else {// Group first by accountid, then create subset of each grouped data by
				// totalRowsPerProcess

			log.info(
					"About to group the data set by accountid, it might take some time based on the records return from the DB");
			Collection<List<Map<String, Object>>> groupByAccountId = envelopeDataList.stream()
					.collect(Collectors.groupingBy(it -> it.get("accountid"))).values();

			log.info("groupByAccountId size is {}", groupByAccountId.size());
			log.debug("groupByAccountId is {}", groupByAccountId);

			partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(downloadDocs, batchId, parentDirectory,
					downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName,
					reportEnvelopeDataList, groupByAccountId, batchType);

		}

		log.info("Size of reportDataFutureAccountList is {}", reportEnvelopeDataList.size());
		try {

			CompletableFuture
					.allOf(reportEnvelopeDataList.toArray(new CompletableFuture[reportEnvelopeDataList.size()])).get();
		} catch (InterruptedException exp) {

			log.error(
					"InterruptedException {} occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId {}",
					exp, batchId);

			exp.printStackTrace();
			throw new AsyncInterruptedException("InterruptedException " + exp
					+ " occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId " + batchId
					+ " message " + exp.getMessage());
		} catch (ExecutionException exp) {

			log.error(
					"ExecutionException {} occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId {} and the cause is {}",
					exp, batchId, exp);
			exp.printStackTrace();

			throw new AsyncInterruptedException("ExecutionException " + exp
					+ " occurred in CSVFileWriterService.processReportEnvelopeDataToWriteFiles for batchId " + batchId
					+ " message " + exp.getMessage() + " cause is " + exp);
		}
	}

	private void partitionGroupByAccountIntoTotalRowsPerProcessAndProcessData(DownloadDocs downloadDocs, String batchId,
			Path parentDirectory, List<PathParam> downloadParams, String fileSaveFormat, ScriptEngine engine,
			String downloadFileName, String downloadFolderName,
			List<CompletableFuture<ConcurrentProcessLogDefinition>> reportEnvelopeDataList,
			Collection<List<Map<String, Object>>> groupByAccountId, String batchType) {

		Integer totalRowsPerProcess = Integer.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.REPORT_TOTALROWSPERPROCESS));

		log.info(
				"About to partition each group by totalRowsPerProcess -> {}, it might take some time based on the record return from the DB",
				totalRowsPerProcess);

		groupByAccountId.forEach(group -> {

			final AtomicInteger groupByCounter = new AtomicInteger(0);
			Collection<List<Map<String, Object>>> groupByTotalRows = group.stream()
					.collect(Collectors.groupingBy(it -> groupByCounter.getAndIncrement() / totalRowsPerProcess))
					.values();

			log.info("groupByTotalRows size is {}", groupByTotalRows.size());
			log.debug("groupByTotalRows -> {}", groupByTotalRows);

			groupByTotalRows.forEach(partitionedSet -> {

				reportEnvelopeDataList.add(processRowDataAsync(partitionedSet, downloadDocs, batchId, parentDirectory,
						downloadParams, fileSaveFormat, engine, downloadFileName, downloadFolderName, batchType));

			});

		});
	}

	private String extractPathParamValue(List<PathParam> downloadParams, String paramName) {

		PathParam pathParam = ReportAppUtil.findPathParam(downloadParams, paramName);
		if (null != pathParam) {
			return pathParam.getParamValue();
		}

		return null;
	}

	private CompletableFuture<ConcurrentProcessLogDefinition> processRowDataAsync(
			List<Map<String, Object>> envelopeDataList, DownloadDocs downloadDocs, String batchId, Path parentDirectory,
			List<PathParam> downloadParams, String fileSaveFormat, ScriptEngine engine, String downloadFileName,
			String downloadFolderName, String batchType) {

		return CompletableFuture.supplyAsync((Supplier<ConcurrentProcessLogDefinition>) () -> {

			log.info("accountId -> {}, totalListSize -> {}", (String) envelopeDataList.get(0).get("accountid"),
					envelopeDataList.size());
			ConcurrentProcessLogDefinition concurrentProcessLogDefinitionGroup = batchDataService
					.createConcurrentProcess(Long.valueOf(envelopeDataList.size()), batchId,
							batchType + "_DOWNLOADGROUP", (String) envelopeDataList.get(0).get("accountid"),
							dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_USERID));

			String docDownloadQueueLimit = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_PERQUEUE_LIMIT);
			String docDownloadByQueue = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_BYQUEUE);

			log.info("docDownloadQueueLimit is {} and docDownloadByQueue flag value is {}", docDownloadQueueLimit,
					docDownloadByQueue);
			if (!StringUtils.isEmpty(docDownloadQueueLimit) && !StringUtils.isEmpty(docDownloadByQueue)
					&& AppConstants.APP_TRUE.equalsIgnoreCase(docDownloadByQueue)) {

				int perQueueLimit = Integer.parseInt(docDownloadQueueLimit);

				final AtomicInteger groupByCounter = new AtomicInteger(0);
				Collection<List<Map<String, Object>>> groupByQueueLimit = envelopeDataList.stream()
						.collect(Collectors.groupingBy(it -> groupByCounter.getAndIncrement() / perQueueLimit))
						.values();

				groupByQueueLimit.forEach(envelopeDataQueueColl -> {

					if (null != envelopeDataQueueColl) {

						List<DownloadDataMessage> downloadDataMessages = new ArrayList<DownloadDataMessage>();

						try {

							for (Map<String, Object> envelopeDataMap : envelopeDataQueueColl) {

								String fileName = null;
								String folderName = null;
								Map<String, Object> inputParams = new HashMap<String, Object>();

								FileUtil.populateInputParams(envelopeDataMap, inputParams, downloadDocs);

								fileName = FileUtil.evaluateFileName(downloadParams, engine, downloadFileName,
										envelopeDataMap, fileName);

								folderName = FileUtil.evaluateFileFolderName(downloadParams, engine, downloadFolderName,
										envelopeDataMap, folderName);

								DownloadDataMessage downloadDataMessage = FileUtil.createDownloadDocMessage(
										downloadDocs, parentDirectory, fileSaveFormat, fileName, folderName,
										inputParams);

								downloadDataMessages.add(downloadDataMessage);

							}

							ConcurrentProcessLogDefinition concurrentProcessLogDefinitionLocal = batchDataService
									.createConcurrentProcess(Long.valueOf(downloadDataMessages.size()), batchId,
											concurrentProcessLogDefinitionGroup.getProcessId(),
											(String) envelopeDataQueueColl.get(0).get("accountid"),
											dsCacheManager.prepareAndRequestCacheDataByKey(
													PropertyCacheConstants.DSAUTH_USERID));

							if (null != downloadDataMessages && !downloadDataMessages.isEmpty()) {

								callDocDownloadQueue(downloadDataMessages,
										concurrentProcessLogDefinitionLocal.getProcessId(), batchId,
										concurrentProcessLogDefinitionGroup.getProcessId(), downloadDocs);
							} else {

								log.warn("no message to be sent for download for processId -> {} so closing processId",
										concurrentProcessLogDefinitionLocal.getProcessId());
								batchDataService.finishConcurrentProcessWithNoRecords(
										concurrentProcessLogDefinitionLocal.getProcessId());
							}

						} catch (Exception exp) {

							handleException(batchId, (String) envelopeDataQueueColl.get(0).get("accountid"), exp);

						}
					}
				});

			} else {

				for (Map<String, Object> envelopeDataMap : envelopeDataList) {

					try {

						String fileName = null;
						String folderName = null;
						Map<String, Object> inputParams = new HashMap<String, Object>();

						FileUtil.populateInputParams(envelopeDataMap, inputParams, downloadDocs);

						fileName = FileUtil.evaluateFileName(downloadParams, engine, downloadFileName, envelopeDataMap,
								fileName);

						folderName = FileUtil.evaluateFileFolderName(downloadParams, engine, downloadFolderName,
								envelopeDataMap, folderName);

						if (StringUtils.isEmpty(docDownloadByQueue)
								|| AppConstants.APP_FALSE.equalsIgnoreCase(docDownloadByQueue)) {

							fileWriterService.pullDocumentAndWriteToDirectory(downloadDocs, batchId, parentDirectory,
									fileSaveFormat, fileName, folderName, inputParams,
									concurrentProcessLogDefinitionGroup.getProcessId());
						} else {

							ConcurrentProcessLogDefinition concurrentProcessLogDefinitionLocal = batchDataService
									.createConcurrentProcess(1L, batchId,
											concurrentProcessLogDefinitionGroup.getProcessId(),
											(String) inputParams.get("accountid"),
											dsCacheManager.prepareAndRequestCacheDataByKey(
													PropertyCacheConstants.DSAUTH_USERID));

							DownloadDataMessage downloadDataMessage = FileUtil.createDownloadDocMessage(downloadDocs,
									parentDirectory, fileSaveFormat, fileName, folderName, inputParams);

							List<DownloadDataMessage> downloadDataMessages = new ArrayList<DownloadDataMessage>();
							downloadDataMessages.add(downloadDataMessage);

							callDocDownloadQueue(downloadDataMessages,
									concurrentProcessLogDefinitionLocal.getProcessId(), batchId,
									concurrentProcessLogDefinitionGroup.getProcessId(), downloadDocs);
						}

					} catch (Exception exp) {

						handleException(batchId, envelopeDataMap, exp);

					}
				}
			}

			return concurrentProcessLogDefinitionGroup;
		}, recordTaskExecutor).handle((concurrentProcessLogDefinition, exp) -> {

			if (null != exp) {

				log.error(
						" ~~~~~~~~~~~~~~~~~~~~~~~~~ Inside csvFileWriteService.handleAsync and exp is {} for batchId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
						exp, batchId);

				exp.printStackTrace();

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend((String) envelopeDataList.get(0).get("accountid"), batchId,
							AppConstants.NOT_AVAILABLE_CONST, exp, FailureCode.ERROR_107,
							FailureStep.PROCESSROWDATAASYNC);

				} else {

					batchDataService.createFailureRecord((String) envelopeDataList.get(0).get("accountid"), batchId,
							FailureCode.ERROR_107.toString(), exp.getMessage(),
							FailureStep.PROCESSROWDATAASYNC.toString(), exp, AppConstants.NOT_AVAILABLE_CONST);
				}

			} else {

				String docDownloadByQueue = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_BYQUEUE);
				if (StringUtils.isEmpty(docDownloadByQueue)
						|| AppConstants.APP_FALSE.equalsIgnoreCase(docDownloadByQueue)) {

					batchDataService.finishConcurrentProcess(concurrentProcessLogDefinition.getProcessId());
				}
			}
			return concurrentProcessLogDefinition;
		});

	}

	private void handleException(String batchId, String accountId, Exception exp) {

		exp.printStackTrace();
		log.error("Exception {} occurred with message {} for accountId {}", exp, exp.getMessage(), accountId + "_loop");

		if (!StringUtils.isEmpty(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

			queueService.createFailureMessageAndSend(accountId + "_loop", batchId, AppConstants.NOT_AVAILABLE_CONST,
					exp, FailureCode.ERROR_107, FailureStep.PROCESSROWDATAFORASYNC);

		} else {

			batchDataService.createFailureRecord(accountId + "_loop", batchId, FailureCode.ERROR_107.toString(),
					exp.getMessage(), FailureStep.PROCESSROWDATAFORASYNC.toString(), exp,
					AppConstants.NOT_AVAILABLE_CONST);
		}
	}

	private void handleException(String batchId, Map<String, Object> envelopeDataMap, Exception exp) {

		exp.printStackTrace();
		log.error("Exception {} occurred with message {} for envelopeId {} and accountId {}", exp, exp.getMessage(),
				envelopeDataMap.get("envelopeid"), envelopeDataMap.get("accountid"));

		if (!StringUtils.isEmpty(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

			queueService.createFailureMessageAndSend(
					envelopeDataMap.get("accountid") + "_" + envelopeDataMap.get("envelopeid"), batchId,
					AppConstants.NOT_AVAILABLE_CONST, exp, FailureCode.ERROR_107, FailureStep.PROCESSROWDATAFORASYNC);

		} else {

			batchDataService.createFailureRecord(
					envelopeDataMap.get("accountid") + "_" + envelopeDataMap.get("envelopeid"), batchId,
					FailureCode.ERROR_107.toString(), exp.getMessage(), FailureStep.PROCESSROWDATAFORASYNC.toString(),
					exp, AppConstants.NOT_AVAILABLE_CONST);
		}
	}

	private void callDocDownloadQueue(List<DownloadDataMessage> downloadDataMessages, String processId, String batchId,
			String groupId, DownloadDocs downloadDocs) {

		ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition = new ConcurrentDocDownloadDataMessageDefinition();
		concurrentDocDownloadDataMessageDefinition.setBatchId(batchId);
		concurrentDocDownloadDataMessageDefinition.setProcessId(processId);
		concurrentDocDownloadDataMessageDefinition.setGroupId(groupId);
		concurrentDocDownloadDataMessageDefinition.setDownloadDocs(downloadDocs);
		concurrentDocDownloadDataMessageDefinition.setDownloadDataMessages(downloadDataMessages);

		queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_NAME, processId, batchId,
				concurrentDocDownloadDataMessageDefinition);
	}

}