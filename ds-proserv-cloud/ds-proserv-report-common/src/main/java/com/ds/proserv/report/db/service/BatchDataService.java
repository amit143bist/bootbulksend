package com.ds.proserv.report.db.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogsInformation;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.coredata.service.CoreConcurrentProcessLogService;
import com.ds.proserv.feign.coredata.service.CoreProcessFailureLogService;
import com.ds.proserv.feign.coredata.service.CoreScheduledBatchLogService;
import com.ds.proserv.feign.report.domain.BatchResultInformation;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.ds.proserv.feign.report.domain.ReportRunArgs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class BatchDataService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CoreProcessFailureLogService coreProcessFailureLogService;

	@Autowired
	private CoreScheduledBatchLogService coreScheduledBatchLogService;

	@Autowired
	private CoreConcurrentProcessLogService coreConcurrentProcessLogService;

	/*
	 * @Autowired private CoreScheduledBatchLogClient coreScheduledBatchLogClient;
	 * 
	 * @Autowired private CoreProcessFailureLogClient coreProcessFailureLogClient;
	 * 
	 * @Autowired private CoreConcurrentProcessLogClient
	 * coreConcurrentProcessLogClient;
	 */

	public String createBatchJob(String batchType, BatchStartParams batchStartParams, Long totalRecords) {

		log.info("Creating BatchJob for batchType -> {} and totalRecords -> {}", batchType, totalRecords);

		ScheduledBatchLogRequest scheduledBatchLogRequest = new ScheduledBatchLogRequest();

		scheduledBatchLogRequest.setBatchType(batchType);
		try {
			scheduledBatchLogRequest.setBatchStartParameters(objectMapper.writeValueAsString(batchStartParams));
		} catch (JsonProcessingException e) {

			log.error(
					"JSON Mapping error occured in converting to BatchStartParams string for object {} in createBatchJob",
					batchStartParams);
			throw new JSONConversionException(
					"JSON Mapping error occured in converting to BatchStartParams in createBatchJob", e);
		}
		scheduledBatchLogRequest.setTotalRecords(totalRecords);

		return coreScheduledBatchLogService.saveBatch(scheduledBatchLogRequest).getBody().getBatchId();
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId, String groupId,
			String accountId, String userId) {

		log.info("New ConcurrentProcess created with batchSize {} for batchId -> {} and groupId -> {}", batchSize,
				batchId, groupId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setGroupId(groupId);
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);
		concurrentProcessLogDefinition.setAccountId(accountId);
		concurrentProcessLogDefinition.setUserId(userId);

		/*
		 * if (null != coreConcurrentProcessLogClient) {
		 * 
		 * return coreConcurrentProcessLogClient.saveConcurrentProcess(
		 * concurrentProcessLogDefinition).getBody(); } else {
		 */

		return coreConcurrentProcessLogService.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();
//		}

	}

	public void finishConcurrentProcess(String processId) {

		log.info("Finishing ConcurrentProcess for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());

		/*
		 * if (null != coreConcurrentProcessLogClient) {
		 * 
		 * coreConcurrentProcessLogClient.updateConcurrentProcess(
		 * concurrentProcessLogDefinition, processId).getBody(); } else {
		 */

		coreConcurrentProcessLogService.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();
//		}

	}

	public void finishConcurrentProcessWithNoRecords(String processId) {

		log.info("Finishing ConcurrentProcess for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETEDWITHNORECORDS.toString());

		coreConcurrentProcessLogService.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();

	}

	public void finishConcurrentProcessWithTotalRecords(String processId) {

		log.info("Finishing ConcurrentProcess with totalrecords for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());
		try {

			/*
			 * if (null != coreConcurrentProcessLogClient) {
			 * 
			 * Long totalRecordsInGroup =
			 * coreConcurrentProcessLogClient.countTotalRecordsInGroup(processId).getBody();
			 * 
			 * concurrentProcessLogDefinition.setTotalRecordsInProcess(totalRecordsInGroup);
			 * coreConcurrentProcessLogClient.updateConcurrentProcess(
			 * concurrentProcessLogDefinition, processId) .getBody(); } else {
			 */

			Long totalRecordsInGroup = coreConcurrentProcessLogService.countTotalRecordsInGroup(processId).getBody();

			concurrentProcessLogDefinition.setTotalRecordsInProcess(totalRecordsInGroup);
			coreConcurrentProcessLogService.updateConcurrentProcess(concurrentProcessLogDefinition, processId)
					.getBody();
//			}

		} catch (ResourceNotFoundException exp) {

			log.error(
					" ~~~~~~~~~~~~~~~~~~~~~~~~~ Cound not find any process with the groupId -> {} ~~~~~~~~~~~~~~~~~~~~~~~~~ ",
					processId);
		}

	}

	public void publishBatchData(String batchId, boolean failureReported,
			BatchResultInformation batchResultInformation) {

		ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch = null;

		/*
		 * if (null != coreConcurrentProcessLogClient) {
		 * 
		 * concurrentProcessLogsInformationForBatch = coreConcurrentProcessLogClient
		 * .findAllProcessesForBatchId(batchId).getBody(); } else {
		 */

		concurrentProcessLogsInformationForBatch = coreConcurrentProcessLogService.findAllProcessesForBatchId(batchId)
				.getBody();
//		}
		List<String> successAccountIds = new ArrayList<String>();
		List<String> failedAccountIds = new ArrayList<String>();
		if (null != concurrentProcessLogsInformationForBatch
				&& null != concurrentProcessLogsInformationForBatch.getConcurrentProcessLogDefinitions()) {

			log.info(
					" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<< {} is the total concurrent threads used to run the batchId -> {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>> ",
					concurrentProcessLogsInformationForBatch.getConcurrentProcessLogDefinitions().size(), batchId);

		}

		if (failureReported) {

			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails = null;

			/*
			 * if (null != coreConcurrentProcessLogClient) {
			 * 
			 * concurrentProcessLogsInformationAccountDetails =
			 * coreConcurrentProcessLogClient .findAllParentGroups(batchId).getBody(); }
			 * else {
			 */

			concurrentProcessLogsInformationAccountDetails = coreConcurrentProcessLogService
					.findAllParentGroups(batchId).getBody();
//			}

			log.info(
					" ------------------------------ ------------------------------ ------------------------------ ------------------------------ ");
			log.error(
					" ------------------------------ Failure Reported for batchId -> {} ------------------------------ ",
					batchId);
			if (null != concurrentProcessLogsInformationAccountDetails
					&& null != concurrentProcessLogsInformationAccountDetails.getConcurrentProcessLogDefinitions()) {

				List<ConcurrentProcessLogDefinition> processAccountInfoList = concurrentProcessLogsInformationAccountDetails
						.getConcurrentProcessLogDefinitions();

				log.info(
						" #################### Total Accounts processed for batchId -> {} are {} #################### ",
						batchId, processAccountInfoList.size());

				processAccountInfoList.forEach(accountInfo -> {

					if (null != accountInfo.getProcessEndDateTime()) {

						log.info(
								" #################### AccountId -> {} has successfully processed {} records #################### ",
								accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());
						successAccountIds.add(accountInfo.getGroupId());
					} else {

						log.error(" #################### AccountId -> {} has reported error #################### ",
								accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());
						failedAccountIds.add(accountInfo.getGroupId());
					}

				});

			}

			showFailedBatchRecordsDetails(batchId, concurrentProcessLogsInformationForBatch);

			log.info(
					" ------------------------------ ------------------------------ ------------------------------ ------------------------------ ");
		} else {

			ConcurrentProcessLogsInformation concurrentProcessLogsInformationAccountDetails = null;

			/*
			 * if (null != coreConcurrentProcessLogClient) {
			 * 
			 * concurrentProcessLogsInformationAccountDetails =
			 * coreConcurrentProcessLogClient
			 * .findAllSuccessParentGroups(batchId).getBody(); } else {
			 */

			concurrentProcessLogsInformationAccountDetails = coreConcurrentProcessLogService
					.findAllSuccessParentGroups(batchId).getBody();
//			}

			if (null != concurrentProcessLogsInformationAccountDetails
					&& null != concurrentProcessLogsInformationAccountDetails.getConcurrentProcessLogDefinitions()) {

				List<ConcurrentProcessLogDefinition> processAccountInfoList = concurrentProcessLogsInformationAccountDetails
						.getConcurrentProcessLogDefinitions();

				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ");
				log.info(
						" #################### Total Accounts successfully processed for batchId -> {} are {} #################### ",
						batchId, processAccountInfoList.size());

				processAccountInfoList.forEach(accountInfo -> {

					log.info(
							" #################### AccountId -> {} has successfully processed {} records #################### ",
							accountInfo.getGroupId(), accountInfo.getTotalRecordsInProcess());

					successAccountIds.add(accountInfo.getGroupId());
				});
				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ");

			}
		}

		batchResultInformation.setSuccessAccountIds(successAccountIds);
		batchResultInformation.setFailedAccountIds(failedAccountIds);

	}

	private void showFailedBatchRecordsDetails(String batchId,
			ConcurrentProcessLogsInformation concurrentProcessLogsInformationForBatch) {

		if (null != concurrentProcessLogsInformationForBatch) {

			List<ConcurrentProcessLogDefinition> processList = concurrentProcessLogsInformationForBatch
					.getConcurrentProcessLogDefinitions();

			processList.forEach(processInfo -> {

				if (null != processInfo.getProcessEndDateTime()) {

					log.info(" :::::::::::::::::::::::::::::: Success thread details :::::::::::::::::::::::::::::: ");
					if (null != processInfo.getAccountId()) {

						log.info("AccountId -> {} is successfully completed using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);
					} else {

						log.info("AccountId -> {} is successfully completed using processId -> {} for batchId -> {}",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId);
					}

				} else {

					log.info(" :::::::::::::::::::::::::::::: Failed thread details :::::::::::::::::::::::::::::: ");
					if (null != processInfo.getAccountId()) {

						log.info("AccountId -> {} is unsuccessfully processed using processId -> {} for batchId -> {}",
								processInfo.getAccountId(), processInfo.getProcessId(), batchId);
					} else {

						log.info("AccountId -> {} is unsuccessfully completed using processId -> {} for batchId -> {}",
								processInfo.getGroupId(), processInfo.getProcessId(), batchId);
					}

				}
			});
		}
	}

	public void finishBatchProcess(String batchId, Long totalRecords) {

		/*
		 * if (null != coreScheduledBatchLogClient) {
		 * 
		 * coreScheduledBatchLogClient.updateBatch(batchId, totalRecords); } else {
		 */

		coreScheduledBatchLogService.updateBatch(batchId, totalRecords);
//		}
	}

	public void finishBatchProcess(String batchId) {

		ResponseEntity<Long> processCount = null;

		/*
		 * if (null != coreConcurrentProcessLogClient) {
		 * 
		 * processCount =
		 * coreConcurrentProcessLogClient.countPendingConcurrentProcessInBatch(batchId);
		 * } else {
		 */

		processCount = coreConcurrentProcessLogService.countPendingConcurrentProcessInBatch(batchId);
//		}

		if (null != processCount && 0 == processCount.getBody().intValue()) {

			log.info("Finishing the batch for batchId -> {}", batchId);

			Long totalRecordsInBatch = null;

			/*
			 * if (null != coreConcurrentProcessLogClient) {
			 * 
			 * totalRecordsInBatch =
			 * coreConcurrentProcessLogClient.countTotalRecordsInBatch(batchId).getBody(); }
			 * else {
			 */

			totalRecordsInBatch = coreConcurrentProcessLogService.countTotalRecordsInBatch(batchId).getBody();
//			}

			ScheduledBatchLogResponse scheduledBatchLogResponse = null;

			/*
			 * if (null != coreScheduledBatchLogClient) {
			 * 
			 * scheduledBatchLogResponse = coreScheduledBatchLogClient.updateBatch(batchId,
			 * totalRecordsInBatch) .getBody(); } else {
			 */

			scheduledBatchLogResponse = coreScheduledBatchLogService.updateBatch(batchId, totalRecordsInBatch)
					.getBody();
//			}

			log.info(
					"Total Count of records completed in a batch {} is {} and totalRecordsInBatch from processLog table is {}",
					batchId, scheduledBatchLogResponse.getTotalRecords(), totalRecordsInBatch);
		} else {

			if (null != processCount) {

				log.warn("Total Count of process pending to be completed is {}", processCount.getBody());
			} else {
				log.error("Something went wrong, processCount cannot be null");
			}
		}

	}

	public boolean isBatchCompleted(String batchId) {

		log.info("Checking batch status for batchId -> {}", batchId);

		/*
		 * if (null != coreScheduledBatchLogClient) {
		 * 
		 * ScheduledBatchLogResponse scheduledBatchLogResponse =
		 * coreScheduledBatchLogClient .findBatchByBatchId(batchId).getBody();
		 * 
		 * return null != scheduledBatchLogResponse.getBatchEndDateTime(); } else {
		 */

		ScheduledBatchLogResponse scheduledBatchLogResponse = coreScheduledBatchLogService.findBatchByBatchId(batchId)
				.getBody();

		return null != scheduledBatchLogResponse.getBatchEndDateTime();
//		}

	}

	public boolean isBatchCompletedWithFailure(String batchId) {

		log.info("Checking Failure status for batchId -> {}", batchId);

		List<String> batchIds = new ArrayList<String>(1);
		batchIds.add(batchId);

		/*
		 * if (null != coreProcessFailureLogClient) {
		 * 
		 * Long totalFailureCount =
		 * coreProcessFailureLogClient.countProcessFailuresByBatchIds(batchIds).getBody(
		 * ); return totalFailureCount != 0; } else {
		 */

		Long totalFailureCount = coreProcessFailureLogService.countProcessFailuresByBatchIds(batchIds).getBody();
		return totalFailureCount != 0;
//		}

	}

	public void createFailureRecord(String accountId, String batchId, String failureCode, String failureReason,
			String failureStep, Throwable exp, String processId) {

		log.error(
				"Failure occurred for accountId -> {} and batchId {} with failureCode -> {}, failureReason -> {}, exceptionMessage is {} and cause is {}",
				accountId, batchId, failureCode, failureReason, exp.getMessage(), exp);

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode);

		if (StringUtils.isEmpty(failureReason)) {

			concurrentProcessFailureLogDefinition.setFailureReason(exp.toString());
		} else {
			concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		}
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());
		concurrentProcessFailureLogDefinition.setFailureRecordId(accountId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep);
		concurrentProcessFailureLogDefinition.setBatchId(batchId);

		if (!StringUtils.isEmpty(processId)) {

			concurrentProcessFailureLogDefinition.setProcessId(processId);
		} else {

			concurrentProcessFailureLogDefinition.setProcessId("PROCESSNOTCREATED");
		}

		/*
		 * if (null != coreProcessFailureLogClient) {
		 * 
		 * coreProcessFailureLogClient.saveFailureLog(
		 * concurrentProcessFailureLogDefinition); } else {
		 */

		coreProcessFailureLogService.saveFailureLog(concurrentProcessFailureLogDefinition);
//		}
	}

	public BatchResultInformation finishBatchProcessData(ReportRunArgs apiRunArgs, String batchType, String batchId) {

		finishBatchProcess(batchId);

		BatchResultInformation batchResultInformation = new BatchResultInformation();
		batchResultInformation.setBatchId(batchId);

		if (apiRunArgs.isCompleteBatchOnError()
				|| (isBatchCompleted(batchId) && !isBatchCompletedWithFailure(batchId))) {

			if (isBatchCompletedWithFailure(batchId)) {

				publishBatchData(batchId, true, batchResultInformation);
				log.info(
						" ------------------------------ " + batchType
								+ " batch with batchId -> {} closed but with errors ------------------------------ ",
						batchId);

				return createBatchResultInfo(batchId, ValidationResult.SOMEORALLFAILED.toString(),
						batchResultInformation);
			} else {

				publishBatchData(batchId, false, batchResultInformation);
				log.info(
						" *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ " + batchType
								+ " batch with batchId -> {} completed successfully *$*$*$*$*$*$*$*$*$*$*$*$*$*$*$ ",
						batchId);
				return createBatchResultInfo(batchId, ValidationResult.SUCCESS.toString(), batchResultInformation);
			}
		} else {

			publishBatchData(batchId, true, batchResultInformation);
			log.info(
					" ------------------------------ " + batchType
							+ " batch with batchId -> {} NOT Completed successfully ------------------------------ ",
					batchId);

			return createBatchResultInfo(batchId, ValidationResult.SOMEORALLFAILED.toString(), batchResultInformation);
		}
	}

	private BatchResultInformation createBatchResultInfo(String batchId, String batchStatus,
			BatchResultInformation batchResultInformation) {

		batchResultInformation.setBatchId(batchId);
		batchResultInformation.setBatchStatus(batchStatus);

		return batchResultInformation;
	}

	public boolean isBatchCreatedWithWorkerThreads(String batchId) {

		log.info("Checking Worker threads for batchId -> {}", batchId);

		try {

			Long totalWorkerThreads = coreConcurrentProcessLogService.countPendingConcurrentProcessInBatch(batchId)
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
}