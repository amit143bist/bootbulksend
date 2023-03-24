package com.ds.proserv.report.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.feign.report.domain.ConcurrentReportCompleteMessageDefinition;
import com.ds.proserv.feign.report.domain.ConcurrentReportDataMessageDefinition;
import com.ds.proserv.report.consumer.client.CoreReportDataClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportDataListener extends AbstractMigrationListener<ConcurrentReportDataMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CoreReportDataClient coreReportDataClient;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConcurrentReportDataMessageDefinition received in processMessage() -> {} and xDeath value is {}",
				concurrentReportDataMessageDefinition, xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_RETRYLIMIT),
				concurrentReportDataMessageDefinition);

	}

	@Override
	protected void callService(ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition) {

		String primaryIds = coreReportDataClient.saveReportData(concurrentReportDataMessageDefinition).getBody();

		String pushToCompleteQueue = dsCacheManager.prepareAndRequestCacheDataByKey(
				PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_PUSH_TO_COMPLETE_QUEUE);
		if (!StringUtils.isEmpty(pushToCompleteQueue) && AppConstants.APP_TRUE.equalsIgnoreCase(pushToCompleteQueue)) {

			ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition = new ConcurrentReportCompleteMessageDefinition();

			concurrentReportCompleteMessageDefinition
					.setAccountId(concurrentReportDataMessageDefinition.getAccountId());
			concurrentReportCompleteMessageDefinition.setBatchId(concurrentReportDataMessageDefinition.getBatchId());
			concurrentReportCompleteMessageDefinition.setNextUri(concurrentReportDataMessageDefinition.getNextUri());
			concurrentReportCompleteMessageDefinition.setPrimaryIds(primaryIds);
			concurrentReportCompleteMessageDefinition
					.setProcessId(concurrentReportDataMessageDefinition.getProcessId());
			concurrentReportCompleteMessageDefinition.setGroupId(concurrentReportDataMessageDefinition.getGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_NAME,
					PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			rabbitTemplate.convertAndSend(queueName, concurrentReportCompleteMessageDefinition);
		} else {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
			concurrentProcessLogDefinition.setBatchId(concurrentReportDataMessageDefinition.getBatchId());
			concurrentProcessLogDefinition.setProcessId(concurrentReportDataMessageDefinition.getProcessId());
			concurrentProcessLogDefinition.setTotalRecordsInProcess(
					Long.valueOf(concurrentReportDataMessageDefinition.getReportRowsList().size()));
			concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());
			concurrentProcessLogDefinition.setGroupId(concurrentReportDataMessageDefinition.getGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			rabbitTemplate.convertAndSend(queueName, concurrentProcessLogDefinition);
		}

	}

	@Override
	protected void sendToDeadQueue(ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("Message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {} for processId {}",
				concurrentReportDataMessageDefinition, errorHeaderMessage,
				concurrentReportDataMessageDefinition.getProcessId());

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentReportDataMessageDefinition, m -> {
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
			ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} for batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentReportDataMessageDefinition.getProcessId(),
				concurrentReportDataMessageDefinition.getBatchId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}