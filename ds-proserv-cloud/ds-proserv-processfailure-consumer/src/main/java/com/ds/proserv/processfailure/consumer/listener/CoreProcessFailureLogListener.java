package com.ds.proserv.processfailure.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.processfailure.consumer.client.CoreProcessFailureLogClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CoreProcessFailureLogListener extends AbstractMigrationListener<ConcurrentProcessFailureLogDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	CoreProcessFailureLogClient coreProcessFailureLogClient;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConcurrentProcessFailureLogDefinition received in processMessage() -> {} and xDeath value is {}",
				concurrentProcessFailureLogDefinition, xDeath);

		super.processMessage(xDeath, getRetryLimit(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_QUEUE_RETRYLIMIT),
				PropertyCacheConstants.PROCESS_FAILURE_QUEUE_RETRYLIMIT), concurrentProcessFailureLogDefinition);

	}

	@Override
	public void callService(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition) {

		log.info("Calling saveFailureLog/updateFailureLog for processId -> {}, failureRecordId are {}",
				concurrentProcessFailureLogDefinition.getProcessId(),
				concurrentProcessFailureLogDefinition.getProcessFailureId());

		if (StringUtils.isEmpty(concurrentProcessFailureLogDefinition.getProcessFailureId())) {

			coreProcessFailureLogClient.saveFailureLog(concurrentProcessFailureLogDefinition);
		} else {

			coreProcessFailureLogClient.updateFailureLog(concurrentProcessFailureLogDefinition,
					concurrentProcessFailureLogDefinition.getProcessFailureId());
		}
	}

	@Override
	public void sendToDeadQueue(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
				concurrentProcessFailureLogDefinition, errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentProcessFailureLogDefinition, m -> {
					m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORSTATUSCODE.toString(),
							httpStatus);
					m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORREASON.toString(),
							errorHeaderMessage);
					m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORTIMESTAMP.toString(),
							DateTimeUtil.convertToString(LocalDateTime.now()));
					return m;
				});
	}

	@Override
	public void logErrorMessage(long retryCount, Exception exp, String expReason, String httpStatus,
			ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing failureRecordId {} and processId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentProcessFailureLogDefinition.getFailureRecordId(),
				concurrentProcessFailureLogDefinition.getProcessId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_FAILURE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_FAILURE_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}