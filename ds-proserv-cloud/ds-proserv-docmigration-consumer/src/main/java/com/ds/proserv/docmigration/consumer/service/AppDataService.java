package com.ds.proserv.docmigration.consumer.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.domain.PageSortParam;
import com.ds.proserv.docmigration.consumer.client.CustomEnvelopeDataClient;
import com.ds.proserv.docmigration.consumer.client.DSEnvelopeClient;
import com.ds.proserv.docmigration.consumer.client.MigrationDataClient;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataDefinition;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataInformation;
import com.ds.proserv.feign.appdata.domain.MigrationDataDefinition;
import com.ds.proserv.feign.connect.domain.EnvelopeMessageDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.report.domain.ConcurrentReportCompleteMessageDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppDataService {

	@Autowired
	private TaskExecutor xmlTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private DSEnvelopeClient dsEnvelopeClient;

	@Autowired
	private MigrationDataClient migrationDataClient;

	@Autowired
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	@Autowired
	private MigrationDocDownloadService migrationDocDownloadService;

	public void saveCustomEnvelopeData(EnvelopeMessageDefinition envelopeMessageDefinition,
			MigrationDataDefinition migrationDataDefinition) {

		DSEnvelopeDefinition dsEnvelopeDefinition = envelopeMessageDefinition.getDsEnvelopeDefinition();
		if (null != dsEnvelopeDefinition) {

			CustomEnvelopeDataDefinition customEnvelopeDataDefinition = new CustomEnvelopeDataDefinition();
			customEnvelopeDataDefinition.setEnvelopeId(dsEnvelopeDefinition.getEnvelopeId());

			if (AppConstants.ENVELOPE_COMPLETED_STATUS.equalsIgnoreCase(dsEnvelopeDefinition.getStatus())
					|| !StringUtils.isEmpty(dsEnvelopeDefinition.getCompletedDateTime())) {

				customEnvelopeDataDefinition.setEnvTimeStamp(dsEnvelopeDefinition.getCompletedDateTime());
				customEnvelopeDataDefinition.setEnvDate(dsEnvelopeDefinition.getCompletedDateTime().substring(0, 10));
			} else {

				customEnvelopeDataDefinition.setEnvTimeStamp(dsEnvelopeDefinition.getTimeGenerated());
				customEnvelopeDataDefinition.setEnvDate(dsEnvelopeDefinition.getTimeGenerated().substring(0, 10));
			}
			customEnvelopeDataDefinition.setSenderIdentifier(dsEnvelopeDefinition.getSenderEmail());
			customEnvelopeDataDefinition.setEnvProcessStatusFlag(ProcessStatus.INPROGRESS.toString());
			customEnvelopeDataDefinition.setEnvProcessStartDateTime(LocalDateTime.now().toString());

			log.info("Saving customenvelopedata for envelopeId -> {}", dsEnvelopeDefinition.getEnvelopeId());
			customEnvelopeDataClient.saveEnvelopeData(customEnvelopeDataDefinition);

		} else {

			List<String> envelopeIds = envelopeMessageDefinition.getRecordIds();
			DSEnvelopeInformation dsEnvelopeInformation = dsEnvelopeClient
					.findEnvelopesByEnvelopeIds(preparePageInformation(0, envelopeIds)).getBody();

			List<CustomEnvelopeDataDefinition> customEnvelopeDataDefinitionList = new ArrayList<CustomEnvelopeDataDefinition>();
			if (null != dsEnvelopeInformation) {

				List<DSEnvelopeDefinition> dsEnvelopeDefinitionList = dsEnvelopeInformation.getDsEnvelopeDefinitions();

				dsEnvelopeDefinitionList.forEach(dsEnvelopeDefinitionVar -> {

					CustomEnvelopeDataDefinition customEnvelopeDataDefinition = new CustomEnvelopeDataDefinition();
					customEnvelopeDataDefinition.setEnvelopeId(dsEnvelopeDefinitionVar.getEnvelopeId());
					customEnvelopeDataDefinition.setEnvTimeStamp(dsEnvelopeDefinitionVar.getSentDateTime());
					customEnvelopeDataDefinition.setSenderIdentifier(dsEnvelopeDefinitionVar.getSenderEmail());

					if (AppConstants.ENVELOPE_COMPLETED_STATUS.equalsIgnoreCase(dsEnvelopeDefinitionVar.getStatus())
							|| !StringUtils.isEmpty(dsEnvelopeDefinitionVar.getCompletedDateTime())) {

						customEnvelopeDataDefinition
								.setEnvDate(dsEnvelopeDefinitionVar.getCompletedDateTime().substring(0, 10));
					} else {

						customEnvelopeDataDefinition
								.setEnvDate(dsEnvelopeDefinitionVar.getTimeGenerated().substring(0, 10));
					}

					customEnvelopeDataDefinition.setEnvProcessStatusFlag(ProcessStatus.INPROGRESS.toString());
					customEnvelopeDataDefinition.setEnvProcessStartDateTime(LocalDateTime.now().toString());
					customEnvelopeDataDefinitionList.add(customEnvelopeDataDefinition);
				});

				CustomEnvelopeDataInformation customEnvelopeDataInformation = new CustomEnvelopeDataInformation();
				customEnvelopeDataInformation.setCustomEnvelopeDataDefinitions(customEnvelopeDataDefinitionList);

				log.info("Saving customenvelopedata for processId -> {} and batchId -> {}",
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				customEnvelopeDataClient.bulkSaveEnvelopeData(customEnvelopeDataInformation);
			}
		}

		migrationDataClient.bulkUpdateSaveMigrationData(migrationDataDefinition);

		sendToReportComplete(envelopeMessageDefinition, migrationDataDefinition);
		migrationDocDownloadService.prepareAndSendDataForDocDownload(migrationDataDefinition.getRowDataMapList(),
				envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());

	}

	public void handleStatus(String asyncStatus, Throwable exp, String callingMethodName, String processId) {

		if (null != exp) {

			log.info("Async processing inside handleStatus got exception in {} for processId -> {}", callingMethodName,
					processId);

			exp.printStackTrace();

		} else {

			if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

				log.info(
						" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing inside handleStatus completed in {} for processId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
						callingMethodName, processId);
			} else {

				log.warn(
						"Result is NOT success inside handleStatus, it is -> {}, check logs for more information for {} and processId -> {}",
						asyncStatus, callingMethodName, processId);
			}

		}
	}

	private void sendToReportComplete(EnvelopeMessageDefinition envelopeMessageDefinition,
			MigrationDataDefinition migrationDataDefinition) {

		CompletableFuture.runAsync(() -> {

			ConcurrentReportCompleteMessageDefinition concurrentReportCompleteMessageDefinition = new ConcurrentReportCompleteMessageDefinition();

			concurrentReportCompleteMessageDefinition.setBatchId(envelopeMessageDefinition.getBatchId());
			concurrentReportCompleteMessageDefinition.setPrimaryIds(migrationDataDefinition.getRecordIds());
			concurrentReportCompleteMessageDefinition.setProcessId(envelopeMessageDefinition.getProcessId());
			concurrentReportCompleteMessageDefinition.setGroupId(envelopeMessageDefinition.getGroupId());

			String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.PROCESS_REPORTCOMPLETE_QUEUE_NAME,
					PropertyCacheConstants.QUEUE_REFERENCE_NAME);

			rabbitTemplate.convertAndSend(queueName, concurrentReportCompleteMessageDefinition);
		}, xmlTaskExecutor);
	}

	private PageInformation preparePageInformation(int pageNumber, List<String> envelopeIdList) {

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(String.join(AppConstants.COMMA_DELIMITER, envelopeIdList));

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		PageSortParam pageSortParam = new PageSortParam();
		pageSortParam.setFieldName("envelopeId");
		pageSortParam.setSortDirection("asc");

		List<PageSortParam> pageSortParamList = new ArrayList<PageSortParam>();
		pageSortParamList.add(pageSortParam);

		pageInformation.setPageSortParams(pageSortParamList);
		pageInformation.setPageQueryParams(pageQueryParamList);
		pageInformation.setPageNumber(pageNumber);
		pageInformation.setRecordsPerPage(envelopeIdList.size());

		return pageInformation;
	}
}