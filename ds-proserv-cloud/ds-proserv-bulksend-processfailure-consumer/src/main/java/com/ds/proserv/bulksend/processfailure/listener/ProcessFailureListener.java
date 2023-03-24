package com.ds.proserv.bulksend.processfailure.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.bulksend.processfailure.client.BulkSendDataSourceClient;
import com.ds.proserv.bulksend.processfailure.client.BulkSendFailureLogClient;
import com.ds.proserv.bulksend.processfailure.client.BulkSendProcessLogClient;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessFailureMessageDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProcessFailureListener extends AbstractMigrationListener<BulkSendProcessFailureMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private BulkSendProcessLogClient bulkSendProcessLogClient;

	@Autowired
	private BulkSendFailureLogClient bulkSendFailureLogClient;

	@Autowired
	private BulkSendDataSourceClient bulkSendDataSourceClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("BulkSendProcessFailureMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_RETRYLIMIT),
				bulkSendProcessFailureMessageDefinition);
	}

	@Override
	protected void callService(BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition) {

		if (null != bulkSendProcessFailureMessageDefinition.getBulkSendFailureLogDefinition()) {

			if (!StringUtils.isEmpty(bulkSendProcessFailureMessageDefinition.getUpdateRecordDataQueryType())) {

				PageInformation pageInformation = preparePageInformation(bulkSendProcessFailureMessageDefinition,
						AppConstants.FAILURE_VALUE);
				bulkSendDataSourceClient.updateBulkSendSelectedRows(pageInformation);
			}

			if (log.isDebugEnabled()) {

				log.debug("Calling BulkSendFailureLogSave for recordIds -> {} in processId -> {}",
						bulkSendProcessFailureMessageDefinition.getRecordIds(),
						bulkSendProcessFailureMessageDefinition.getProcessId());
			}
			bulkSendFailureLogClient
					.saveBulkSendFailure(bulkSendProcessFailureMessageDefinition.getBulkSendFailureLogDefinition());

			List<String> applicationIds = DSUtil.getFieldsAsList(
					bulkSendProcessFailureMessageDefinition.getBulkSendFailureLogDefinition().getApplicationIds());
			queueService.closeConcurrentProcess(bulkSendProcessFailureMessageDefinition.getGroupId(),
					bulkSendProcessFailureMessageDefinition.getBatchId(),
					bulkSendProcessFailureMessageDefinition.getProcessId(), ProcessStatus.FAILED.toString(),
					Long.valueOf(applicationIds.size()));
		} else {

			if (!StringUtils.isEmpty(bulkSendProcessFailureMessageDefinition.getUpdateRecordDataQueryType())) {

				PageInformation pageInformation = preparePageInformation(bulkSendProcessFailureMessageDefinition,
						AppConstants.SUCCESS_VALUE);
				bulkSendDataSourceClient.updateBulkSendSelectedRows(pageInformation);
			}

			if (log.isDebugEnabled()) {

				log.debug("Calling BulkSendProcessLogSave for recordIds -> {} in processId -> {}",
						bulkSendProcessFailureMessageDefinition.getRecordIds(),
						bulkSendProcessFailureMessageDefinition.getProcessId());
			}
			bulkSendProcessLogClient
					.saveBulkSendProcessLog(bulkSendProcessFailureMessageDefinition.getBulkSendProcessLogDefinition());

			sendToBulkSendRecordLogQueue(bulkSendProcessFailureMessageDefinition);
		}
	}

	private PageInformation preparePageInformation(
			BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition, String processStatus) {

		if (log.isDebugEnabled()) {

			log.debug("Updating recordIds -> {} with processStatus -> {} in processId -> {}",
					bulkSendProcessFailureMessageDefinition.getRecordIds(), processStatus,
					bulkSendProcessFailureMessageDefinition.getProcessId());
		}

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue(bulkSendProcessFailureMessageDefinition.getRecordType());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue(bulkSendProcessFailureMessageDefinition.getUpdateRecordDataQueryType());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_PROCESS_STATUS);
		pageQueryParam.setParamValue(processStatus);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam
				.setParamName(bulkSendProcessFailureMessageDefinition.getUpdateRecordDataQueryTypePrimaryKeyName());

		List<String> recordIds = bulkSendProcessFailureMessageDefinition.getRecordIds();
		String commaSeparatedStr = String.join(AppConstants.COMMA_DELIMITER, recordIds);

		pageQueryParam.setParamValue(commaSeparatedStr);
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}

	private void sendToBulkSendRecordLogQueue(
			BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition) {

		List<String> recordIds = bulkSendProcessFailureMessageDefinition.getRecordIds();

		if (log.isDebugEnabled()) {

			log.debug("Inside sendToBulkSendRecordLogQueue for recordIds -> {} in processId -> {}", recordIds,
					bulkSendProcessFailureMessageDefinition.getProcessId());
		}

		List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>(
				recordIds.size());
		recordIds.forEach(recordId -> {

			BulkSendRecordLogDefinition bulkSendRecordLogDefinition = new BulkSendRecordLogDefinition();
			bulkSendRecordLogDefinition.setBulkBatchId(
					bulkSendProcessFailureMessageDefinition.getBulkSendProcessLogDefinition().getBatchId());
			bulkSendRecordLogDefinition.setStartDateTime(bulkSendProcessFailureMessageDefinition.getStartDateTime());
			bulkSendRecordLogDefinition.setEndDateTime(bulkSendProcessFailureMessageDefinition.getEndDateTime());
			bulkSendRecordLogDefinition.setRecordId(recordId);
			bulkSendRecordLogDefinition.setRecordType(bulkSendProcessFailureMessageDefinition.getRecordType());

			bulkSendRecordLogDefinitions.add(bulkSendRecordLogDefinition);

		});

		BulkSendRecordLogMessageDefinition bulkSendRecordLogMessageDefinition = new BulkSendRecordLogMessageDefinition();
		bulkSendRecordLogMessageDefinition.setBulkSendRecordLogDefinitions(bulkSendRecordLogDefinitions);
		bulkSendRecordLogMessageDefinition.setBatchId(bulkSendProcessFailureMessageDefinition.getBatchId());
		bulkSendRecordLogMessageDefinition.setProcessId(bulkSendProcessFailureMessageDefinition.getProcessId());
		bulkSendRecordLogMessageDefinition.setGroupId(bulkSendProcessFailureMessageDefinition.getGroupId());

		queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_BULKSENDRECORDLOG_QUEUE_NAME,
				bulkSendRecordLogMessageDefinition);
	}

	@Override
	protected void sendToDeadQueue(BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition,
			String httpStatus, String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
				bulkSendProcessFailureMessageDefinition, errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				bulkSendProcessFailureMessageDefinition, m -> {
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
			BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing bulkSendProcessFailureMessageRequest for batchId -> {} and processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), bulkSendProcessFailureMessageDefinition.getBatchId(),
				bulkSendProcessFailureMessageDefinition.getProcessId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}