package com.ds.proserv.bulksend.recordlog.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.bulksend.recordlog.client.BulkSendRecordLogClient;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogInformation;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSendRecordLogListener extends AbstractMigrationListener<BulkSendRecordLogMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private BulkSendRecordLogClient bulkSendRecordLogClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(BulkSendRecordLogMessageDefinition bulkSendRecordLogMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("bulkSendRecordLogMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDRECORDLOG_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDRECORDLOG_QUEUE_RETRYLIMIT),
				bulkSendRecordLogMessageDefinition);
	}

	@Override
	protected void callService(BulkSendRecordLogMessageDefinition bulkSendRecordLogMessageDefinition) {

		BulkSendRecordLogInformation bulkSendRecordLogInformation = new BulkSendRecordLogInformation();
		bulkSendRecordLogInformation
				.setBulkSendRecordLogDefinitions(bulkSendRecordLogMessageDefinition.getBulkSendRecordLogDefinitions());

		bulkSendRecordLogClient.bulkSaveBulkSendRecordLogs(bulkSendRecordLogInformation);

		queueService.closeConcurrentProcess(bulkSendRecordLogMessageDefinition.getGroupId(),
				bulkSendRecordLogMessageDefinition.getBatchId(), bulkSendRecordLogMessageDefinition.getProcessId(),
				ProcessStatus.COMPLETED.toString(),
				Long.valueOf(bulkSendRecordLogMessageDefinition.getBulkSendRecordLogDefinitions().size()));

	}

	@Override
	protected void sendToDeadQueue(BulkSendRecordLogMessageDefinition bulkSendRecordLogMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
				bulkSendRecordLogMessageDefinition, errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				bulkSendRecordLogMessageDefinition, m -> {
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
			BulkSendRecordLogMessageDefinition bulkSendRecordLogMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing bulkSendRecordLogMessageDefinitionRequest for batchId -> {} and processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), bulkSendRecordLogMessageDefinition.getBatchId(),
				bulkSendRecordLogMessageDefinition.getProcessId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDRECORDLOG_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDRECORDLOG_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}