package com.ds.proserv.bulksendenvelopelog.consumer.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.bulksendenvelopelog.consumer.client.BulkSendEnvelopeLogClient;
import com.ds.proserv.bulksendenvelopelog.consumer.client.DSCustomFieldClient;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogInformation;
import com.ds.proserv.feign.connect.domain.EnvelopeMessageDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldInformation;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSendEnvelopeLogListener extends AbstractMigrationListener<EnvelopeMessageDefinition> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private QueueService queueService;

	@Autowired
	private DSCustomFieldClient dsCustomFieldClient;

	@Autowired
	private BulkSendEnvelopeLogClient bulkSendEnvelopeLogClient;

	@Override
	@RabbitListener(queues = "#{@getQueueName}")
	protected void processMessage(EnvelopeMessageDefinition envelopeMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("EnvelopeMessageDefinition received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDENVELOPELOG_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDENVELOPELOG_QUEUE_RETRYLIMIT),
				envelopeMessageDefinition);
	}

	@Override
	protected void callService(EnvelopeMessageDefinition envelopeMessageDefinition) {

		List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList = new ArrayList<BulkSendEnvelopeLogDefinition>();
		if (null != envelopeMessageDefinition.getDsEnvelopeDefinition()) {

			filterBulkBatchIdECF(envelopeMessageDefinition, bulkSendEnvelopeLogDefinitionList);

		} else {

			getAllBulkBatchIdECFEnvelopes(envelopeMessageDefinition, bulkSendEnvelopeLogDefinitionList);
		}

		if (null != bulkSendEnvelopeLogDefinitionList && !bulkSendEnvelopeLogDefinitionList.isEmpty()) {

			log.info("Total Records to be saved in processId -> {} and batchId -> {} is {}",
					envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId(),
					bulkSendEnvelopeLogDefinitionList.size());
			BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation = new BulkSendEnvelopeLogInformation();
			bulkSendEnvelopeLogInformation.setBulkSendEnvelopeLogDefinitions(bulkSendEnvelopeLogDefinitionList);

			bulkSendEnvelopeLogClient.bulkSaveBulkSendEnvelopeLog(bulkSendEnvelopeLogInformation);

			if (!StringUtils.isEmpty(envelopeMessageDefinition.getProcessId())) {

				queueService.closeConcurrentProcess(envelopeMessageDefinition.getGroupId(),
						envelopeMessageDefinition.getBatchId(), envelopeMessageDefinition.getProcessId(),
						ProcessStatus.COMPLETED.toString(), Long.valueOf(bulkSendEnvelopeLogDefinitionList.size()));
			}
		} else {

			log.warn(
					"-------------------- No Records to save for ECF -> {} in processId -> {} and batchId -> {} --------------------",
					AppConstants.ECFNAME_BULKBATCHID, envelopeMessageDefinition.getProcessId(),
					envelopeMessageDefinition.getBatchId());
		}

	}

	private void filterBulkBatchIdECF(EnvelopeMessageDefinition envelopeMessageDefinition,
			List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList) {

		List<DSCustomFieldDefinition> dsCustomFieldDefinitions = envelopeMessageDefinition.getDsEnvelopeDefinition()
				.getDsCustomFieldDefinitions();
		if (null != dsCustomFieldDefinitions && !dsCustomFieldDefinitions.isEmpty()) {

			List<DSCustomFieldDefinition> bulkBatchCustomFieldDefinitions = dsCustomFieldDefinitions.stream()
					.filter(dsCustomFieldDefinition -> AppConstants.ECFNAME_BULKBATCHID
							.equalsIgnoreCase(dsCustomFieldDefinition.getFieldName()))
					.collect(Collectors.toList());

			if (null != bulkBatchCustomFieldDefinitions && !bulkBatchCustomFieldDefinitions.isEmpty()) {

				BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition = new BulkSendEnvelopeLogDefinition();
				bulkSendEnvelopeLogDefinition.setBulkBatchId(bulkBatchCustomFieldDefinitions.get(0).getFieldValue());
				bulkSendEnvelopeLogDefinition
						.setEnvelopeId(envelopeMessageDefinition.getDsEnvelopeDefinition().getEnvelopeId());

				bulkSendEnvelopeLogDefinitionList.add(bulkSendEnvelopeLogDefinition);
			}
		}
	}

	private void getAllBulkBatchIdECFEnvelopes(EnvelopeMessageDefinition envelopeMessageDefinition,
			List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList) {

		List<String> recordIds = envelopeMessageDefinition.getRecordIds();

		DSCustomFieldInformation dsCustomFieldInformation = dsCustomFieldClient
				.findCustomFieldsByEnvelopeIdsAndFieldName(preparePageInformation(recordIds)).getBody();

		if (null != dsCustomFieldInformation) {

			List<DSCustomFieldDefinition> dsCustomFieldDefinitions = dsCustomFieldInformation
					.getDsCustomFieldDefinitions();

			if (null != dsCustomFieldDefinitions && !dsCustomFieldDefinitions.isEmpty()) {

				dsCustomFieldDefinitions.forEach(dsCustomFieldDefinition -> {

					BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition = new BulkSendEnvelopeLogDefinition();
					bulkSendEnvelopeLogDefinition.setBulkBatchId(dsCustomFieldDefinition.getFieldValue());
					bulkSendEnvelopeLogDefinition.setEnvelopeId(dsCustomFieldDefinition.getEnvelopeId());

					bulkSendEnvelopeLogDefinitionList.add(bulkSendEnvelopeLogDefinition);
				});
			}
		} else {

			log.warn(
					"-------------------- No Records to fetched for ECF -> {} in processId -> {} and batchId -> {} --------------------",
					AppConstants.ECFNAME_BULKBATCHID, envelopeMessageDefinition.getProcessId(),
					envelopeMessageDefinition.getBatchId());
		}
	}

	private PageInformation preparePageInformation(List<String> envelopeIdList) {

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(String.join(AppConstants.COMMA_DELIMITER, envelopeIdList));

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.CUSTOMFIELD_PARAM_NAME);
		pageQueryParam.setParamValue(AppConstants.ECFNAME_BULKBATCHID);
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
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
				"{} is thrown and exception message is {} in processing bulkSendEnvelopeLogRequest for batchId -> {} and processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), envelopeMessageDefinition.getBatchId(), envelopeMessageDefinition.getProcessId(),
				retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_BULKSENDENVELOPELOG_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_BULKSENDENVELOPELOG_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}