package com.ds.proserv.report.shell.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.cipher.AESCipher;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.JobType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.BatchNotAuthorizedException;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ResourceConditionFailedException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.service.CoreCacheDataLogService;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.coredata.service.CoreScheduledBatchLogService;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.BatchTriggerInformation;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.report.processor.ManageReportDataProcessor;
import com.ds.proserv.report.processor.PrepareReportDataProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchTriggerService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ManageReportDataProcessor manageReportDataProcessor;

	@Autowired
	private CoreScheduledBatchLogService coreScheduledBatchLogService;

	@Autowired
	private PrepareReportDataProcessor prepareReportDataProcessor;

	@Autowired
	private CoreCacheDataLogService coreCacheDataLogService;

	public void callService(BatchTriggerInformation batchTriggerInformation) throws Exception {

//		checkIfAuthorizedToRunApp();

		PrepareReportDefinition prepareReportDefinition = objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.REPORT_RULEENGINE_FILEPATH))),
				PrepareReportDefinition.class);

		log.info("prepareReportDefinition loaded and created for jobType -> {}", batchTriggerInformation.getJobType());

		if (JobType.MANAGEDATA.toString().equalsIgnoreCase(batchTriggerInformation.getJobType())) {

			for (ManageDataAPI manageDataAPI : prepareReportDefinition.getManageDataAPIs()) {

				setupInputParams(batchTriggerInformation, prepareReportDefinition,
						manageDataAPI.getExportRunArgs().getPathParams(),
						manageDataAPI.getExportRunArgs().getBatchType());
			}
		} else {

			for (PrepareDataAPI prepareDataAPI : prepareReportDefinition.getPrepareDataAPIs()) {

				setupInputParams(batchTriggerInformation, prepareReportDefinition,
						prepareDataAPI.getApiRunArgs().getPathParams(), prepareDataAPI.getApiRunArgs().getBatchType());
			}

		}

		loadBatchWithValidParams(batchTriggerInformation, prepareReportDefinition);
	}

	public void checkIfAuthorizedToRunApp() {

		try {

			String auditorName = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_DB_AUDITOR_NAME);
			String integratorKey = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSAUTH_INTEGRATORKEY);

			CacheLogDefinition cacheLogDefinition = coreCacheDataLogService
					.findByCacheKeyAndCacheReference(AppConstants.NAF, AppConstants.NAF).getBody();

			String encryptedNAF = cacheLogDefinition.getCacheValue();

			final String secretKey = new String(
					Base64.getEncoder().encode((auditorName + AppConstants.COLON + integratorKey).getBytes()));
			final String salt = new String(
					Base64.getEncoder().encode((integratorKey + AppConstants.COLON + auditorName).getBytes()));

			Long naf = Long.parseLong(AESCipher.decrypt(encryptedNAF, secretKey, salt));

			Long currentTime = DateTimeUtil.currentEpochTime();

			if (currentTime > naf) {

				throw new BatchNotAuthorizedException(
						" $$$$$$$$$$$$$$$$$$$$ Not Authorized to trigger this job $$$$$$$$$$$$$$$$$$$$ ");
			}

		} catch (ResourceNotFoundException exp) {

			throw new ResourceConditionFailedException(
					" !!!!!!!!!!!!!!!!!!!! Required inputs not ready to trigger the batch  !!!!!!!!!!!!!!!!!!!! ");

		}
	}

	private void setupInputParams(BatchTriggerInformation batchTriggerInformation,
			PrepareReportDefinition prepareReportDefinition, List<PathParam> pathParamList, String batchType) {

		try {

			ResponseEntity<ScheduledBatchLogResponse> scheduledBatchLogResponseEntity = coreScheduledBatchLogService
					.findLatestBatchByBatchType(batchType);

			ScheduledBatchLogResponse scheduledBatchLogResponse = scheduledBatchLogResponseEntity.getBody();

			if (isCompleteBatchOnError(prepareReportDefinition)
					|| null != scheduledBatchLogResponse.getBatchEndDateTime()) {

				if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

					log.info(
							"Successfully found last completed batch job of jobType -> {}, last completed batchId is {}",
							batchTriggerInformation.getJobType(), scheduledBatchLogResponse.getBatchId());
				} else {
					log.error(
							" ------------------------------ Another Batch running of jobType -> {} since {} ------------------------------ ",
							batchTriggerInformation.getJobType(), scheduledBatchLogResponse.getBatchStartDateTime());
				}

				calculateBatchTriggerParameters(scheduledBatchLogResponse, batchTriggerInformation, pathParamList);

			} else {

				log.error(
						" ------------------------------ Another Batch running of jobType -> {} since {} ------------------------------ ",
						batchTriggerInformation.getJobType(), scheduledBatchLogResponse.getBatchStartDateTime());

				throw new RunningBatchException(
						"Another Batch already running for batch type " + batchTriggerInformation.getJobType()
								+ " since " + scheduledBatchLogResponse.getBatchStartDateTime());
			}

		} catch (ResourceNotFoundException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("In ResourceNotFoundException block, No Batch running of jobType -> {}",
					batchTriggerInformation.getJobType());

			calculateBatchTriggerParameters(null, batchTriggerInformation, pathParamList);

		} catch (ResponseStatusException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("In ResponseStatusException block, No Batch running of jobType -> {}",
					batchTriggerInformation.getJobType());
			if (exp.getStatus() == HttpStatus.NOT_FOUND) {

				calculateBatchTriggerParameters(null, batchTriggerInformation, pathParamList);
			}

		}
	}

	private boolean isCompleteBatchOnError(PrepareReportDefinition prepareReportDefinition) {

		return prepareReportDefinition.getJobRunArgs().isCompleteBatchOnError();
	}

	private void loadBatchWithValidParams(BatchTriggerInformation batchTriggerInformation,
			PrepareReportDefinition prepareReportDefinition) throws Exception {

		if (JobType.MANAGEDATA.toString().equalsIgnoreCase(batchTriggerInformation.getJobType())) {

			manageReportDataProcessor.callAPIWithInputParams(prepareReportDefinition, null, null);
		} else {

			prepareReportDataProcessor.callAPIWithInputParams(prepareReportDefinition, null, null);
		}
	}

	public void calculateBatchTriggerParameters(ScheduledBatchLogResponse scheduledBatchLogResponse,
			BatchTriggerInformation batchTriggerInformation, List<PathParam> pathParamList) {

		String newBatchStartDateTime = null;
		String newBatchEndDateTime = null;

		if (null != batchTriggerInformation.getBatchStartDateTime()) {

			// BatchStartDateTime is sent with other params
			if (null != batchTriggerInformation.getBatchEndDateTime()) {

				// Both BatchStartDateTime and BatchEndDateTime are sent in the request
				newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();
				newBatchEndDateTime = batchTriggerInformation.getBatchEndDateTime();
			} else {

				if (null != batchTriggerInformation.getNumberOfHours()
						&& batchTriggerInformation.getNumberOfHours() > -1) {

					// BatchStartDateTime and NumberOfHours are sent in the request
					newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();
					newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(
							DateTimeUtil
									.convertToLocalDateTimeByPattern(batchTriggerInformation.getBatchStartDateTime(),
											DateTimeUtil.DATE_TIME_PATTERN_NANO)
									.plusHours(batchTriggerInformation.getNumberOfHours()),
							DateTimeUtil.DATE_TIME_PATTERN_NANO);

				} else {

					// Only BatchStartDateTime is sent in the request
					newBatchStartDateTime = batchTriggerInformation.getBatchStartDateTime();
					newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(LocalDateTime.now(),
							DateTimeUtil.DATE_TIME_PATTERN_NANO);

				}
			}
		} else if (null != batchTriggerInformation.getNumberOfHours()
				&& batchTriggerInformation.getNumberOfHours() > -1) {

			// Only NumberOfHours are sent in the request
			String lastBatchParameters = scheduledBatchLogResponse.getBatchStartParameters();

			BatchStartParams startParams;
			try {

				startParams = objectMapper.readValue(lastBatchParameters, BatchStartParams.class);
			} catch (IOException e) {

				log.error(
						"JSON Mapping error occured in converting to BatchStartParams for string {} in calculateBatchTriggerParameters",
						lastBatchParameters);
				throw new JSONConversionException(
						"JSON Mapping error occured in converting to BatchStartParams in calculateBatchTriggerParameters",
						e);
			}

			newBatchStartDateTime = DateTimeUtil
					.convertToStringByPattern(
							DateTimeUtil.convertToLocalDateTimeByPattern(startParams.getEndDateTime(),
									DateTimeUtil.DATE_TIME_PATTERN_NANO).plusSeconds(1),
							DateTimeUtil.DATE_TIME_PATTERN_NANO);

			LocalDateTime calculatedTime = DateTimeUtil
					.convertToLocalDateTimeByPattern(startParams.getEndDateTime(), DateTimeUtil.DATE_TIME_PATTERN_NANO)
					.plusSeconds(3600 * batchTriggerInformation.getNumberOfHours());
			LocalDateTime currentTime = LocalDateTime.now();

			if (calculatedTime.isAfter(currentTime)) {// BatchEndDateTime should not be greater than currentdatetime

				newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(currentTime,
						DateTimeUtil.DATE_TIME_PATTERN_NANO);
			} else {

				newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(
						DateTimeUtil
								.convertToLocalDateTimeByPattern(startParams.getEndDateTime(),
										DateTimeUtil.DATE_TIME_PATTERN_NANO)
								.plusHours(batchTriggerInformation.getNumberOfHours()),
						DateTimeUtil.DATE_TIME_PATTERN_NANO);

			}

		}

		log.info("Inside calculateBatchTriggerParameters, newBatchStartDateTime is {} and newBatchEndDateTime is {}",
				newBatchStartDateTime, newBatchEndDateTime);

		populatePathParamList(pathParamList, newBatchStartDateTime, newBatchEndDateTime, batchTriggerInformation);

	}

	private void populatePathParamList(List<PathParam> pathParamList, String newBatchStartDateTime,
			String newBatchEndDateTime, BatchTriggerInformation batchTriggerInformation) {

		PathParam pathParam = new PathParam();
		pathParam.setParamName(AppConstants.INPUT_FROM_DATE);
		pathParam.setParamValue(newBatchStartDateTime);

		pathParamList.add(pathParam);

		pathParam = new PathParam();
		pathParam.setParamName(AppConstants.INPUT_TO_DATE);
		pathParam.setParamValue(newBatchEndDateTime);

		pathParamList.add(pathParam);

		log.info("Job's pathParamList size before is {}", pathParamList.size());

		List<PathParam> dynamicBatchTiggerPathParams = batchTriggerInformation.getPathParams();

		if (null != dynamicBatchTiggerPathParams && !dynamicBatchTiggerPathParams.isEmpty()) {

			log.info("Overriding or adding new PathParam from batchTriggerInformation to the job");

			// Replace (aka override) path from batchTriggerInformation to the job's
			// pathParamList if param name is same
			Iterator<PathParam> existingPathParamIterator = pathParamList.iterator();

			while (existingPathParamIterator.hasNext()) {

				PathParam existingPathParam = existingPathParamIterator.next();
				PathParam dynamicPathParam = dynamicBatchTiggerPathParams.stream()
						.filter(batchTriggerParam -> batchTriggerParam.getParamName()
								.equalsIgnoreCase(existingPathParam.getParamName()))
						.findAny().orElse(null);

				if (null != dynamicPathParam) {

					existingPathParam.setParamValue(dynamicPathParam.getParamValue());
				}
			}

			// Add new Path from batchTriggerInformation to the job's pathParamList
			Iterator<PathParam> dynamicPathParamIterator = dynamicBatchTiggerPathParams.iterator();

			while (dynamicPathParamIterator.hasNext()) {

				PathParam dynamicPathParam = dynamicPathParamIterator.next();

				PathParam filteredExistingPathParam = pathParamList.stream()
						.filter(existingPathParam -> existingPathParam.getParamName()
								.equalsIgnoreCase(dynamicPathParam.getParamName()))
						.findAny().orElse(null);

				if (null == filteredExistingPathParam) {

					pathParamList.add(dynamicPathParam);
				}
			}

		}

		log.info("Job's pathParamList size after is {}", pathParamList.size());
	}
}