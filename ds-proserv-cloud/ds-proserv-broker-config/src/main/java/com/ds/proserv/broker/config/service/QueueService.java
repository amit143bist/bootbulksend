package com.ds.proserv.broker.config.service;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.MissingQueueConfigurationException;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.domain.IDocuSignInformation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QueueService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private DSCacheManager dsCacheManager;

	public void findQueueNameAndSend(String keyName, IDocuSignInformation messageInformation) {

		String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(keyName,
				PropertyCacheConstants.QUEUE_REFERENCE_NAME);

		if (StringUtils.isEmpty(queueName)) {

			log.error(keyName + " cannot be empty, please check cache or property file for key -> {}", keyName);
			throw new MissingQueueConfigurationException(
					keyName + " cannot be empty, please check cache or property file");
		}

		log.info("************************* Sending message in message to queue -> {} *************************",
				queueName);

		rabbitTemplate.convertAndSend(queueName, messageInformation);

	}

	public void findQueueNameAndSend(String keyName, String processId, String batchId,
			IDocuSignInformation messageInformation) {

		String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(keyName,
				PropertyCacheConstants.QUEUE_REFERENCE_NAME);

		if (StringUtils.isEmpty(queueName)) {

			log.error(keyName + " cannot be empty, please check cache or property file for key -> {}", keyName);
			throw new MissingQueueConfigurationException(
					keyName + " cannot be empty, please check cache or property file");
		}

		log.info(
				"************************* Sending message in message to queue -> {} for processId -> {} and batchId -> {} *************************",
				queueName, processId, batchId);

		rabbitTemplate.convertAndSend(queueName, messageInformation);

	}

	public void createFailureMessageAndSend(String failureRecordId, String batchId, String processId,
			String failureReason, FailureCode failureCode, FailureStep failureStep) {
		log.error(
				"Failure occurred for accountId -> {} and batchId {} processId -> {} with failureCode -> {}, failureReason -> {}, ",
				failureRecordId, batchId, processId, failureCode, failureReason);

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setBatchId(batchId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode.toString());
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());

		concurrentProcessFailureLogDefinition.setFailureRecordId(failureRecordId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep.toString());
		concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		if (!StringUtils.isEmpty(processId)) {

			concurrentProcessFailureLogDefinition.setProcessId(processId);
		} else {

			concurrentProcessFailureLogDefinition.setProcessId(AppConstants.PROCESSNOTCREATED);
		}

		findQueueNameAndSend(PropertyCacheConstants.PROCESS_FAILURE_QUEUE_NAME, processId, batchId,
				concurrentProcessFailureLogDefinition);

	}

	public void createFailureMessageAndSend(String failureRecordId, String batchId, String processId, Throwable exp,
			FailureCode failureCode, FailureStep failureStep) {

		String failureReason = exp.toString();

		if (!StringUtils.isEmpty(exp.getMessage())) {

			failureReason = exp.getMessage();
		}

		createFailureMessageAndSend(failureRecordId, batchId, processId, failureReason, failureCode, failureStep);
	}

	public void closeConcurrentProcess(String groupId, String batchId, String processId, String processStatus,
			Long totalRecordsInProcess) {

		if (!StringUtils.isEmpty(processId) && !StringUtils.isEmpty(batchId)) {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
			concurrentProcessLogDefinition.setBatchId(batchId);
			concurrentProcessLogDefinition.setProcessId(processId);
			concurrentProcessLogDefinition.setProcessStatus(processStatus);
			concurrentProcessLogDefinition.setGroupId(groupId);
			concurrentProcessLogDefinition.setTotalRecordsInProcess(totalRecordsInProcess);

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			rabbitTemplate.convertAndSend(queueName, concurrentProcessLogDefinition);
		} else {

			log.warn("Either ProcessId -> {} or BatchId -> {} is null or empty so wrong call to close the process.",
					processId, batchId);
		}
	}
}