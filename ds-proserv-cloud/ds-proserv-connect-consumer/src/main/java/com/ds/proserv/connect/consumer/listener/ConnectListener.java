package com.ds.proserv.connect.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.ThrottleService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.connect.service.ConnectAsyncService;
import com.ds.proserv.feign.connect.domain.ConnectMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectListener extends AbstractMigrationListener<ConnectMessageDefinition> {

	/*
	 * @Autowired private TaskExecutor recordTaskExecutor;
	 */

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ThrottleService throttleService;

	@Autowired
	private ConnectAsyncService connectAsyncService;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(ConnectMessageDefinition connectMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConnectMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_CONNECT_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_CONNECT_QUEUE_RETRYLIMIT),
				connectMessageDefinition);

		throttleService.throttleConsumers();
	}

	@Override
	protected void callService(ConnectMessageDefinition connectMessageDefinition) {

		connectAsyncService.saveEnvelopeData(connectMessageDefinition);
	}

	@Override
	protected void sendToDeadQueue(ConnectMessageDefinition connectMessageDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", connectMessageDefinition,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				connectMessageDefinition, m -> {
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
	protected void logErrorMessage(long retryCount, Exception exp, String expReason, String httpStatus,
			ConnectMessageDefinition connectMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} and batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), connectMessageDefinition.getProcessId(), connectMessageDefinition.getBatchId(),
				retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_CONNECT_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_CONNECT_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}