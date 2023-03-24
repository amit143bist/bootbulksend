package com.ds.proserv.bulksend.consumer.listener;

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
import com.ds.proserv.bulksend.common.client.BulkSendDataSourceClient;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchItem;
import com.ds.proserv.bulksend.common.service.BulkSendService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.BatchStatus;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidMessageException;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.exception.NoDataProcessingException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendMessageDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessFailureMessageDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSendListener extends AbstractMigrationListener<BulkSendMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private BulkSendService bulkSendService;

	@Autowired
	private BulkSendDataSourceClient bulkSendDataSourceClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(BulkSendMessageDefinition bulkSendMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("BulkSendMessageDefinition received in processMessage() -> {} and xDeath value is {}",
				bulkSendMessageDefinition, xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSEND_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSEND_QUEUE_RETRYLIMIT),
				bulkSendMessageDefinition);
	}

	@Override
	protected void callService(BulkSendMessageDefinition bulkSendMessageDefinition) {

		try {

			if (!StringUtils.isEmpty(bulkSendMessageDefinition.getUpdateRecordDataQueryType())) {

				PageInformation pageInformation = preparePageInformation(bulkSendMessageDefinition);
				bulkSendDataSourceClient.updateBulkSendSelectedRows(pageInformation);
			}

			EnvelopeBatchItem envelopeBatchProcessed = bulkSendService
					.processBulkSendMessage(bulkSendMessageDefinition);

			if (envelopeBatchProcessed.getSuccess()) {

				saveBulkSendProcessLog(bulkSendMessageDefinition, envelopeBatchProcessed);

			} else {

				saveBulkSendFailureLog(bulkSendMessageDefinition, envelopeBatchProcessed);
				List<String> errorDetails = envelopeBatchProcessed.getErrorDetails();
				if (null != errorDetails && !errorDetails.isEmpty()) {

					String errorDetailAsStr = String.join(AppConstants.COMMA_DELIMITER, errorDetails);

					log.error("{} occurred for queryIdentifier {} in calling DSBulkSendAPI for processId -> {}",
							errorDetailAsStr, bulkSendMessageDefinition.getQueryIdentifier(),
							bulkSendMessageDefinition.getProcessId());
					throw new InvalidMessageException(
							errorDetailAsStr + " occurred for queryIdentifier "
									+ bulkSendMessageDefinition.getQueryIdentifier() + " in calling DSBulkSendAPI ",
							FailureCode.ERROR_222, FailureStep.ASYNC_BULKSEND_DSAPI);
				} else {

					ListenerProcessingException listenerProcessingException = new ListenerProcessingException(
							FailureCode.ERROR_112.toString() + " " + FailureStep.BULK_SEND_RECORDS.toString());
					queueService.createFailureMessageAndSend(bulkSendMessageDefinition.getQueryIdentifier(),
							bulkSendMessageDefinition.getBatchId(), bulkSendMessageDefinition.getProcessId(),
							listenerProcessingException, FailureCode.ERROR_112, FailureStep.BULK_SEND_RECORDS);

					throw listenerProcessingException;
				}

			}

		} catch (InvalidMessageException exp) {

			queueService.createFailureMessageAndSend(bulkSendMessageDefinition.getQueryIdentifier(),
					bulkSendMessageDefinition.getBatchId(), bulkSendMessageDefinition.getProcessId(), exp,
					exp.getFailureCode(), exp.getFailureStep());
			throw exp;
		} catch (NoDataProcessingException exp) {

			queueService.closeConcurrentProcess(bulkSendMessageDefinition.getGroupId(),
					bulkSendMessageDefinition.getBatchId(), bulkSendMessageDefinition.getProcessId(),
					ProcessStatus.COMPLETEDWITHNORECORDS.toString(), Long.valueOf(0L));
		}
	}

	private PageInformation preparePageInformation(BulkSendMessageDefinition bulkSendMessageDefinition) {

		if (log.isDebugEnabled()) {

			log.debug("Updating recordIds -> {} with processStatus -> INPROGRESS in processId -> {}",
					bulkSendMessageDefinition.getRecordIds(), bulkSendMessageDefinition.getProcessId());
		}

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue(bulkSendMessageDefinition.getQueryIdentifier());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue(bulkSendMessageDefinition.getUpdateRecordDataQueryType());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_PROCESS_STATUS);
		pageQueryParam.setParamValue(AppConstants.IN_PROGRESS);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(bulkSendMessageDefinition.getUpdateRecordDataQueryTypePrimaryKeyName());

		List<String> recordIds = bulkSendMessageDefinition.getRecordIds();
		String commaSeparatedStr = String.join(AppConstants.COMMA_DELIMITER, recordIds);

		pageQueryParam.setParamValue(commaSeparatedStr);
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}

	private void saveBulkSendFailureLog(BulkSendMessageDefinition bulkSendMessageDefinition,
			EnvelopeBatchItem envelopeBatchProcessed) {

		log.info("Saving failure for processId -> {}", bulkSendMessageDefinition.getProcessId());

		BulkSendFailureLogDefinition bulkSendFailureLogDefinition = new BulkSendFailureLogDefinition();

		if (null != envelopeBatchProcessed.getTotalRecordIdsProcessed()) {

			bulkSendFailureLogDefinition.setBatchSize(envelopeBatchProcessed.getTotalRecordIdsProcessed());
		} else {

			log.error(
					"Total RecordIds processed cannot be NULL at this stage for BulkSendFailureLog in processId -> {}",
					bulkSendMessageDefinition.getProcessId());
		}

		if (null != envelopeBatchProcessed.getErrorDetails() && !envelopeBatchProcessed.getErrorDetails().isEmpty()) {

			String errorDetails = String.join(AppConstants.COMMA_DELIMITER, envelopeBatchProcessed.getErrorDetails());
			bulkSendFailureLogDefinition.setErrorMessage(errorDetails);
		}

		if (null != envelopeBatchProcessed.getCommaSeparatedRecordIds()) {

			bulkSendFailureLogDefinition.setApplicationIds(
					bulkSendMessageDefinition.getProcessId() + "_" + bulkSendMessageDefinition.getQueryIdentifier()
							+ "_" + envelopeBatchProcessed.getCommaSeparatedRecordIds());
		} else {

			log.error("ApplicationIds processed cannot be NULL at this stage for processId -> {}",
					bulkSendMessageDefinition.getProcessId());
		}

		bulkSendFailureLogDefinition.setBatchFailureDateTime(LocalDateTime.now().toString());

		sentToBulkSendProcessFailureQueue(bulkSendMessageDefinition, bulkSendFailureLogDefinition, null,
				envelopeBatchProcessed);

	}

	private void saveBulkSendProcessLog(BulkSendMessageDefinition bulkSendMessageDefinition,
			EnvelopeBatchItem envelopeBatchProcessed) {

		log.info("Saving success for processId -> {}", bulkSendMessageDefinition.getProcessId());

		BulkSendProcessLogDefinition bulkSendProcessLogDefinition = new BulkSendProcessLogDefinition();
		bulkSendProcessLogDefinition.setMailingListId(envelopeBatchProcessed.getListId());
		bulkSendProcessLogDefinition.setBatchId(envelopeBatchProcessed.getBatchId());
		bulkSendProcessLogDefinition.setBatchName(envelopeBatchProcessed.getBatchName());

		if (null != envelopeBatchProcessed.getTotalRecordIdsProcessed()) {

			bulkSendProcessLogDefinition.setBatchSize(envelopeBatchProcessed.getTotalRecordIdsProcessed());
		} else {

			log.error(
					"Total RecordIds processed cannot be NULL at this stage for BulkSendProcessLog in processId -> {}",
					bulkSendMessageDefinition.getProcessId());
		}

		if (!StringUtils.isEmpty(envelopeBatchProcessed.getTotalQueued())) {

			bulkSendProcessLogDefinition.setQueueSize(Long.valueOf(envelopeBatchProcessed.getTotalQueued()));
		}

		if (!StringUtils.isEmpty(envelopeBatchProcessed.getTotalFailed())) {

			bulkSendProcessLogDefinition.setFailedSize(Long.valueOf(envelopeBatchProcessed.getTotalFailed()));
		}

		if (!StringUtils.isEmpty(envelopeBatchProcessed.getTotalSent())) {

			bulkSendProcessLogDefinition.setSuccessSize(Long.valueOf(envelopeBatchProcessed.getTotalSent()));
		}

		bulkSendProcessLogDefinition.setBatchSubmittedDateTime(LocalDateTime.now().toString());
		bulkSendProcessLogDefinition.setBatchStatus(BatchStatus.SUBMITTED.toString());

		sentToBulkSendProcessFailureQueue(bulkSendMessageDefinition, null, bulkSendProcessLogDefinition,
				envelopeBatchProcessed);

	}

	private void sentToBulkSendProcessFailureQueue(BulkSendMessageDefinition bulkSendMessageDefinition,
			BulkSendFailureLogDefinition bulkSendFailureLogDefinition,
			BulkSendProcessLogDefinition bulkSendProcessLogDefinition, EnvelopeBatchItem envelopeBatchProcessed) {

		BulkSendProcessFailureMessageDefinition bulkSendProcessFailureMessageDefinition = new BulkSendProcessFailureMessageDefinition();
		bulkSendProcessFailureMessageDefinition.setBatchId(bulkSendMessageDefinition.getBatchId());

		if (null != bulkSendFailureLogDefinition) {

			log.info("Sending to {} queue with failureMessage for processId ->, batchId -> {} and recordIds -> {}",
					PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_NAME,
					bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId(),
					envelopeBatchProcessed.getCommaSeparatedRecordIds());
			bulkSendProcessFailureMessageDefinition.setBulkSendFailureLogDefinition(bulkSendFailureLogDefinition);
		}

		if (null != bulkSendProcessLogDefinition) {

			log.info(
					"Sending to {} queue with successful processMessage for processId ->, batchId -> {} and recordIds -> {}",
					PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_NAME,
					bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId(),
					envelopeBatchProcessed.getCommaSeparatedRecordIds());
			bulkSendProcessFailureMessageDefinition.setBulkSendProcessLogDefinition(bulkSendProcessLogDefinition);
		}

		bulkSendProcessFailureMessageDefinition.setGroupId(bulkSendMessageDefinition.getGroupId());
		bulkSendProcessFailureMessageDefinition.setProcessId(bulkSendMessageDefinition.getProcessId());

		if (null != envelopeBatchProcessed.getCommaSeparatedRecordIds()) {

			List<String> recordIdList = DSUtil.getFieldsAsList(envelopeBatchProcessed.getCommaSeparatedRecordIds());
			bulkSendProcessFailureMessageDefinition.setRecordIds(recordIdList);
		} else {

			log.error("RecordIds processed cannot be NULL at this stage for processId -> {}",
					bulkSendMessageDefinition.getProcessId());
		}

		bulkSendProcessFailureMessageDefinition.setRecordType(bulkSendMessageDefinition.getQueryIdentifier());
		bulkSendProcessFailureMessageDefinition.setStartDateTime(bulkSendMessageDefinition.getStartDateTime());
		bulkSendProcessFailureMessageDefinition.setEndDateTime(bulkSendMessageDefinition.getEndDateTime());

		bulkSendProcessFailureMessageDefinition
				.setUpdateRecordDataQueryType(bulkSendMessageDefinition.getUpdateRecordDataQueryType());
		bulkSendProcessFailureMessageDefinition.setUpdateRecordDataQueryTypePrimaryKeyName(
				bulkSendMessageDefinition.getUpdateRecordDataQueryTypePrimaryKeyName());

		queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_BULKSENDPROCESSFAILURE_QUEUE_NAME,
				bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId(),
				bulkSendProcessFailureMessageDefinition);
	}

	@Override
	protected void sendToDeadQueue(BulkSendMessageDefinition bulkSendMessageDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", bulkSendMessageDefinition,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				bulkSendMessageDefinition, m -> {
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
			BulkSendMessageDefinition bulkSendMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing processId {} and batchId {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId(),
				retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSEND_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSEND_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}