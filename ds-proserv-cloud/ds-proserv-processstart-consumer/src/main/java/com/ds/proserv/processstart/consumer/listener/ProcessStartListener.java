package com.ds.proserv.processstart.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.feign.report.domain.ConcurrentProcessMessageDefinition;
import com.ds.proserv.feign.report.domain.ConcurrentReportDataMessageDefinition;
import com.ds.proserv.report.prepare.service.CommonServiceData;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessStartListener extends AbstractMigrationListener<ConcurrentProcessMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CommonServiceData commonServiceData;

	private Configuration docContextPathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	private Configuration pathConfiguration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS,
			Option.AS_PATH_LIST, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentProcessMessageDefinition concurrentProcessMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("ConcurrentProcessMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager
								.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_START_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_START_QUEUE_RETRYLIMIT),
				concurrentProcessMessageDefinition);

	}

	@Override
	protected void callService(ConcurrentProcessMessageDefinition concurrentProcessMessageDefinition) {

		DocumentContext paginationDocContext = JsonPath.using(docContextPathConfiguration)
				.parse(concurrentProcessMessageDefinition.getPaginationJson());

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = commonServiceData.processEachPageJSONResponse(
				concurrentProcessMessageDefinition.getAccountId(), concurrentProcessMessageDefinition.getInputParams(),
				concurrentProcessMessageDefinition.getPrepareAPI(),
				concurrentProcessMessageDefinition.getPaginationJson(), pathConfiguration, paginationDocContext,
				concurrentProcessMessageDefinition.getTableColumnMetaData(),
				concurrentProcessMessageDefinition.getBatchId(), concurrentProcessMessageDefinition.getNextUri(),
				concurrentProcessMessageDefinition.getParentGroupId(), concurrentProcessMessageDefinition.getUserId(),
				concurrentProcessMessageDefinition.getProcessId());

		if (null != concurrentProcessLogDefinition.getReportRowsList()) {

			ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition = new ConcurrentReportDataMessageDefinition();

			concurrentReportDataMessageDefinition.setAccountId(concurrentProcessMessageDefinition.getAccountId());
			concurrentReportDataMessageDefinition.setBatchId(concurrentProcessMessageDefinition.getBatchId());
			concurrentReportDataMessageDefinition.setNextUri(concurrentProcessMessageDefinition.getNextUri());
			concurrentReportDataMessageDefinition.setProcessId(concurrentProcessMessageDefinition.getProcessId());
			concurrentReportDataMessageDefinition
					.setTableColumnMetaData(concurrentProcessMessageDefinition.getTableColumnMetaData());
			concurrentReportDataMessageDefinition
					.setPrimaryId(concurrentProcessMessageDefinition.getPrepareAPI().getOutputApiPrimaryId());

			concurrentReportDataMessageDefinition.setReportRowsList(concurrentProcessLogDefinition.getReportRowsList());
			concurrentReportDataMessageDefinition.setGroupId(concurrentProcessMessageDefinition.getParentGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_REPORTDATA_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			log.info("Sending processId -> {} of totalRecords -> {} to queue -> {}",
					concurrentProcessMessageDefinition.getProcessId(),
					concurrentProcessLogDefinition.getReportRowsList().size(), queueName);

			rabbitTemplate.convertAndSend(queueName, concurrentReportDataMessageDefinition);
		} else {

			log.warn("ReportRowsList is empty for processId -> {}", concurrentProcessMessageDefinition.getProcessId());
			concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETEDWITHNORECORDS.toString());
			concurrentProcessLogDefinition.setGroupId(concurrentProcessMessageDefinition.getParentGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			log.info("Sending processId -> {} of totalRecords -> {} to queue -> {}",
					concurrentProcessMessageDefinition.getProcessId(), "0 or null", queueName);
			rabbitTemplate.convertAndSend(queueName, concurrentProcessLogDefinition);
		}

	}

	@Override
	protected void sendToDeadQueue(ConcurrentProcessMessageDefinition concurrentProcessMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("Message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {} for processId {}",
				concurrentProcessMessageDefinition, errorHeaderMessage,
				concurrentProcessMessageDefinition.getProcessId());

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentProcessMessageDefinition, m -> {
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
			ConcurrentProcessMessageDefinition concurrentProcessMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} for batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentProcessMessageDefinition.getProcessId(),
				concurrentProcessMessageDefinition.getBatchId(), retryCount,
				getRetryLimit(
						dsCacheManager
								.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_START_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_START_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}