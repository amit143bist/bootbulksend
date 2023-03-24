package com.ds.proserv.docdownload.consumer.listener;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.ThrottleService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.docdownload.consumer.client.CustomEnvelopeDataClient;
import com.ds.proserv.docdownload.consumer.service.BucketService;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdRequest;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdResponse;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import com.ds.proserv.feign.report.domain.ConcurrentDocDownloadDataMessageDefinition;
import com.ds.proserv.feign.report.domain.DownloadDataMessage;
import com.ds.proserv.report.file.service.FileWriterService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocDownloadListener extends AbstractMigrationListener<ConcurrentDocDownloadDataMessageDefinition> {

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private BucketService bucketService;

	@Autowired
	private ThrottleService throttleService;

	@Autowired
	private FileWriterService fileWriterService;

	@Autowired
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("DocDownloadRequest received in processMessage() and xDeath value is {}", xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_RETRYLIMIT),
				concurrentDocDownloadDataMessageDefinition);

		throttleService.throttleConsumers();
	}

	@Override
	protected void callService(ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition) {

		List<DownloadDataMessage> downloadDataMessages = concurrentDocDownloadDataMessageDefinition
				.getDownloadDataMessages();

		if (null != downloadDataMessages && !downloadDataMessages.isEmpty()) {

			boolean singleRecordOnly = false;
			CustomEnvelopeDataIdResponse customEnvelopeDataIdResponse = null;
			if (downloadDataMessages.size() == 1) {

				singleRecordOnly = true;
			}

			if (!singleRecordOnly) {

				List<String> recordIds = downloadDataMessages.stream()
						.filter(downloadDataMessage -> downloadDataMessage.getRecordId() != null)
						.map(DownloadDataMessage::getRecordId).map(String::trim).collect(Collectors.toList());

				log.info("RecordIds to be checked in backend for processId -> {} and batchId -> {} is recordIds -> {}",
						concurrentDocDownloadDataMessageDefinition.getProcessId(),
						concurrentDocDownloadDataMessageDefinition.getBatchId(), recordIds);

				CustomEnvelopeDataIdRequest customEnvelopeDataIdRequest = new CustomEnvelopeDataIdRequest();
				customEnvelopeDataIdRequest.setEnvelopeIds(recordIds);

				customEnvelopeDataIdResponse = customEnvelopeDataClient
						.findPendingDocDownloadEnvelopesByEnvelopeIds(customEnvelopeDataIdRequest).getBody();
			}

			List<String> envelopeIds = null;
			if (null != customEnvelopeDataIdResponse && null != customEnvelopeDataIdResponse.getTotalRecords()
					&& customEnvelopeDataIdResponse.getTotalRecords() > 0) {

				envelopeIds = customEnvelopeDataIdResponse.getEnvelopeIds();
			} else {

				envelopeIds = new ArrayList<String>();
				envelopeIds.add(downloadDataMessages.get(0).getRecordId());
			}

			// Checking if docs pending to be written to disk
			if (null != envelopeIds && !envelopeIds.isEmpty()) {

				log.info("Checking and Writing to Disk for envelopeIds -> {}", envelopeIds);
				writeToDisk(concurrentDocDownloadDataMessageDefinition, downloadDataMessages, envelopeIds);
			} else {

				log.warn("No records to process for processId -> {} and batchId -> {}, so closing the process",
						concurrentDocDownloadDataMessageDefinition.getProcessId(),
						concurrentDocDownloadDataMessageDefinition.getBatchId());
				closeConcurrentProcess(concurrentDocDownloadDataMessageDefinition, envelopeIds);
			}

		}

	}

	@Data
	private static class DataWriteOutput {

		private String asyncResult;
		private String bucketName = null;
		private List<String> failedEnvelopeIds = new ArrayList<String>();
		private List<String> processedEnvelopeIds = new ArrayList<String>();

	}

	private void writeToDisk(ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition,
			List<DownloadDataMessage> downloadDataMessages, List<String> envelopeIds) {

		String bucketName = null;
		if (bucketService.useBucketLogic()) {

			bucketName = bucketService.findBucketName(1);

			log.info("Bucket to be used is {} for envelopeIds -> {}", bucketName, envelopeIds);
		}

		DataWriteOutput dataWriteOutput = new DataWriteOutput();
		dataWriteOutput.setBucketName(bucketName);

		List<String> failedEnvelopeIds = dataWriteOutput.getFailedEnvelopeIds();
		List<String> processedEnvelopeIds = dataWriteOutput.getProcessedEnvelopeIds();

		for (DownloadDataMessage downloadDataMessage : downloadDataMessages) {

			try {

				if (null != envelopeIds && !envelopeIds.isEmpty()
						&& !StringUtils.isEmpty(downloadDataMessage.getRecordId())
						&& (envelopeIds.contains(downloadDataMessage.getRecordId().trim())
								|| checkRecordIdInResponseIgnoringCase(envelopeIds,
										downloadDataMessage.getRecordId().trim()))) {

					String folderName = null;
					if (StringUtils.isEmpty(bucketName)) {

						folderName = downloadDataMessage.getFolderName();
					} else {

						folderName = bucketName + File.separator + downloadDataMessage.getRecordId();
					}

					log.info("Files will be written to folderName -> {} for recordId -> {}", folderName,
							downloadDataMessage.getRecordId());
					Path parentDirectory = Paths.get(downloadDataMessage.getParentDirectory());
					fileWriterService.pullDocumentAndWriteToDirectory(
							concurrentDocDownloadDataMessageDefinition.getDownloadDocs(),
							concurrentDocDownloadDataMessageDefinition.getBatchId(), parentDirectory,
							downloadDataMessage.getFileSaveFormat(), downloadDataMessage.getFileName(), folderName,
							downloadDataMessage.getInputParams(),
							concurrentDocDownloadDataMessageDefinition.getProcessId());

					processedEnvelopeIds.add(downloadDataMessage.getRecordId());
				} else {

					// This scenario will happen on retry
					log.warn(
							"RecordId -> {} not returned in {} CustomEnvelopeDataIdResponse for processId -> {} and batchId -> {}",
							downloadDataMessage.getRecordId(), envelopeIds,
							concurrentDocDownloadDataMessageDefinition.getProcessId(),
							concurrentDocDownloadDataMessageDefinition.getBatchId());
				}
			} catch (Throwable exp) {

				exp.printStackTrace();
				failedEnvelopeIds.add(downloadDataMessage.getRecordId());
				log.error(
						"Exception -> {} occurred while processing recordId -> {} for processId -> {} and batchId -> {}",
						exp, downloadDataMessage.getRecordId(),
						concurrentDocDownloadDataMessageDefinition.getProcessId(),
						concurrentDocDownloadDataMessageDefinition.getBatchId());
			}
		}

		CompletableFuture.runAsync(() -> {

			handleProcessedEnvelopeData(dataWriteOutput.getProcessedEnvelopeIds(), dataWriteOutput.getBucketName());
			handleFailedEnvelopeData(concurrentDocDownloadDataMessageDefinition,
					dataWriteOutput.getFailedEnvelopeIds());

		}, recordTaskExecutor).handle((result, exp) -> {

			if (null != exp) {

				log.error("Exception {} occurred in writeToDisk for envelopeIds -> {}", exp, envelopeIds);
				exp.printStackTrace();
				sendToDeadQueue(concurrentDocDownloadDataMessageDefinition, HttpStatus.UNPROCESSABLE_ENTITY.toString(),
						exp.getMessage());
			} else {

				log.info(
						"No exception occurred in processing for envelopeIds -> {} in processId -> {} and batchId -> {} in writeToDisk",
						envelopeIds, concurrentDocDownloadDataMessageDefinition.getProcessId(),
						concurrentDocDownloadDataMessageDefinition.getBatchId());
			}

			return result;
		});

	}

	private void handleFailedEnvelopeData(
			ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition,
			List<String> failedEnvelopeIds) {

		if (null != failedEnvelopeIds && !failedEnvelopeIds.isEmpty()) {

			log.error(
					"Check logs, some failure happened in handleFailedEnvelopeData for {} for processId -> {} and batchId -> {}",
					failedEnvelopeIds, concurrentDocDownloadDataMessageDefinition.getProcessId(),
					concurrentDocDownloadDataMessageDefinition.getBatchId());
			throw new ListenerProcessingException("Failure happened for " + failedEnvelopeIds);
		} else {

			log.warn(
					"All records processed in handleFailedEnvelopeData for processId -> {} and batchId -> {}, so closing the process",
					concurrentDocDownloadDataMessageDefinition.getProcessId(),
					concurrentDocDownloadDataMessageDefinition.getBatchId());
			closeConcurrentProcess(concurrentDocDownloadDataMessageDefinition, failedEnvelopeIds);
		}
	}

	private void handleProcessedEnvelopeData(List<String> processedEnvelopeIds, String bucketName) {

		if (null != processedEnvelopeIds && !processedEnvelopeIds.isEmpty()) {

			if (!StringUtils.isEmpty(bucketName)) {

				log.info(
						"Calling customEnvelopeDataClient in handleProcessedEnvelopeData for processedEnvelopeIds -> {} with bucketName -> {}",
						processedEnvelopeIds, bucketName);
				customEnvelopeDataClient.updateCustomEnvelopeDataDocDownloadStatusEndTimeWithBucketName(
						preparePageInformationWithBucketName(processedEnvelopeIds, bucketName));
			} else {

				log.info(
						"Calling customEnvelopeDataClient in handleProcessedEnvelopeData for processedEnvelopeIds -> {}",
						processedEnvelopeIds);
				customEnvelopeDataClient
						.updateCustomEnvelopeDataDocDownloadStatusEndTime(preparePageInformation(processedEnvelopeIds));
			}

		}
	}

	private boolean checkRecordIdInResponseIgnoringCase(List<String> envelopeIds, String recordId) {

		log.info("Checking recordId -> {} inside checkRecordIdInResponseIgnoringCase", recordId);
		for (String envelopeId : envelopeIds) {

			if (!StringUtils.isEmpty(envelopeId) && !StringUtils.isEmpty(recordId)
					&& envelopeId.trim().equalsIgnoreCase(recordId.trim())) {
				return true;
			}
		}

		return false;
	}

	private void closeConcurrentProcess(
			ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition,
			List<String> envelopeIds) {

		log.info("Calling closeConcurrentProcess for envelopeIds -> {}", envelopeIds);

		if (!StringUtils.isEmpty(concurrentDocDownloadDataMessageDefinition.getProcessId())) {

			ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
			concurrentProcessLogDefinition.setBatchId(concurrentDocDownloadDataMessageDefinition.getBatchId());
			concurrentProcessLogDefinition.setProcessId(concurrentDocDownloadDataMessageDefinition.getProcessId());
			concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.COMPLETED.toString());
			concurrentProcessLogDefinition.setGroupId(concurrentDocDownloadDataMessageDefinition.getGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_COMPLETE_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			rabbitTemplate.convertAndSend(queueName, concurrentProcessLogDefinition);
		}
	}

	private PageInformation preparePageInformation(List<String> envelopeIdList) {

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(String.join(AppConstants.COMMA_DELIMITER, envelopeIdList));

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);
		pageInformation.setPageQueryParams(pageQueryParamList);
		return pageInformation;
	}

	private PageInformation preparePageInformationWithBucketName(List<String> envelopeIdList, String bucketName) {

		PageInformation pageInformation = new PageInformation();

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(String.join(AppConstants.COMMA_DELIMITER, envelopeIdList));
		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.BUCKET_PARAM_NAME);
		pageQueryParam.setParamValue(bucketName);
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);
		return pageInformation;
	}

	@Override
	protected void sendToDeadQueue(
			ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition, String httpStatus,
			String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
				concurrentDocDownloadDataMessageDefinition, errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				concurrentDocDownloadDataMessageDefinition, m -> {
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
			ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition) {

		log.error(
				"{} is thrown and exception message is {} in processing docDownloadRequest for batchId -> {} and processId -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), concurrentDocDownloadDataMessageDefinition.getBatchId(),
				concurrentDocDownloadDataMessageDefinition.getProcessId(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_RETRYLIMIT),
				httpStatus);
	}

}