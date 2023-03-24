package com.ds.proserv.docmigration.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.broker.config.service.ThrottleService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.docmigration.consumer.service.AppDataService;
import com.ds.proserv.docmigration.consumer.service.MigrationRowDataService;
import com.ds.proserv.feign.appdata.domain.MigrationDataDefinition;
import com.ds.proserv.feign.connect.domain.EnvelopeMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocMigrationListener extends AbstractMigrationListener<EnvelopeMessageDefinition> {

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private AppDataService appDataService;

	@Autowired
	private ThrottleService throttleService;

	@Autowired
	private MigrationRowDataService migrationDataService;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(EnvelopeMessageDefinition envelopeMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("DocMigrationRequest received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_DOCMIGRATION_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_DOCMIGRATION_QUEUE_RETRYLIMIT),
				envelopeMessageDefinition);

		throttleService.throttleConsumers();
	}

	@Override
	protected void callService(EnvelopeMessageDefinition envelopeMessageDefinition) {

		// Save Data in doc migration related table
		CompletableFuture<MigrationDataDefinition> migrationDataDefinitionFuture = migrationDataService
				.prepareMigrationDataSet(envelopeMessageDefinition);

		migrationDataDefinitionFuture.thenApplyAsync((migrationDataDefinition) -> {

			if (null != migrationDataDefinition.getRowDataMapList()
					&& !migrationDataDefinition.getRowDataMapList().isEmpty()) {

				// Save Data in CustomEnvelopeData table
				try {

					appDataService.saveCustomEnvelopeData(envelopeMessageDefinition, migrationDataDefinition);
				} catch (Exception exp) {

					log.error("Exception -> {} occurred in callService", exp);
					exp.printStackTrace();
					sendToDeadQueue(envelopeMessageDefinition, HttpStatus.UNPROCESSABLE_ENTITY.toString(),
							exp.getMessage());
				}
			} else if (!StringUtils.isEmpty(envelopeMessageDefinition.getProcessId())) {

				CompletableFuture.runAsync(() -> {

					log.warn("There is nothing to migrate for processId -> {} so sending message to close the process",
							envelopeMessageDefinition.getProcessId());
					queueService.closeConcurrentProcess(envelopeMessageDefinition.getGroupId(),
							envelopeMessageDefinition.getBatchId(), envelopeMessageDefinition.getProcessId(),
							ProcessStatus.COMPLETED.toString(), 0L);
				}, recordTaskExecutor);
			}

			return AppConstants.SUCCESS_VALUE;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != asyncStatus) {

				appDataService.handleStatus(asyncStatus.toString(), exp, "callService",
						envelopeMessageDefinition.getProcessId());
			} else {

				log.info("asyncStatus is null in callService for processId -> {} and recordIds -> {}",
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getRecordIds());
			}

			if (null != exp
					|| (null != asyncStatus && AppConstants.FAILURE_VALUE.equalsIgnoreCase(asyncStatus.toString()))) {

				exp.printStackTrace();
				sendToDeadQueue(envelopeMessageDefinition, HttpStatus.UNPROCESSABLE_ENTITY.toString(),
						exp.getMessage());
			}

			return asyncStatus;
		});

	}

	@Override
	protected void sendToDeadQueue(EnvelopeMessageDefinition envelopeMessageDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", envelopeMessageDefinition,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				envelopeMessageDefinition, m -> {
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
			EnvelopeMessageDefinition envelopeMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing docMigrationRequest for batchId -> {} and processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), envelopeMessageDefinition.getBatchId(), envelopeMessageDefinition.getProcessId(),
				retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_DOCMIGRATION_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_DOCMIGRATION_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}