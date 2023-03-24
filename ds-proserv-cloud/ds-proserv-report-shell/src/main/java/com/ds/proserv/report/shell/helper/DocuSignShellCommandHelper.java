package com.ds.proserv.report.shell.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.Shell;
import org.springframework.stereotype.Component;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.cipher.AESCipher;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.constant.QueryOption;
import com.ds.proserv.common.constant.QueryType;
import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.service.CoreCacheDataLogService;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogsInformation;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogsInformation;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogsInformation;
import com.ds.proserv.feign.coredata.service.CoreConcurrentProcessLogService;
import com.ds.proserv.feign.coredata.service.CoreProcessFailureLogService;
import com.ds.proserv.feign.coredata.service.CoreScheduledBatchLogService;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.BatchTriggerInformation;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.report.db.service.ReportJDBCService;
import com.ds.proserv.report.shell.plugin.DocuSignInputReader;
import com.ds.proserv.report.shell.plugin.DocuSignOutputDecorator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocuSignShellCommandHelper {

	@Autowired
	private Shell shell;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocuSignShellHelper shellHelper;

	@Autowired
	private DocuSignInputReader inputReader;

	@Lazy
	@Autowired
	private ReportJDBCService reportJDBCService;

	@Autowired
	private DocuSignOutputDecorator docuSignOutputDecorator;

	@Autowired
	private CoreCacheDataLogService coreCacheDataLogService;

	@Autowired
	private CoreScheduledBatchLogService coreScheduledBatchLogService;

	@Autowired
	private CoreProcessFailureLogService coreProcessFailureLogService;

	@Autowired
	private CoreConcurrentProcessLogService coreConcurrentProcessLogService;

	public String captureDownloadDirectoryPath() {
		String downloadDirectory;
		// 3. read Batch's CSV Folder Path --------------------------------------------
		boolean csvFolderPathCaptured = false;
		do {

			downloadDirectory = inputReader.prompt("Exisiting Folder Path to export the CSV");

			try {
				if (StringUtils.isEmpty(downloadDirectory)) {

					throw new InvalidInputException("downloadDirectory cannot be empty");
				}

				File file = new File(downloadDirectory);
				if (!file.exists()) {

					throw new InvalidInputException(downloadDirectory
							+ " is not a valid folder path, try escaping '\\' with two '\\\\', for instance valid path is C:\\\\dir");
				}
				csvFolderPathCaptured = true;
			} catch (InvalidInputException exp) {

				shellHelper.printError(exp.getMessage());
			}
		} while (!csvFolderPathCaptured);
		return downloadDirectory;
	}

	public String captureValidateDate(String dateType) {

		String batchDateTime = null;
		boolean dateCaptured = false;
		do {

			batchDateTime = inputReader.prompt(dateType);

			try {
				if (StringUtils.isEmpty(batchDateTime)) {

					throw new InvalidInputException(dateType + " cannot be empty");
				}
				if (batchDateTime.contains("T")) {

					DateTimeUtil.isValidDateTimeByPatternNano(batchDateTime);
				} else {

					DateTimeUtil.isValidDate(batchDateTime);
					if ("To Date".equalsIgnoreCase(dateType)) {
						batchDateTime = batchDateTime + "T23:59:59.9999999Z";
					} else {

						batchDateTime = batchDateTime + "T00:00:00.0000000Z";
					}
					DateTimeUtil.isValidDateTimeByPatternNano(batchDateTime);
				}

				dateCaptured = true;
			} catch (InvalidInputException exp) {

				shellHelper.printError(dateType + " cannot be empty");
			} catch (Exception exp) {

				shellHelper.printError(dateType
						+ " entered in invalid format!, acceptable format is yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z' or yyyy-MM-dd");
			}
		} while (!dateCaptured);
		return batchDateTime;
	}

	public void captureBatchOperations() {

		Map<String, String> options = new HashMap<>();

		options.put("1", QueryOption.FETCHLASTBATCHBYTYPE.name());
		options.put("2", QueryOption.FETCHALLTHREADSBYID.name());
		options.put("3", QueryOption.CLOSELASTRUNNINGBATCHID.name());
		options.put("4", QueryOption.FETCHALLFAILURESBYBATCHID.name());
		options.put("5", QueryOption.FETCHALLACCOUNTSTOTALCOUNTBYID.name());
		options.put("6", QueryOption.FETCHALLINCOMPLETEBATCHES.name());
		options.put("7", QueryOption.RETRYFAILEDBATCHID.name());
		options.put("8", QueryOption.RUNONDEMANDQUERY.name());
		options.put("9", QueryOption.SAVETOOLEXPIRY.name());

		String optionValue = inputReader.selectFromList("Query Type Option", "Please enter one of the [] values",
				options, true, null);
		QueryOption queryOption = QueryOption.valueOf(options.get(optionValue.toUpperCase()));

		String batchId = null;
		String batchType = null;
		ScheduledBatchLogResponse scheduledBatchLogResponse = null;

		switch (queryOption) {

		case FETCHLASTBATCHBYTYPE:

			batchType = captureData("BatchType", false);

			scheduledBatchLogResponse = coreScheduledBatchLogService.findLatestBatchByBatchType(batchType).getBody();

			docuSignOutputDecorator.formatScheduledBatchLogResponseOutput(scheduledBatchLogResponse);
			break;
		case FETCHALLTHREADSBYID:

			batchId = captureData("BatchId", true);
			ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch = coreConcurrentProcessLogService
					.findAllProcessesForBatchId(batchId).getBody();

			docuSignOutputDecorator.formatAllThreadData(concurrentProcessLogsInformationForBatch, batchId);
			break;
		case CLOSELASTRUNNINGBATCHID:

			batchId = captureData("BatchId", true);
			coreScheduledBatchLogService.updateBatch(batchId);
			break;
		case FETCHALLFAILURESBYBATCHID:

			batchId = captureData("BatchId", true);

			ConcurrentProcessFailureLogsInformation concurrentProcessFailureLogsInformation = coreProcessFailureLogService
					.listAllProcessFailureLogForBatchId(batchId).getBody();

			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails = coreConcurrentProcessLogService
					.findAllParentGroups(batchId).getBody();
			docuSignOutputDecorator.printAllAccountStatus(concurrentProcessLogsInformationAccountDetails, batchId);
			docuSignOutputDecorator.printFailureData(concurrentProcessFailureLogsInformation, batchId);
			break;
		case FETCHALLACCOUNTSTOTALCOUNTBYID:

			batchId = captureData("BatchId", true);
			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetailsById = coreConcurrentProcessLogService
					.findAllParentGroups(batchId).getBody();

			docuSignOutputDecorator.printAllAccountStatus(concurrentProcessLogsInformationAccountDetailsById, batchId);
			break;
		case FETCHALLINCOMPLETEBATCHES:

			batchType = captureData("BatchType", false);

			try {

				ScheduledBatchLogsInformation scheduledBatchLogsInformation = coreScheduledBatchLogService
						.findAllInCompleteBatches(batchType).getBody();

				if (null != scheduledBatchLogsInformation
						&& null != scheduledBatchLogsInformation.getScheduledBatchLogResponses()
						&& !scheduledBatchLogsInformation.getScheduledBatchLogResponses().isEmpty()) {

					scheduledBatchLogsInformation.getScheduledBatchLogResponses().forEach(batchLogResponse -> {

						docuSignOutputDecorator.formatScheduledBatchLogResponseOutput(batchLogResponse);
					});
				}
			} catch (ResourceNotFoundException exp) {

				shellHelper.printSuccess(String.format(
						" #################### No incomplete batch exists for batchType -> %s #################### ",
						batchType.toString()));

			}
			break;
		case RETRYFAILEDBATCHID:

			batchId = captureData("BatchId", true);

			retryFailedBatchWithNewBatchId(batchId);
			break;
		case RUNONDEMANDQUERY:

			QueryType queryType = readQueryType();

			switch (queryType) {

			case SELECT:

				String selectSql = captureData("SelectSQLQuery", false);

				shellHelper.printInfo(String.format("Select Query is %s", selectSql));
				List<Map<String, Object>> selectDataMapList = reportJDBCService.runSelectQuery(selectSql);

				selectDataMapList.forEach(selectDataMap -> {

					selectDataMap.forEach((key, value) -> {

						shellHelper.printSuccess(String.format("ColumnName is %s and ColumnValue is %s", key, value));

					});
				});
				break;
			case NON_SELECT:

				String nonSelectSql = captureData("NonSelectSQLQuery", false);

				shellHelper.printInfo(String.format("Non-Select Query is %s", nonSelectSql));
				reportJDBCService.runNonSelectQuery(nonSelectSql);

				break;

			default:
				shellHelper.printError(String.format("Wrong option %s selected in RUNONDEMANDQUERY", queryType));
			}

			break;

		case SAVETOOLEXPIRY:

			String auditorName = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_DB_AUDITOR_NAME);
			String integratorKey = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_INTEGRATORKEY);

			String nafValue = captureData(AppConstants.NAF, false);

			final String secretKey = new String(
					Base64.getEncoder().encode((auditorName + AppConstants.COLON + integratorKey).getBytes()));
			final String salt = new String(
					Base64.getEncoder().encode((integratorKey + AppConstants.COLON + auditorName).getBytes()));

			String encryptedNAF = AESCipher.encrypt(nafValue, secretKey, salt);
			try {

				CacheLogDefinition savedCacheLogDefinition = coreCacheDataLogService
						.findByCacheKeyAndCacheReference(AppConstants.NAF, AppConstants.NAF).getBody();

				savedCacheLogDefinition.setCacheValue(encryptedNAF);
				coreCacheDataLogService.updateCache(savedCacheLogDefinition.getCacheId(), savedCacheLogDefinition);
			} catch (ResourceNotFoundException exp) {

				CacheLogDefinition cacheLogDefinition = new CacheLogDefinition();
				cacheLogDefinition.setCacheKey(AppConstants.NAF);
				cacheLogDefinition.setCacheValue(encryptedNAF);
				cacheLogDefinition.setCacheReference(AppConstants.NAF);
				coreCacheDataLogService.saveCache(cacheLogDefinition);
			}

			break;
		default:
			shellHelper.printError(String.format("Wrong query option %s selected", optionValue));
		}
	}

	private QueryType readQueryType() {

		Map<String, String> selectOptions = new HashMap<>();

		selectOptions.put("1", QueryType.SELECT.name());
		selectOptions.put("2", QueryType.NON_SELECT.name());

		String selectOptionValue = inputReader.selectFromList("Query Type Options", "Please enter one of the [] values",
				selectOptions, true, null);
		QueryType queryType = QueryType.valueOf(selectOptions.get(selectOptionValue.toUpperCase()));
		return queryType;
	}

	private void retryFailedBatchWithNewBatchId(String batchId) {

		try {
			ScheduledBatchLogResponse scheduledBatchLogResponse = coreScheduledBatchLogService
					.findBatchByBatchId(batchId).getBody();

			if (null != scheduledBatchLogResponse && null == scheduledBatchLogResponse.getBatchEndDateTime()) {

				String batchType = scheduledBatchLogResponse.getBatchType();

				List<ConcurrentProcessLogDefinition> inCompleteAccountIdProcessList = extractFailedAccountIds(batchId);

				Set<String> inCompleteAccountIds = inCompleteAccountIdProcessList.stream()
						.map(process -> process.getAccountId()).collect(Collectors.toSet());

				Set<String> inCompleteUserIds = inCompleteAccountIdProcessList.stream()
						.map(process -> process.getUserId()).collect(Collectors.toSet());

				if (null != inCompleteAccountIds && !inCompleteAccountIds.isEmpty()) {

					cleanLastHungBatchData(batchId, inCompleteAccountIdProcessList, inCompleteAccountIds, batchType);
					retryNewBatchForHungData(scheduledBatchLogResponse, inCompleteAccountIds, inCompleteUserIds);
				} else {

					shellHelper.printError(String.format(
							" #################### No incomplete processes exist for batchId -> %s #################### ",
							batchId));
				}

			} else {

				shellHelper.printError(String.format(
						" #################### No batch exists for batchId -> %s #################### ", batchId));
			}
		} catch (ResourceNotFoundException exp) {

			shellHelper.printError(String.format(
					" #################### ResourceNotFoundException message for batchId -> %s is %s #################### ",
					batchId, exp.getMessage()));
		} catch (Exception exp) {

			shellHelper.printError(String.format(
					" #################### Exception %s thrown in running batchId -> %s #################### ",
					exp.getMessage(), batchId));
		}
	}

	private void retryNewBatchForHungData(ScheduledBatchLogResponse scheduledBatchLogResponse,
			Set<String> inCompleteAccountIds, Set<String> inCompleteUserIds)
			throws JsonProcessingException, JsonMappingException {

		String lastBatchParameters = null;
		try {

			lastBatchParameters = scheduledBatchLogResponse.getBatchStartParameters();
			BatchStartParams startParams = objectMapper.readValue(lastBatchParameters, BatchStartParams.class);

			String startDateTime = startParams.getBeginDateTime();

			String endDateTime = startParams.getEndDateTime();

			String selectedAccountIdsCommaSeparated = inCompleteAccountIds.stream().collect(Collectors.joining(","));

			String selectedUserIdsCommaSeparated = inCompleteUserIds.stream().collect(Collectors.joining(","));

			BatchTriggerInformation batchTriggerInformation = new BatchTriggerInformation();

			List<PathParam> pathParams = new ArrayList<PathParam>();
			PathParam pathParam = new PathParam();
			pathParam.setParamName("selectAccountIds");
			pathParam.setParamValue(selectedAccountIdsCommaSeparated);
			pathParams.add(pathParam);

			pathParam = new PathParam();
			pathParam.setParamName("selectUserIds");
			pathParam.setParamValue(selectedUserIdsCommaSeparated);
			pathParams.add(pathParam);

			batchTriggerInformation.setPathParams(pathParams);

			String dynamicParams = objectMapper.writeValueAsString(batchTriggerInformation);

			assertThat(shell.evaluate(() -> "loadEnvelopeData batchStartDateTime " + startDateTime
					+ " batchEndDateTime " + endDateTime + " dynamicParams " + dynamicParams));
		} catch (IOException e) {

			log.error(
					"JSON Mapping error occured in converting to BatchStartParams for string {} in calculateBatchTriggerParameters",
					lastBatchParameters);
			throw new JSONConversionException(
					"JSON Mapping error occured in converting to BatchStartParams in calculateBatchTriggerParameters",
					e);
		}
	}

	private void cleanLastHungBatchData(String batchId,
			List<ConcurrentProcessLogDefinition> inCompleteAccountIdProcessList, Set<String> inCompleteAccountIds,
			String batchType) throws IOException, JsonParseException, JsonMappingException, FileNotFoundException {

		deleteReportDataForInCompleteAccountIds(batchId, inCompleteAccountIds, batchType);
		closeHungThreads(inCompleteAccountIdProcessList);
		closeFailureRecords(batchId);
		coreScheduledBatchLogService.updateBatch(batchId, -1L);
	}

	private void closeHungThreads(List<ConcurrentProcessLogDefinition> inCompleteAccountIdProcessList) {

		inCompleteAccountIdProcessList.forEach(processLog -> {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
			concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.RETRIED.toString());

			coreConcurrentProcessLogService.updateConcurrentProcess(concurrentProcessLogDefinition,
					processLog.getProcessId());
		});
	}

	private void deleteReportDataForInCompleteAccountIds(String batchId, Set<String> inCompleteAccountIds,
			String batchType) throws IOException, JsonParseException, JsonMappingException, FileNotFoundException {

		File jsonFile = new File(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.REPORT_RULEENGINE_FILEPATH));
		PrepareReportDefinition prepareReportDefinition = objectMapper.readValue(new FileReader(jsonFile),
				PrepareReportDefinition.class);

		PrepareDataAPI batchPrepareDataAPI = prepareReportDefinition.getPrepareDataAPIs().stream()
				.filter(prepareDataAPI -> batchType.equalsIgnoreCase(prepareDataAPI.getApiRunArgs().getBatchType()))
				.findAny().orElse(null);
		reportJDBCService.deleteReportData(inCompleteAccountIds, batchId, batchPrepareDataAPI.getApiDataTableName());
	}

	private void closeFailureRecords(String batchId) {

		ConcurrentProcessFailureLogsInformation concurrentProcessFailureLogsInformationLocal = coreProcessFailureLogService
				.listAllProcessFailureLogForBatchId(batchId).getBody();

		docuSignOutputDecorator.printFailureData(concurrentProcessFailureLogsInformationLocal, batchId);

		concurrentProcessFailureLogsInformationLocal.getConcurrentProcessFailureLogDefinitions()
				.forEach(failureInfo -> {

					ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();
					concurrentProcessFailureLogDefinition.setSuccessDateTime(LocalDateTime.now().toString());
					concurrentProcessFailureLogDefinition.setFailureRecordId(failureInfo.getFailureRecordId());
					concurrentProcessFailureLogDefinition.setRetryStatus(RetryStatus.S.toString());

					coreProcessFailureLogService.updateFailureLog(concurrentProcessFailureLogDefinition,
							failureInfo.getProcessFailureId());
				});

	}

	private List<ConcurrentProcessLogDefinition> extractFailedAccountIds(String batchId) {

		ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetailsLocal = coreConcurrentProcessLogService
				.findAllParentGroups(batchId).getBody();

		List<ConcurrentProcessLogDefinition> inCompleteAccountIdProcessList = docuSignOutputDecorator
				.printAllAccountStatus(concurrentProcessLogsInformationAccountDetailsLocal, batchId);
		return inCompleteAccountIdProcessList;
	}

	public String captureData(String data, boolean uuidFormat) {

		String batchData = null;
		boolean dataCaptured = false;
		do {

			batchData = inputReader.prompt(data);

			try {
				if (StringUtils.isEmpty(batchData)) {

					throw new InvalidInputException(data + " cannot be empty");
				}

				if (uuidFormat) {

					UUID.fromString(data);
				}

				dataCaptured = true;
			} catch (InvalidInputException exp) {

				shellHelper.printError(data + " cannot be empty");
			} catch (Exception exp) {

				if (uuidFormat) {

					shellHelper.printError(data + " is not a valid UUID format");
				}
			}
		} while (!dataCaptured);
		return batchData;
	}
}