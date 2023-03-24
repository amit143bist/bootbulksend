package com.ds.proserv.processcomplete.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.processcomplete.consumer.client.CoreConcurrentProcessLogClient;
import com.ds.proserv.processcomplete.consumer.client.CoreScheduledBatchLogClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessCompleteListener extends AbstractMigrationListener<ConcurrentProcessLogDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CoreScheduledBatchLogClient coreScheduledBatchLogClient;

	@Autowired
	private CoreConcurrentProcessLogClient coreConcurrentProcessLogClient;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentProcessLogDefinition concurrentProcessLogDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConcurrentProcessLogDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_RETRYLIMIT),
				concurrentProcessLogDefinition);

	}

	@Override
	protected void callService(ConcurrentProcessLogDefinition concurrentProcessLogDefinition) {

		log.info("Calling updateConcurrentProcess for processId -> {} and batchId {}",
				concurrentProcessLogDefinition.getProcessId(), concurrentProcessLogDefinition.getBatchId());

		ConcurrentProcessLogDefinition savedConcurrentProcessLogDefinition = coreConcurrentProcessLogClient
				.updateConcurrentProcess(concurrentProcessLogDefinition, concurrentProcessLogDefinition.getProcessId())
				.getBody();

		String groupId = concurrentProcessLogDefinition.getGroupId();
		if (StringUtils.isEmpty(groupId) && !StringUtils.isEmpty(savedConcurrentProcessLogDefinition.getGroupId())) {

			groupId = savedConcurrentProcessLogDefinition.getGroupId();
		}

		if (!StringUtils.isEmpty(groupId)) {

			ResponseEntity<Long> processCountInGroup = coreConcurrentProcessLogClient
					.countPendingConcurrentProcessInGroup(groupId);

			if (0 == processCountInGroup.getBody().intValue()) {

				Long totalRecordsInGroup = coreConcurrentProcessLogClient.countTotalRecordsInGroup(groupId).getBody();
				ConcurrentProcessLogDefinition concurrentProcessLogDefinitionGroup = new ConcurrentProcessLogDefinition();
				concurrentProcessLogDefinitionGroup.setProcessStatus(ProcessStatus.COMPLETED.toString());
				concurrentProcessLogDefinitionGroup.setTotalRecordsInProcess(totalRecordsInGroup);
				coreConcurrentProcessLogClient.updateConcurrentProcess(concurrentProcessLogDefinitionGroup, groupId)
						.getBody();
			}
		} else {

			log.warn("groupId is null or empty for processId -> {} and batchId {}",
					concurrentProcessLogDefinition.getProcessId(), concurrentProcessLogDefinition.getBatchId());
		}

		ResponseEntity<Long> processCountInBatch = coreConcurrentProcessLogClient
				.countPendingConcurrentProcessInBatch(concurrentProcessLogDefinition.getBatchId());

		if (0 == processCountInBatch.getBody().intValue()) {

			Long totalRecordsInBatch = coreConcurrentProcessLogClient
					.countTotalRecordsInBatch(concurrentProcessLogDefinition.getBatchId()).getBody();
			coreScheduledBatchLogClient.updateBatch(concurrentProcessLogDefinition.getBatchId(), totalRecordsInBatch);
		}
	}

	@Override
	protected void sendToDeadQueue(ConcurrentProcessLogDefinition concurrentProcessLogDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", concurrentProcessLogDefinition,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentProcessLogDefinition, m -> {
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
			ConcurrentProcessLogDefinition concurrentProcessLogDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} and batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentProcessLogDefinition.getProcessId(),
				concurrentProcessLogDefinition.getBatchId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}