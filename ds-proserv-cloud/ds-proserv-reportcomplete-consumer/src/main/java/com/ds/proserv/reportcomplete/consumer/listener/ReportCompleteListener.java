package com.ds.proserv.reportcomplete.consumer.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.feign.report.domain.ConcurrentReportCompleteMessageDefinition;
import com.ds.proserv.reportcomplete.consumer.client.CustomEnvelopeDataClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportCompleteListener extends AbstractMigrationListener<ConcurrentReportCompleteMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ReportCompleteRequest received in processMessage() -> {} and xDeath value is {}",
				concurrentReportCompleteMessageDefinition, xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_RETRYLIMIT),
				concurrentReportCompleteMessageDefinition);
	}

	@Override
	protected void callService(ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition) {

		CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String primaryIds = concurrentReportCompleteMessageDefinition.getPrimaryIds();

			PageInformation pageInformation = new PageInformation();
			PageQueryParam pageQueryParam = new PageQueryParam();
			pageQueryParam.setParamName(AppConstants.PRIMARYIDS_PARAM_NAME);
			pageQueryParam.setParamValue(primaryIds);

			List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
			pageQueryParamList.add(pageQueryParam);
			pageInformation.setPageQueryParams(pageQueryParamList);

			customEnvelopeDataClient.updateCustomEnvelopeDataProcessStatusEndTime(pageInformation);

			return primaryIds;
		}).thenApplyAsync(primaryIds -> {

			if (!StringUtils.isEmpty(concurrentReportCompleteMessageDefinition.getProcessId())) {

				List<String> primaryIdList = Stream.of(primaryIds.split(AppConstants.COMMA_DELIMITER))
						.collect(Collectors.toList());

				queueService.closeConcurrentProcess(concurrentReportCompleteMessageDefinition.getGroupId(),
						concurrentReportCompleteMessageDefinition.getBatchId(),
						concurrentReportCompleteMessageDefinition.getProcessId(), ProcessStatus.COMPLETED.toString(),
						Long.valueOf(primaryIdList.size()));
			}

			return AppConstants.SUCCESS_VALUE;
		}).handleAsync((result, exp) -> {

			if (null != exp) {

				exp.printStackTrace();
				sendToDeadQueue(concurrentReportCompleteMessageDefinition, HttpStatus.UNPROCESSABLE_ENTITY.toString(),
						exp.getMessage());
			} else {

				log.info(
						"No exception occurred in processing for primaryIds -> {} in processId -> {} and batchId -> {} ",
						concurrentReportCompleteMessageDefinition.getPrimaryIds(),
						concurrentReportCompleteMessageDefinition.getProcessId(),
						concurrentReportCompleteMessageDefinition.getBatchId());
			}

			return result;
		});

	}

	@Override
	protected void sendToDeadQueue(ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("Message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {} for processId {}",
				concurrentReportCompleteMessageDefinition, errorHeaderMessage,
				concurrentReportCompleteMessageDefinition.getProcessId());

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentReportCompleteMessageDefinition, m -> {
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
			ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing reportCompleteRequest for processId {} and batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentReportCompleteMessageDefinition.getProcessId(),
				concurrentReportCompleteMessageDefinition.getBatchId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}