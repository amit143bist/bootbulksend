package com.ds.proserv.applicationenvelope.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.applicationenvelope.consumer.client.ApplicationEnvelopeClient;
import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidMessageException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeInformation;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApplicationEnvelopeListener extends AbstractMigrationListener<ApplicationEnvelopeMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private ApplicationEnvelopeClient applicationEnvelopeClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("EnvelopeMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_RETRYLIMIT),
				applicationEnvelopeMessageDefinition);

	}

	@Override
	protected void callService(ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition) {

		if (null != applicationEnvelopeMessageDefinition.getApplicationEnvelopeDefinitions()
				&& !applicationEnvelopeMessageDefinition.getApplicationEnvelopeDefinitions().isEmpty()) {

			ApplicationEnvelopeInformation applicationEnvelopeInformation = new ApplicationEnvelopeInformation();
			applicationEnvelopeInformation.setApplicationEnvelopeDefinitions(
					applicationEnvelopeMessageDefinition.getApplicationEnvelopeDefinitions());

			applicationEnvelopeClient.bulkSave(applicationEnvelopeInformation);

			if (!StringUtils.isEmpty(applicationEnvelopeInformation.getProcessId())) {

				queueService.closeConcurrentProcess(applicationEnvelopeInformation.getGroupId(),
						applicationEnvelopeInformation.getBatchId(), applicationEnvelopeInformation.getProcessId(),
						ProcessStatus.COMPLETED.toString(),
						Long.valueOf(applicationEnvelopeMessageDefinition.getApplicationEnvelopeDefinitions().size()));
			}
		} else {

			log.error(
					"--------------------- INVALID MESSAGE, ApplicationEnvelopeDefinitions cannot be Null ---------------------");
			throw new InvalidMessageException("INVALID MESSAGE, ApplicationEnvelopeDefinitions cannot be Null");
		}
	}

	@Override
	protected void sendToDeadQueue(ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
				applicationEnvelopeMessageDefinition, errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				applicationEnvelopeMessageDefinition, m -> {
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
			ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing applicationEnvelopeMessageDefinitionRequest for processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), applicationEnvelopeMessageDefinition.getProcessId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}