package com.ds.proserv.batch.common.service;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.MissingQueueConfigurationException;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.domain.IDocuSignInformation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchQueueService {

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

	public void createFailureMessageAndSend(String accountId, String batchId, String processId, Throwable exp,
			FailureCode failureCode, FailureStep failureStep) {

		log.error(
				"Failure occurred for accountId -> {} and batchId {} with failureCode -> {}, failureReason -> {}, exceptionMessage is {} and cause is {}",
				accountId, batchId, failureCode, exp.getMessage(), exp.getMessage(), exp);

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setBatchId(batchId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode.toString());
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());

		if (StringUtils.isEmpty(exp.getMessage())) {

			concurrentProcessFailureLogDefinition.setFailureReason(exp.toString());
		} else {

			concurrentProcessFailureLogDefinition.setFailureReason(exp.getMessage());
		}
		concurrentProcessFailureLogDefinition.setFailureRecordId(accountId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep.toString());

		if (!StringUtils.isEmpty(processId)) {

			concurrentProcessFailureLogDefinition.setProcessId(processId);
		} else {

			concurrentProcessFailureLogDefinition.setProcessId("PROCESSNOTCREATED");
		}

		findQueueNameAndSend(PropertyCacheConstants.PROCESS_FAILURE_QUEUE_NAME, processId, batchId,
				concurrentProcessFailureLogDefinition);
	}

}