package com.ds.proserv.exception.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.common.util.PreparePageUtil;
import com.ds.proserv.exception.consumer.client.DSExceptionClient;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessExceptionListener extends AbstractMigrationListener<DSExceptionMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private DSExceptionClient dsExceptionClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(DSExceptionMessageDefinition dsExceptionMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConcurrentProcessLogDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_EXCEPTION_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_EXCEPTION_QUEUE_RETRYLIMIT),
				dsExceptionMessageDefinition);
	}

	@Override
	protected void callService(DSExceptionMessageDefinition dsExceptionMessageDefinition) {

		String exceptionIdsCommaSeparated = String.join(AppConstants.COMMA_DELIMITER,
				dsExceptionMessageDefinition.getRecordIds());

		dsExceptionClient.updateExceptionRetryStatus(
				PreparePageUtil.prepareExceptionPageInformation(dsExceptionMessageDefinition.getRetryStatus(),
						dsExceptionMessageDefinition.getProcessId(), exceptionIdsCommaSeparated));

		queueService.closeConcurrentProcess(dsExceptionMessageDefinition.getGroupId(),
				dsExceptionMessageDefinition.getBatchId(), dsExceptionMessageDefinition.getProcessId(),
				ProcessStatus.COMPLETED.toString(), Long.valueOf(dsExceptionMessageDefinition.getRecordIds().size()));
	}

	@Override
	protected void sendToDeadQueue(DSExceptionMessageDefinition dsExceptionMessageDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", dsExceptionMessageDefinition,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				dsExceptionMessageDefinition, m -> {
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
			DSExceptionMessageDefinition dsExceptionMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} and batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), dsExceptionMessageDefinition.getProcessId(),
				dsExceptionMessageDefinition.getBatchId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_EXCEPTION_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_EXCEPTION_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}