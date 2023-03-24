package com.ds.proserv.connect.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.connect.domain.EnvelopeStatusCode;
import com.ds.proserv.connect.factory.ConnectProcessorFactory;
import com.ds.proserv.connect.helper.ConnectHelper;
import com.ds.proserv.connect.processor.IConnectProcessor;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeMessageDefinition;
import com.ds.proserv.feign.connect.domain.EnvelopeMessageDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectDownstreamQueueService {

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ConnectHelper connectHelper;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private ConnectProcessorFactory connectProcessorFactory;

	public CompletableFuture<String> sendToDownstreamQueues(
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String processId, String batchId) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;

			if (null != toProcessDSEnvelopeDefinitionList && !toProcessDSEnvelopeDefinitionList.isEmpty()) {

				// Like CORE_PARALLEL_DOC_MIGRATION_QUEUE, BULKSEND_ENVELOPELOG_QUEUE,
				// APPLICATION_ENVELOPE_QUEUE
				String downstreamQueueNames = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CONNECT_DOWNSTREAM_QUEUE_NAMES);

				if (!StringUtils.isEmpty(downstreamQueueNames)) {// To send to downstream queues

					String migrationQueueName = docMigrationQueueName();
					List<String> queueNames = DSUtil.getFieldsAsList(downstreamQueueNames);

					queueNames.parallelStream().forEach(queueName -> {

						log.debug("Processing for queueName -> {} for processId -> {}", queueName, processId);
						prepareMessageAndSendToQueue(toProcessDSEnvelopeDefinitionList, processId, batchId, queueName,
								migrationQueueName);

					});
				} else {// Nothing to Send

					log.info("^^^^^^^^^^^^^^^^^^^^ No downstream queues configured ^^^^^^^^^^^^^^^^^^^^");
				}

			} else {

				log.error("-------------------- NO ENVELOPES ARE PROCESSED AND SAVED --------------------");
			}
			return asyncStatus;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			connectHelper.handleAsyncStatus(asyncStatus, exp, "sendToDownstreamQueuesInDownstreamQueueService",
					processId);
			return asyncStatus;
		});
	}

	private CompletableFuture<String> prepareMessageAndSendToQueue(
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String processId, String batchId,
			String queueName, String migrationQueueName) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;

			if (!StringUtils.isEmpty(migrationQueueName) && queueName.equalsIgnoreCase(migrationQueueName)) {

				List<String> envStatusesAvailableForDocMigration = getEnvStatusesAvailableForDocMigration();
				List<DSEnvelopeDefinition> toMigrateList = toProcessDSEnvelopeDefinitionList.stream()
						.filter(toProcessDSEnvelopeDefinition -> envStatusesAvailableForDocMigration
								.contains(toProcessDSEnvelopeDefinition.getStatus().toUpperCase()))
						.collect(Collectors.toList());

				if (null != toMigrateList && !toMigrateList.isEmpty()) {

					log.info("Original Size is {} and new migrateList size is {} for processId -> {} and batchId -> {}",
							toProcessDSEnvelopeDefinitionList.size(), toMigrateList.size(), processId, batchId);

					List<String> customerProcessors = findCustomerProcessors();
					if (null != customerProcessors && !customerProcessors.isEmpty()) {

						List<IConnectProcessor> dsCustomerProcessors = connectProcessorFactory
								.processData(customerProcessors);

						if (null != dsCustomerProcessors && !dsCustomerProcessors.isEmpty()) {

							dsCustomerProcessors.forEach(dsCustomerProcessor -> {

								dsCustomerProcessor.processConnectData(migrationQueueName, toMigrateList);
							});
						}

					}

					if (null != toMigrateList && !toMigrateList.isEmpty()) {

						sendToQueue(toMigrateList, processId, batchId, queueName);
					} else {

						log.info(
								"<<<<<<<<<<<<<<<<<<<< No Records are available to send to Migration Queue >>>>>>>>>>>>>>>>>>>>");
					}
				} else {

					log.info("No Records available to migrate for queueName -> {},  processId -> {} and batchId -> {}",
							queueName, processId, batchId);
				}
			} else if (!StringUtils.isEmpty(getApplicationEnvelopeQueueName())
					&& getApplicationEnvelopeQueueName().equalsIgnoreCase(queueName)) {

				sendToApplicationEnvelopeQueue(toProcessDSEnvelopeDefinitionList, processId, batchId, queueName);

			} else {

				sendToQueue(toProcessDSEnvelopeDefinitionList, processId, batchId, queueName);
			}
			return asyncStatus;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			connectHelper.handleAsyncStatus(asyncStatus, exp, "prepareMessageAndSendToQueueInDownstreamQueueService",
					processId);
			return asyncStatus;
		});
	}

	private CompletableFuture<String> sendToApplicationEnvelopeQueue(
			List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList, String processId, String batchId,
			String queueName) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;

			if (null != toProcessDSEnvelopeDefinitionList && !toProcessDSEnvelopeDefinitionList.isEmpty()) {

				List<ApplicationEnvelopeDefinition> applicationEnvelopeDefinitionList = new ArrayList<ApplicationEnvelopeDefinition>();
				toProcessDSEnvelopeDefinitionList.forEach(toProcessDSEnvelopeDefinition -> {

					ApplicationEnvelopeDefinition applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

					if (null != toProcessDSEnvelopeDefinition.getDsCustomFieldDefinitions()
							&& !toProcessDSEnvelopeDefinition.getDsCustomFieldDefinitions().isEmpty()) {

						prepareAndSetApplicationIdType(toProcessDSEnvelopeDefinition, applicationEnvelopeDefinition);
					} else {

						log.error("CustomField cannot be empty or Null for envelopeId -> {}",
								toProcessDSEnvelopeDefinition.getEnvelopeId());
						throw new InvalidInputException("CustomField cannot be empty or Null for envelopeId "
								+ toProcessDSEnvelopeDefinition.getEnvelopeId());
					}

					applicationEnvelopeDefinition.setEnvelopeId(toProcessDSEnvelopeDefinition.getEnvelopeId());
					applicationEnvelopeDefinition
							.setEnvelopeSentTimestamp(toProcessDSEnvelopeDefinition.getSentDateTime());

					if (null != toProcessDSEnvelopeDefinition.getDsRecipientDefinitions()
							&& !toProcessDSEnvelopeDefinition.getDsRecipientDefinitions().isEmpty()) {

						List<String> recipientEmailList = toProcessDSEnvelopeDefinition.getDsRecipientDefinitions()
								.stream().map(DSRecipientDefinition::getRecipientEmail).collect(Collectors.toList());
						applicationEnvelopeDefinition.setRecipientEmails(recipientEmailList);
					} else {

						log.error("RecipientEmail cannot be empty or null");

						throw new InvalidInputException("RecipientEmail cannot be empty or null for envelopeId "
								+ toProcessDSEnvelopeDefinition.getEnvelopeId());
					}

					applicationEnvelopeDefinitionList.add(applicationEnvelopeDefinition);
				});

				if (null != applicationEnvelopeDefinitionList && !applicationEnvelopeDefinitionList.isEmpty()) {

					ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition = new ApplicationEnvelopeMessageDefinition();
					applicationEnvelopeMessageDefinition
							.setApplicationEnvelopeDefinitions(applicationEnvelopeDefinitionList);

					// Create New Concurrent Processes for downstream queues
					if (!StringUtils.isEmpty(processId)) {

						ConcurrentProcessLogDefinition concurrentProcessLogDefinitionLocal = coreBatchDataService
								.createConcurrentProcess(Long.valueOf(applicationEnvelopeDefinitionList.size()),
										batchId, processId);

						applicationEnvelopeMessageDefinition.setBatchId(batchId);
						applicationEnvelopeMessageDefinition.setGroupId(processId);
						applicationEnvelopeMessageDefinition
								.setProcessId(concurrentProcessLogDefinitionLocal.getProcessId());
					}

					rabbitTemplate.convertAndSend(queueName, applicationEnvelopeMessageDefinition);
				} else {

					log.error("applicationEnvelopeDefinitionList should not be empty or null for processId -> {}",
							processId);
					throw new InvalidInputException(
							"applicationEnvelopeDefinitionList cannot be empty or Null for processId " + processId);
				}
			}

			return asyncStatus;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			connectHelper.handleAsyncStatus(asyncStatus, exp, "sendToApplicationEnvelopeQueueInDownstreamQueueService",
					processId);
			return asyncStatus;
		});
	}

	private void prepareAndSetApplicationIdType(DSEnvelopeDefinition toProcessDSEnvelopeDefinition,
			ApplicationEnvelopeDefinition applicationEnvelopeDefinition) {

		String applicationId = toProcessDSEnvelopeDefinition.getDsCustomFieldDefinitions().stream()
				.filter(customFieldDefinition -> AppConstants.ECFNAME_APPLICATIONID
						.equalsIgnoreCase(customFieldDefinition.getFieldName()))
				.map(DSCustomFieldDefinition::getFieldValue).findFirst().orElse(null);

		if (!StringUtils.isEmpty(applicationId)) {

			applicationEnvelopeDefinition.setApplicationId(applicationId);
		} else {

			log.error("applicationId cannot be empty or Null for envelopeId -> {}",
					toProcessDSEnvelopeDefinition.getEnvelopeId());
			throw new InvalidInputException("applicationId cannot be empty or Null for envelopeId "
					+ toProcessDSEnvelopeDefinition.getEnvelopeId());
		}

		String applicationType = toProcessDSEnvelopeDefinition.getDsCustomFieldDefinitions().stream()
				.filter(customFieldDefinition -> AppConstants.APPLICATIONTYPE_ECF_NAME
						.equalsIgnoreCase(customFieldDefinition.getFieldName()))
				.map(DSCustomFieldDefinition::getFieldValue).findFirst().orElse(null);

		if (!StringUtils.isEmpty(applicationType)) {

			applicationEnvelopeDefinition.setApplicationType(applicationType);
		} else {

			log.error("applicationType cannot be empty or Null for envelopeId -> {}",
					toProcessDSEnvelopeDefinition.getEnvelopeId());
			throw new InvalidInputException("applicationType cannot be empty or Null for envelopeId "
					+ toProcessDSEnvelopeDefinition.getEnvelopeId());
		}
	}

	private String getApplicationEnvelopeQueueName() {

		return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
				PropertyCacheConstants.QUEUE_REFERENCE_NAME);
	}

	private List<String> findCustomerProcessors() {

		String customerProcessorsAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_ALLOWED_CUSTOMER_PROCESSOR_TYPES,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(customerProcessorsAsStr)) {

			return null;
		} else {

			List<String> customerProcessors = Stream.of(customerProcessorsAsStr.split(AppConstants.COMMA_DELIMITER))
					.map(String::trim).map(String::toUpperCase).collect(Collectors.toList());

			return customerProcessors;
		}
	}

	private CompletableFuture<String> sendToQueue(List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList,
			String processId, String batchId, String queueName) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;

			EnvelopeMessageDefinition envelopeMessageDefinition = new EnvelopeMessageDefinition();
			if (StringUtils.isEmpty(processId) && null != toProcessDSEnvelopeDefinitionList
					&& !toProcessDSEnvelopeDefinitionList.isEmpty() && toProcessDSEnvelopeDefinitionList.size() == 1) {

				envelopeMessageDefinition.setDsEnvelopeDefinition(toProcessDSEnvelopeDefinitionList.get(0));

				log.info("Sending to queueName -> {} for envelopeId -> {}", queueName,
						toProcessDSEnvelopeDefinitionList.get(0).getEnvelopeId());
				rabbitTemplate.convertAndSend(queueName, envelopeMessageDefinition);
			} else {

				Long totalRecordsInProcess = 1L;
				if (toProcessDSEnvelopeDefinitionList.size() == 1) {

					envelopeMessageDefinition.setDsEnvelopeDefinition(toProcessDSEnvelopeDefinitionList.get(0));
				} else {

					List<String> recordIds = toProcessDSEnvelopeDefinitionList.stream()
							.map(DSEnvelopeDefinition::getEnvelopeId).collect(Collectors.toList());

					totalRecordsInProcess = Long.valueOf(recordIds.size());

					// Create New Concurrent Processes for downstream queues
					ConcurrentProcessLogDefinition concurrentProcessLogDefinitionLocal = coreBatchDataService
							.createConcurrentProcess(totalRecordsInProcess, batchId, processId);

					envelopeMessageDefinition.setBatchId(batchId);
					envelopeMessageDefinition.setProcessId(concurrentProcessLogDefinitionLocal.getProcessId());
					envelopeMessageDefinition.setGroupId(processId);
					envelopeMessageDefinition.setRecordIds(recordIds);

				}

				log.info("Sending to queueName -> {} for processId -> {}", queueName, processId);
				rabbitTemplate.convertAndSend(queueName, envelopeMessageDefinition);
			}

			return asyncStatus;
		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			connectHelper.handleAsyncStatus(asyncStatus, exp, "sendToQueueInDownstreamQueueService", processId);
			return asyncStatus;
		});
	}

	private String docMigrationQueueName() {

		String docMigrationQueueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_DOCMIGRATION_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

		if (!StringUtils.isEmpty(docMigrationQueueName)) {

			return docMigrationQueueName;
		} else {

			return null;
		}
	}

	public List<String> getEnvStatusesAvailableForDocMigration() {

		String saveDocMigrationEnvStatusesStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_DOCMIGRATION_ENVSTATUS,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveDocMigrationEnvStatusesStr)) {

			List<String> envStatusesAvailableToMigrate = new ArrayList<String>(4);
			envStatusesAvailableToMigrate.add(EnvelopeStatusCode.COMPLETED.value().toUpperCase());
			envStatusesAvailableToMigrate.add(EnvelopeStatusCode.DECLINED.value().toUpperCase());
			envStatusesAvailableToMigrate.add(EnvelopeStatusCode.VOIDED.value().toUpperCase());
			envStatusesAvailableToMigrate.add(AppConstants.ENVELOPE_EXPIRED_STATUS.toUpperCase());
			return envStatusesAvailableToMigrate;
		} else {

			List<String> envStatusesAvailableToMigrate = Stream
					.of(saveDocMigrationEnvStatusesStr.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
					.map(String::toUpperCase).collect(Collectors.toList());

			return envStatusesAvailableToMigrate;
		}
	}

}