package com.ds.proserv.bulksendmonitor.batch.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.bulksendmonitor.batch.client.BulkSendProcessLogClient;
import com.ds.proserv.bulksendmonitor.batch.domain.BulkSendBatchMonitorResponse;
import com.ds.proserv.bulksendmonitor.batch.service.BulkSendMonitorService;
import com.ds.proserv.bulksendmonitor.batch.transformer.BulkSendMonitorTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.BatchStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogIdResult;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogInformation;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendMonitorController {

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private BulkSendMonitorService bulkSendMonitorService;

	@Autowired
	private BulkSendProcessLogClient bulkSendProcessLogClient;

	@Autowired
	private BulkSendMonitorTransformer bulkSendMonitorTransformer;

	@GetMapping("/docusign/bulksenddata/monitor/manualretry")
	public void retryExceptionsManually() {

		triggerExceptionAsync();
	}

	private CompletableFuture<String> triggerExceptionAsync() {

		CompletableFuture<String> completableFuture = new CompletableFuture<>();

		Executors.newCachedThreadPool().submit(() -> {

			log.info("Triggered BulkSend batch manually");
			triggerBatchJob();
			return null;
		});

		return completableFuture;

	}

	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	public void triggerBatchJob() {

		String coreBatchId = coreBatchDataService.checkOrCreateBatch();
		BulkSendProcessLogIdResult bulkSendProcessLogIdResult = bulkSendProcessLogClient
				.findAllBatchIdsByBatchStatuses(preparePageInformation()).getBody();

		if (null != bulkSendProcessLogIdResult) {
			List<String> bulkBatchIds = bulkSendProcessLogIdResult.getBatchIds();

			if (null != bulkBatchIds && !bulkBatchIds.isEmpty()) {

				if (bulkBatchIds.size() > 50) {

					final AtomicInteger groupByCounter = new AtomicInteger(0);
					Collection<List<String>> groupedBatchColl = bulkBatchIds.stream()
							.collect(Collectors.groupingBy(it -> groupByCounter.getAndIncrement() / 50)).values();

					groupedBatchColl.forEach(groupedBatches -> {

						monitorBulkBatchProgress(groupedBatches, coreBatchId);
					});
				} else {

					monitorBulkBatchProgress(bulkBatchIds, coreBatchId);
				}

				coreBatchDataService.finishNewBatch(coreBatchId, Long.valueOf(bulkBatchIds.size()));

			} else {

				log.warn(
						"++++++++++++++++++++ No SUBMITTED or INPROGRESS BATCHES in DB for coreBatchId -> {} ++++++++++++++++++++",
						coreBatchId);
				coreBatchDataService.finishNewBatch(coreBatchId, 0L);
			}
		} else {

			log.error("Some issue happened, bulkSendProcessLogIdResult should not be null for coreBatchId -> {}",
					coreBatchId);
		}
	}

	private void monitorBulkBatchProgress(List<String> bulkBatchIds, String coreBatchId) {

		Map<String, List<BulkSendBatchMonitorResponse>> bulkSendBatchMonitorResponseMap = bulkSendMonitorService
				.fetchBatchStatusesByBatchIds(bulkBatchIds, coreBatchId);

		List<BulkSendProcessLogDefinition> bulkSendProcessLogDefinitions = new ArrayList<BulkSendProcessLogDefinition>(
				bulkBatchIds.size());
		bulkSendBatchMonitorResponseMap.forEach((status, bulkSendBatchMonitorResponseList) -> {

			log.info("For coreBatchId -> {} and {} status, records size is {}", coreBatchId, status,
					bulkSendBatchMonitorResponseList.size());

			bulkSendBatchMonitorResponseList.forEach(bulkSendBatchMonitorResponse -> {

				bulkSendProcessLogDefinitions.add(bulkSendMonitorTransformer
						.transformToBulkSendProcessLogDefinition(bulkSendBatchMonitorResponse, status));
			});

		});

		if (null != bulkSendProcessLogDefinitions && !bulkSendProcessLogDefinitions.isEmpty()) {

			BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
			bulkSendProcessLogInformation.setBulkSendProcessLogDefinitions(bulkSendProcessLogDefinitions);
			bulkSendProcessLogInformation.setTotalRecords(Long.valueOf(bulkSendProcessLogDefinitions.size()));

			bulkSendProcessLogClient.bulkSaveBulkSendProcessLog(bulkSendProcessLogInformation);
		} else {

			log.warn("bulkSendProcessLogDefinitions cannot be null or empty at this point for coreBatchId -> {}",
					coreBatchId);
		}
	}

	private PageInformation preparePageInformation() {

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.BATCHSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue(String.join(AppConstants.COMMA_DELIMITER, BatchStatus.SUBMITTED.toString(),
				BatchStatus.INPROGRESS.toString()));

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);
		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
	}
}