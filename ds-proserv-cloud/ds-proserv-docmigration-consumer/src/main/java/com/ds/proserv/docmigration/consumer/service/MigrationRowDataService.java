package com.ds.proserv.docmigration.consumer.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.docmigration.consumer.client.DSEnvelopeClient;
import com.ds.proserv.feign.appdata.domain.MigrationDataDefinition;
import com.ds.proserv.feign.connect.domain.EnvelopeMessageDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.ds.proserv.feign.util.ReportDataRuleUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MigrationRowDataService {

	@Autowired
	private TaskExecutor xmlTaskExecutor;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RuleEngineDefinition ruleEngineDefinition = null;

	@Autowired
	private DSEnvelopeClient dsEnvelopeClient;

	public CompletableFuture<MigrationDataDefinition> prepareMigrationDataSet(
			EnvelopeMessageDefinition envelopeMessageDefinition) {

		return CompletableFuture.supplyAsync((Supplier<MigrationDataDefinition>) () -> {

			DSEnvelopeInformation dsEnvelopeInformation = null;
			MigrationDataDefinition migrationDataDefinition = new MigrationDataDefinition();

			if (null != envelopeMessageDefinition.getDsEnvelopeDefinition()) {

				log.info("Preparing DSEnvelopeInformation for EnvelopeId -> {}",
						envelopeMessageDefinition.getDsEnvelopeDefinition().getEnvelopeId());
				List<DSEnvelopeDefinition> dsEnvelopeDefinitions = new ArrayList<DSEnvelopeDefinition>();
				dsEnvelopeDefinitions.add(envelopeMessageDefinition.getDsEnvelopeDefinition());

				dsEnvelopeInformation = new DSEnvelopeInformation();
				dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitions);

				migrationDataDefinition
						.setRecordIds(envelopeMessageDefinition.getDsEnvelopeDefinition().getEnvelopeId());
			} else {

				log.info("Fetching Envelope Tree for processId -> {} and batchId -> {}",
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				dsEnvelopeInformation = dsEnvelopeClient.findEnvelopesTreeListByEnvelopeIds(
						preparePageInformation(envelopeMessageDefinition.getRecordIds())).getBody();

				migrationDataDefinition.setRecordIds(
						String.join(AppConstants.COMMA_DELIMITER, envelopeMessageDefinition.getRecordIds()));
			}

			try {

				if (null != dsEnvelopeInformation.getDsEnvelopeDefinitions()
						&& !dsEnvelopeInformation.getDsEnvelopeDefinitions().isEmpty()) {

					log.debug("ruleEngineDefinition is successfully read for processId -> {} and batchId -> {}",
							envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());

					String dsEnvelopeInformationJson = objectMapper.writeValueAsString(dsEnvelopeInformation);

					if (log.isDebugEnabled()) {

						log.debug("dsEnvelopeInformationJson -> {}", dsEnvelopeInformationJson);
					}

					List<String> pathList = ReportDataRuleUtil.preparePathList(envelopeMessageDefinition.getBatchId(),
							envelopeMessageDefinition.getProcessId(), dsEnvelopeInformationJson, ruleEngineDefinition,
							scriptEngine, "$.dsEnvelopeDefinitions[*]");

					String tableName = ruleEngineDefinition.getApiDataTableName();

					if (StringUtils.isEmpty(tableName)) {

						throw new ListenerProcessingException("TableName cannot be empty in " + dsCacheManager
								.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DOCMIGRATION_RULE_ENGINE_PATH));
					}

					migrationDataDefinition.setApiDataTableName(tableName);
					migrationDataDefinition.setProcessId(envelopeMessageDefinition.getProcessId());
					migrationDataDefinition.setBatchId(envelopeMessageDefinition.getBatchId());

					if (null != pathList && !pathList.isEmpty()) {

						List<List<ReportData>> reportRowsList = new ArrayList<List<ReportData>>(pathList.size());

						pathList.forEach(path -> {

							reportRowsList.add(
									ReportDataRuleUtil.prepareColumnDataMap(ruleEngineDefinition.getOutputColumns(),
											dsEnvelopeInformationJson, path, envelopeMessageDefinition.getProcessId(),
											envelopeMessageDefinition.getBatchId(), scriptEngine));
						});

						migrationDataDefinition.setRowDataMapList(
								convertListToMap(reportRowsList, envelopeMessageDefinition.getProcessId(),
										envelopeMessageDefinition.getBatchId(), getAccountId()));
					} else {

						log.info(
								"No Records to process to save in migration table for RecordIds -> {} in processId -> {} and batchId -> {}",
								envelopeMessageDefinition.getRecordIds(), envelopeMessageDefinition.getProcessId(),
								envelopeMessageDefinition.getBatchId());
					}

				} else {

					log.error(
							"-------------------- NO DATA RETURNED FOR RecordIds -> {} in processId -> {} and batchId -> {} --------------------",
							envelopeMessageDefinition.getRecordIds(), envelopeMessageDefinition.getProcessId(),
							envelopeMessageDefinition.getBatchId());
				}

			} catch (JsonParseException e) {

				log.error("JsonParseException -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (JsonMappingException e) {

				log.error("JsonMappingException -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (IOException e) {

				log.error("IOException -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (NoSuchMethodException e) {

				log.error("NoSuchMethodException -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (ScriptException e) {

				log.error("ScriptException -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (Throwable e) {

				log.error("Throwable -> {} caught for processId -> {} and batchId -> {}", e,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			}

			return migrationDataDefinition;
		}, xmlTaskExecutor).handle((migrationDataDefinition, exp) -> {

			if (null != exp) {

				exp.printStackTrace();
				log.error("Exp -> {} occurred in prepareMigrationDataSet for processId -> {} and batchId -> {}", exp,
						envelopeMessageDefinition.getProcessId(), envelopeMessageDefinition.getBatchId());
				throw new ListenerProcessingException(exp.getMessage());
			}
			return migrationDataDefinition;
		});

	}

	private String getAccountId() {

		return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);
	}

	private List<Map<String, Object>> convertListToMap(List<List<ReportData>> reportRowsList, String processId,
			String batchId, String accountId) {

		List<Map<String, Object>> rowDataMapList = new ArrayList<>(reportRowsList.size());
		for (List<ReportData> reportDataList : reportRowsList) {

			Map<String, Object> columnDataMap = new HashMap<String, Object>();

			columnDataMap.put("recordid", UUID.randomUUID().toString());
			columnDataMap.put("createddatetime", LocalDateTime.now().toString());
			columnDataMap.put("createdby", "CORE_PARALLEL_DOC_MIGRATION_QUEUE");
			columnDataMap.put("accountid", accountId); // This should be set by ruleEngine as well to override default
														// value
			columnDataMap.put("batchid", batchId);
			columnDataMap.put("processid", processId);

			reportDataList.forEach(reportData -> {

				columnDataMap.put(reportData.getReportColumnName(), reportData.getReportColumnValue());
			});

			rowDataMapList.add(columnDataMap);
		}

		return rowDataMapList;
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
}