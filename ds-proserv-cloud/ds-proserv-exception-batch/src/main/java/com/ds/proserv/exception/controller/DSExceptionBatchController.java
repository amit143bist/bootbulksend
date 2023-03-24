package com.ds.proserv.exception.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.batch.common.service.BatchQueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.common.util.PreparePageUtil;
import com.ds.proserv.connect.client.DSExceptionClient;
import com.ds.proserv.connect.domain.ConnectCacheData;
import com.ds.proserv.connect.helper.ConnectHelper;
import com.ds.proserv.connect.service.ConnectProcessorBulkService;
import com.ds.proserv.feign.connect.domain.ConnectMessageDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionIdResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
public class DSExceptionBatchController {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ConnectHelper connectHelper;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private DSExceptionClient dsExceptionClient;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private ConnectProcessorBulkService connectProcessorBulkService;

	@GetMapping("/docusign/envelopedata/exception/manualretry")
	public void retryExceptionsManually() {

		triggerExceptionAsync();
	}

	private CompletableFuture<String> triggerExceptionAsync() {

		CompletableFuture<String> completableFuture = new CompletableFuture<>();

		Executors.newCachedThreadPool().submit(() -> {

			log.info("Triggered batch creation manually");
			// processQueuedConnectMessages();
			processQueuedMessages();
			return null;
		});

		return completableFuture;

	}

	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	private void processQueuedMessages() {

		try {

			log.info("Starting processQueuedMessages at {}", LocalDateTime.now());
			int totalBatchSize = 0;
			String batchId = coreBatchDataService.checkOrCreateBatch();
			List<CompletableFuture<String>> reportDataFutureDSExceptionList = new ArrayList<CompletableFuture<String>>();

			PageInformation pageInformation = PreparePageUtil.prepareExceptionPageInformation(0, getRecordsPerPage());
			DSExceptionIdResult dsExceptionIdResult = dsExceptionClient
					.findAllExceptionIdsByRetryStatusesOrNullRetryStatus(pageInformation).getBody();

			Long totalRecords = dsExceptionIdResult.getTotalRecords();

			boolean connectByQueue = connectHelper.isConnectByQueue();

			if (null != totalRecords && totalRecords > 0) {

				List<String> exceptionIds = dsExceptionIdResult.getDsEnvelopeIds();

				if (connectByQueue) {

					processDataQueueBased(batchId, exceptionIds);
				} else {

					totalBatchSize = processDataNonQueueBased(totalBatchSize, batchId, reportDataFutureDSExceptionList,
							exceptionIds);
				}

			} else {

				log.warn(
						"!!!!!!!!!!!!!!!!!!!!!!!! No Connect message to process at this time -> {} !!!!!!!!!!!!!!!!!!!!!!!!",
						LocalDateTime.now());
			}

			log.info("All batched processed for batchId -> {}", batchId);

			if (connectByQueue) {

				if (coreBatchDataService.isBatchCreatedWithWorkerThreads(batchId)) {

					log.info(
							"######################### All Messages queued in process Exception flow for batchId -> {} #########################",
							batchId);
				} else {

					coreBatchDataService.finishNewBatch(batchId, Long.valueOf(totalBatchSize));
				}
			} else {

				coreBatchDataService.finishNewBatch(batchId, Long.valueOf(totalBatchSize));
			}

		} catch (RunningBatchException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		} catch (Throwable e) {

			e.printStackTrace();
		}
	}

