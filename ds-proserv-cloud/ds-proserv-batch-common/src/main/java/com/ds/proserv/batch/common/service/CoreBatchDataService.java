package com.ds.proserv.batch.common.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.batch.common.client.CoreConcurrentProcessLogClient;
import com.ds.proserv.batch.common.client.CoreProcessFailureLogClient;
import com.ds.proserv.batch.common.client.CoreScheduledBatchLogClient;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.BatchTriggerInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CoreBatchDataService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private CoreScheduledBatchLogClient coreScheduledBatchLogClient;

	@Autowired
	private CoreProcessFailureLogClient coreProcessFailureLogClient;

	@Autowired
	private CoreConcurrentProcessLogClient coreConcurrentProcessLogClient;

	public BatchTriggerInformation prepareBatchTriggerInformation(String batchType, String programStartDateTime,
			boolean useTrackIds, boolean useTrackIdsWithToDate) throws JsonProcessingException {

		BatchTriggerInformation batchTriggerInformation = new BatchTriggerInformation();
		batchTriggerInformation.setJobType(batchType);
		try {

			ResponseEntity<ScheduledBatchLogResponse> scheduledBatchLogResponseEntity = coreScheduledBatchLogClient
					.findLatestBatchByBatchType(batchType);

			ScheduledBatchLogResponse scheduledBatchLogResponse = scheduledBatchLogResponseEntity.getBody();

			if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

				if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

					log.info(
							"Successfully found last completed batch job of batchType -> {}, last completed batchId is {}",
							batchType, scheduledBatchLogResponse.getBatchId());
				} else {
					log.error(
							" ------------------------------ Another Batch running of batchType -> {} since {} ------------------------------ ",
							batchType, scheduledBatchLogResponse.getBatchStartDateTime());
				}

				calculateBatchTriggerParameters(scheduledBatchLogResponse, batchTriggerInformation,
						programStartDateTime, useTrackIds, useTrackIdsWithToDate);

			} else {

				log.error(
						" ------------------------------ Another Batch running of batchType -> {} since {} ------------------------------ ",
						batchType, scheduledBatchLogResponse.getBatchStartDateTime());

				throw new RunningBatchException("Another Batch already running for batchType " + batchType + " since "
						+ scheduledBatchLogResponse.getBatchStartDateTime());
			}

		} catch (ResourceNotFoundException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("In ResourceNotFoundException block, No Batch running of batchType -> {}", batchType);

			calculateBatchTriggerParameters(null, batchTriggerInformation, programStartDateTime, useTrackIds,
					useTrackIdsWithToDate);

		} catch (ResponseStatusException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("In ResponseStatusException block, No Batch running of jobType -> {}",
					batchTriggerInformation.getJobType());
			if (exp.getStatus() == HttpStatus.NOT_FOUND) {

				calculateBatchTriggerParameters(null, batchTriggerInformation, programStartDateTime, useTrackIds,
						useTrackIdsWithToDate);
			}

		}

		createBatch(batchTriggerInformation);
		return batchTriggerInformation;
	}

	private void createBatch(BatchTriggerInformation batchTriggerInformation) throws JsonProcessingException {

		BatchStartParams batchStartParams = new BatchStartParams();

		if (!StringUtils.isEmpty(batchTriggerInformation.getBatchStartDateTime())) {

			batchStartParams.setBeginDateTime(batchTriggerInformation.getBatchStartDateTime());
		}

		if (!StringUtils.isEmpty(batchTriggerInformation.getBatchEndDateTime())) {

			batchStartParams.setEndDateTime(batchTriggerInformation.getBatchEndDateTime());
		}

		ScheduledBatchLogRequest scheduledBatchLogRequest = new ScheduledBatchLogRequest();
		scheduledBatchLogRequest.setBatchType(batchTriggerInformation.getJobType());
		scheduledBatchLogRequest.setBatchStartParameters(objectMapper.writeValueAsString(batchStartParams));
		scheduledBatchLogRequest.setTotalRecords(0L);

		String batchId = coreScheduledBatchLogClient.saveBatch(scheduledBatchLogRequest).getBody().getBatchId();
		batchTriggerInformation.setBatchId(batchId);
	}

	public boolean reprocessingPastRecords() {

		String reprocessingPastRecord = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.REPROCESSING_PAST_RECORDS);

		if (!StringUtils.isEmpty(reprocessingPastRecord)) {

			return Boolean.parseBoolean(reprocessingPastRecord);
		}

		return false;
	}

	public void calculateBatchTriggerParameters(ScheduledBatchLogResponse scheduledBatchLogResponse,
			BatchTriggerInformation batchTriggerInformation, String programStartDateTime, boolean useTrackIds,
			boolean useTrackIdsWithToDate) {

		if (useTrackIds) {

			if (useTrackIdsWithToDate) {

				batchTriggerInformation.setBatchEndDateTime(
						DateTimeUtil.convertToStringByPattern(LocalDateTime.now(), DateTimeUtil.DATE_TIME_PATTERN));
			}
		} else {

			String newBatchStartDateTime = null;
			String newBatchEndDateTime = null;

			if (null == scheduledBatchLogResponse || reprocessingPastRecords()) {// FirstTime trigger

				newBatchStartDateTime = DateTimeUtil.convertToStringByPattern(DateTimeUtil
						.convertToLocalDateTimeByPattern(programStartDateTime, DateTimeUtil.DATE_TIME_PATTERN),
						DateTimeUtil.DATE_TIME_PATTERN);

				newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(LocalDateTime.now(),
						DateTimeUtil.DATE_TIME_PATTERN);

			} else {

				String lastBatchParameters = scheduledBatchLogResponse.getBatchStartParameters();
				BatchStartParams startParams = null;
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
								DateTimeUtil
										.convertToLocalDateTimeByPattern(startParams.getEndDateTime(),
												DateTimeUtil.DATE_TIME_PATTERN)
										.plus(1, ChronoField.MILLI_OF_DAY.getBaseUnit()),
								DateTimeUtil.DATE_TIME_PATTERN);

				newBatchEndDateTime = DateTimeUtil.convertToStringByPattern(LocalDateTime.now(),
						DateTimeUtil.DATE_TIME_PATTERN);
			}

			batchTriggerInformation.setBatchStartDateTime(newBatchStartDateTime);
			batchTriggerInformation.setBatchEndDateTime(newBatchEndDateTime);

			log.info(
					"Inside calculateBatchTriggerParameters, newBatchStartDateTime is {} and newBatchEndDateTime is {}",
					newBatchStartDateTime, newBatchEndDateTime);
		}

	}

	public String checkOrCreateBatch() {

		String batchType = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DS_JOB_BATCHTYPE);

		try {

			ResponseEntity<ScheduledBatchLogResponse> scheduledBatchLogResponseEntity = coreScheduledBatchLogClient
					.findLatestBatchByBatchType(batchType);

			ScheduledBatchLogResponse scheduledBatchLogResponse = scheduledBatchLogResponseEntity.getBody();

			if (!StringUtils.isEmpty(scheduledBatchLogResponse.getBatchEndDateTime())) {

				log.info("Successfully found last completed batch job of batchType -> {}, last completed batchId is {}",
						batchType, scheduledBatchLogResponse.getBatchId());

				return createNewBatch(batchType);

			} else {

				log.error("Another Batch running of batchType -> {} since {}", batchType,
						scheduledBatchLogResponse.getBatchStartDateTime());

				throw new RunningBatchException("Another Batch already running for batch type " + batchType + " since "
						+ scheduledBatchLogResponse.getBatchStartDateTime());

			}
		} catch (ResponseStatusException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("No Batch running of batchType -> {}", batchType);
			if (exp.getStatus() == HttpStatus.NOT_FOUND) {

				return createNewBatch(batchType);

			}
		}

		return null;
	}

	private String createNewBatch(String batchType) {

		ScheduledBatchLogRequest scheduledBatchLogRequest = new ScheduledBatchLogRequest();
		scheduledBatchLogRequest.setBatchType(batchType);
		scheduledBatchLogRequest.setBatchStartParameters(batchType);
		scheduledBatchLogRequest.setTotalRecords(0L);

		return coreScheduledBatchLogClient.saveBatch(scheduledBatchLogRequest).getBody().getBatchId();
	}

	public void finishNewBatch(String batchId, Long totalRecordsInBatch) {

		coreScheduledBatchLogClient.updateBatch(batchId, totalRecordsInBatch);
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId) {

		log.info("Creating New ConcurrentProcess with batchSize {} for batchId -> {}", batchSize, batchId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);

		return coreConcurrentProcessLogClient.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId, String groupId) {

		log.info("New ConcurrentProcess created with batchSize {} for batchId -> {} and groupId -> {}", batchSize,
				batchId, groupId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setGroupId(groupId);
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);

		return coreConcurrentProcessLogClient.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();

	}

	public void finishConcurrentProcess(String processId, String processStatus) {

		log.info("Finishing ConcurrentProcess for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(processStatus);

		coreConcurrentProcessLogClient.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();

	}

	public void createFailureProcess(String failureRecordId, String failureCode, String failureReason,
			String failureStep, String processId) {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setFailureRecordId(failureRecordId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode);
		concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep);
		concurrentProcessFailureLogDefinition.setProcessId(processId);
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());
		coreProcessFailureLogClient.saveFailureLog(concurrentProcessFailureLogDefinition);
	}

	public void createFailureProcess(String failureRecordId, String batchId, String processId, Throwable exp,
			FailureCode failureCode, FailureStep failureStep) {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setBatchId(batchId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode.toString());
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());

		if (StringUtils.isEmpty(exp.getMessage())) {

			concurrentProcessFailureLogDefinition.setFailureReason(exp.toString());
		} else {

			concurrentProcessFailureLogDefinition.setFailureReason(exp.getMessage());
		}
		concurrentProcessFailureLogDefinition.setFailureRecordId(failureRecordId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep.toString());

		if (!StringUtils.isEmpty(processId)) {

			concurrentProcessFailureLogDefinition.setProcessId(processId);
		} else {

			concurrentProcessFailureLogDefinition.setProcessId(AppConstants.PROCESSNOTCREATED);
		}

		coreProcessFailureLogClient.saveFailureLog(concurrentProcessFailureLogDefinition);
	}

	public boolean isBatchCreatedWithWorkerThreads(String batchId) {

		log.info("Checking Worker threads for batchId -> {}", batchId);

		try {

			Long totalWorkerThreads = coreConcurrentProcessLogClient.countPendingConcurrentProcessInBatch(batchId)
					.getBody();

			log.info("Total Worker thread for batchId -> {} is {}", batchId, totalWorkerThreads);
			if (totalWorkerThreads > 0) {

				return true;
			}

		} catch (ResourceNotFoundException exp) {

			log.warn("ResourceNotFoundException caught so no worker thread created");

		} catch (ResponseStatusException exp) {

			log.warn("ResponseStatusException caught so no worker thread created");
		}

		return false;

	}

	public void closeConcurrentProcess(String batchId, String processId, String processStatus,
			Long totalRecordsInProcess) {

		if (!StringUtils.isEmpty(processId) && !StringUtils.isEmpty(batchId)) {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
			concurrentProcessLogDefinition.setBatchId(batchId);
			concurrentProcessLogDefinition.setProcessId(processId);
			concurrentProcessLogDefinition.setProcessStatus(processStatus);
			concurrentProcessLogDefinition.setTotalRecordsInProcess(totalRecordsInProcess);

			coreConcurrentProcessLogClient.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();
		} else {

			log.warn("Either ProcessId -> {} or BatchId -> {} is null or empty so wrong call to close the process.",
					processId, batchId);
		}
	}
}