	private Integer getRecordsPerPage() {

		String recordsPerPageStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_RECORDS_PERPAGE, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(recordsPerPageStr)) {

			return 50;
		} else {

			return Integer.parseInt(recordsPerPageStr);
		}
	}

	private void processDataQueueBased(String batchId, List<String> exceptionIds) {

		Integer recordsPerPage = getRecordsPerPage();

		final AtomicInteger counter = new AtomicInteger(0);
		final Collection<List<String>> exceptionBatches = exceptionIds.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / recordsPerPage)).values();

		int pageNumber = 0;
		for (List<String> recordIds : exceptionBatches) {

			ConcurrentProcessLogDefinition childProcessConcurrentProcessLogDefinition = coreBatchDataService
					.createConcurrentProcess(Long.valueOf(recordIds.size()), batchId);

			ConnectMessageDefinition connectMessageDefinition = new ConnectMessageDefinition();
			connectMessageDefinition.setRecordIds(recordIds);
			connectMessageDefinition.setBatchId(batchId);
			connectMessageDefinition.setProcessId(childProcessConcurrentProcessLogDefinition.getProcessId());
			connectMessageDefinition.setPageNumber(pageNumber);

			batchQueueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_CONNECT_QUEUE_NAME,
					childProcessConcurrentProcessLogDefinition.getProcessId(), batchId, connectMessageDefinition);
			pageNumber++;
		}

	}

	private int processDataNonQueueBased(int totalBatchSize, String batchId,
			List<CompletableFuture<String>> reportDataFutureDSExceptionList, List<String> exceptionIds)
			throws InterruptedException, ExecutionException {

		ConnectCacheData connectCacheData = new ConnectCacheData(dsCacheManager);

		final AtomicInteger counter = new AtomicInteger(0);
		final Collection<List<String>> exceptionBatches = exceptionIds.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / connectCacheData.getRecordsPerPage()))
				.values();

		int pageCounter = 0;

		for (List<String> exceptionBatch : exceptionBatches) {

			totalBatchSize = createAsyncBatchV2(pageCounter, batchId, exceptionBatch, totalBatchSize,
					reportDataFutureDSExceptionList, connectCacheData);

			pageCounter++;
		}

		if (!StringUtils.isEmpty(batchId) && null != reportDataFutureDSExceptionList
				&& !reportDataFutureDSExceptionList.isEmpty()) {

			log.info("Waiting for all Async job to complete for batchId -> {}", batchId);
			CompletableFuture.allOf(reportDataFutureDSExceptionList
					.toArray(new CompletableFuture[reportDataFutureDSExceptionList.size()])).get();
		}
		return totalBatchSize;
	}

	private int createAsyncBatchV2(int pageNumber, String batchId, List<String> exceptionIds, int totalBatchSize,
			List<CompletableFuture<String>> reportDataFutureDSExceptionList, ConnectCacheData connectCacheData)
			throws InterruptedException {

		log.info("createAsyncBatchV2(): Starting new retry batch");

		if (null != connectCacheData.isStepSleepEnabled() && connectCacheData.isStepSleepEnabled()
				&& ((pageNumber + 1) % connectCacheData.getThresholdCheck() == 0)) {

			log.info("Pausing batch after {} batches processed for {} milliseconds for pageNumber {}",
					connectCacheData.getThresholdCheck(), connectCacheData.getSleepInterval(), pageNumber);

			Thread.sleep(connectCacheData.getSleepInterval());

		}

		totalBatchSize = totalBatchSize + exceptionIds.size();
		reportDataFutureDSExceptionList
				.add(processConnectExceptionAsync(exceptionIds, batchId, pageNumber, connectCacheData));
		return totalBatchSize;
	}

	private CompletableFuture<String> processConnectExceptionAsync(List<String> exceptionIds, String batchId,
			int pageNumber, ConnectCacheData connectCacheData) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String processId = null;
			String asyncStatus = AppConstants.SUCCESS_VALUE;
			try {

				log.info("processConnectExceptionAsync called for batchId -> {}", batchId);

				List<DSExceptionDefinition> dsExceptionDefinitionList = connectProcessorBulkService
						.fetchAndPrepareDSExceptionDefinitionList(exceptionIds, batchId, pageNumber);

				int batchSize = dsExceptionDefinitionList.size();

				processId = coreBatchDataService.createConcurrentProcess(Long.valueOf(batchSize), batchId)
						.getProcessId();
				connectProcessorBulkService.processBulkSaveWithCounter(1, dsExceptionDefinitionList, batchId, processId,
						connectCacheData);
				connectProcessorBulkService.bulkSaveExceptionAfterProcessing(1, dsExceptionDefinitionList, batchId,
						processId, connectCacheData);

				coreBatchDataService.finishConcurrentProcess(processId, ProcessStatus.COMPLETED.toString());
			} catch (Throwable exp) {

				log.error("Some error {} occurred in processConnectExceptionAsync", exp);
				asyncStatus = AppConstants.FAILURE_VALUE;

				if (StringUtils.isEmpty(processId)) {

					coreBatchDataService.createFailureProcess("PAGE_" + pageNumber, FailureCode.ERROR_107.toString(),
							"UnknownExceptionByBatch", FailureStep.BULK_CONNECT_PROCESS.toString(), batchId);
				} else {

					coreBatchDataService.createFailureProcess("PAGE_" + pageNumber, FailureCode.ERROR_107.toString(),
							"UnknownExceptionByProcess", FailureStep.BULK_CONNECT_PROCESS.toString(), processId);
				}
				exp.printStackTrace();

			}
			return asyncStatus;

		}, recordTaskExecutor).handle((asyncStatus, exp) -> {

			if (null != exp) {

				log.info("Async processing got error in handle, check failure table and/or logs for more details");

				exp.printStackTrace();

			} else {

				if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

					log.info(" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed $$$$$$$$$$$$$$$$$$$$$$$$$ ");
				} else {

					log.warn("Result is NOT success, it is {}, check logs for more information", asyncStatus);
				}

			}

			return asyncStatus;
		});
	}

}