package com.ds.proserv.bulksend.common.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.bulksend.common.client.BulkSendDataSourceClient;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchItem;
import com.ds.proserv.bulksend.common.processor.DSEnvCreateAsyncAPIProcessor;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidMessageException;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.bulksend.sourcedata.domain.BulkSendPrepareDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendMessageDefinition;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.ds.proserv.feign.util.ReportDataRuleUtil;
import com.ds.proserv.send.common.helper.SendQueryHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSendService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired
	private SendQueryHelper sendQueryHelper;

	@Autowired
	private BulkSendDataSourceClient bulkSendDataSourceClient;

	@Autowired
	private DSEnvCreateAsyncAPIProcessor dsEnvCreateAsyncAPIProcessor;

	public EnvelopeBatchItem processBulkSendMessage(BulkSendMessageDefinition bulkSendMessageDefinition) {

		EnvelopeBatchItem envelopeBatchItemResponse = null;

		PageInformation pageInformation = preparePageInformation(bulkSendMessageDefinition);

		log.info("Calling bulkSendDataSourceClient for processId -> {} and batchId -> {}",
				bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
		BulkSendPrepareDefinition bulkSendPrepareDefinition = bulkSendDataSourceClient
				.findBulkSendSelectedRows(pageInformation).getBody();

		if (null != bulkSendPrepareDefinition && !bulkSendPrepareDefinition.getSelectedRows().isEmpty()) {

			try {

				String bulkSendPrepareDefinitionJson = objectMapper.writeValueAsString(bulkSendPrepareDefinition);

				RuleEngineDefinition ruleEngineDefinition = bulkSendMessageDefinition.getRuleEngineDefinition();

				List<String> pathList = ReportDataRuleUtil.preparePathList(bulkSendMessageDefinition.getBatchId(),
						bulkSendMessageDefinition.getProcessId(), bulkSendPrepareDefinitionJson, ruleEngineDefinition,
						scriptEngine, "$.selectedRows[*]");

				if (null == pathList || pathList.isEmpty()) {

					log.error("PathList cannot be empty for processId -> {} and batchId -> {}",
							bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());

					throw new InvalidMessageException(
							"Null or Empty pathList returned for queryIdentifier "
									+ bulkSendMessageDefinition.getQueryIdentifier() + " for selectRecordDataQueryType "
									+ bulkSendMessageDefinition.getSelectRecordDataQueryType(),
							FailureCode.ERROR_221, FailureStep.ASYNC_BULKSEND_PREPAREPATHLIST);
				}

				List<List<ReportData>> reportRowsList = new ArrayList<List<ReportData>>(pathList.size());
				pathList.forEach(path -> {

					reportRowsList.add(ReportDataRuleUtil.prepareColumnDataMap(ruleEngineDefinition.getOutputColumns(),
							bulkSendPrepareDefinitionJson, path, null, null, scriptEngine));
				});

				List<String> headerRow = new ArrayList<String>();
				reportRowsList.get(0).forEach(row1 -> {

					headerRow.add(row1.getReportColumnName());
				});

				String bulkSendHeaderLine = String.join(AppConstants.COMMA_DELIMITER, headerRow);

				List<String[]> bulkSendRowDataList = createRowColumnArray(reportRowsList);

				EnvelopeBatchItem envelopeBatchItemToProcess = new EnvelopeBatchItem();
				envelopeBatchItemToProcess.setRowDataList(bulkSendRowDataList);

				envelopeBatchItemResponse = dsEnvCreateAsyncAPIProcessor.process(envelopeBatchItemToProcess,
						bulkSendMessageDefinition.getBaseUri(),
						bulkSendMessageDefinition.getDraftEnvelopeIdOrTemplateId(),
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId(),
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getUseTemplate(),
						bulkSendHeaderLine, bulkSendMessageDefinition.getAccountId(),
						bulkSendMessageDefinition.getUserId());

				List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
				if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

					String paramName = bulkSendMessageDefinition.getSelectRecordDataQueryTypePrimaryKeyName();

					String commaSeparatedRecordIds = DSUtil.extractPageQueryParamValue(pageQueryParams, paramName);

					if (null != bulkSendPrepareDefinition.getRecordIds()
							&& !bulkSendPrepareDefinition.getRecordIds().isEmpty()) {

						List<String> fetchedRecordIds = bulkSendPrepareDefinition.getRecordIds().stream()
								.map(Object::toString).collect(Collectors.toList());

						List<String> originalSentRecordIds = Stream
								.of(commaSeparatedRecordIds.split(AppConstants.COMMA_DELIMITER))
								.collect(Collectors.toList());

						if (null != fetchedRecordIds && null != originalSentRecordIds
								&& fetchedRecordIds.size() != originalSentRecordIds.size()) {

							log.info(
									"Original Sent size {} is different from fetchedSize {} in procesId {} and queryIdentifier -> {}",
									originalSentRecordIds.size(), fetchedRecordIds.size(),
									bulkSendMessageDefinition.getProcessId(),
									bulkSendMessageDefinition.getQueryIdentifier());
							commaSeparatedRecordIds = String.join(AppConstants.COMMA_DELIMITER, fetchedRecordIds);
						}
					}

					if (!StringUtils.isEmpty(commaSeparatedRecordIds)) {

						envelopeBatchItemResponse.setCommaSeparatedRecordIds(commaSeparatedRecordIds);
						envelopeBatchItemResponse.setTotalRecordIdsProcessed(
								Long.valueOf(commaSeparatedRecordIds.split(AppConstants.COMMA_DELIMITER).length));
					}
				}

			} catch (JsonParseException e) {

				log.error("JsonParseException -> {} caught for processId -> {} and batchId -> {}", e,
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (JsonMappingException e) {

				log.error("JsonMappingException -> {} caught for processId -> {} and batchId -> {}", e,
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (IOException e) {

				log.error("IOException -> {} caught for processId -> {} and batchId -> {}", e,
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (NoSuchMethodException e) {

				log.error("NoSuchMethodException -> {} caught for processId -> {} and batchId -> {}", e,
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			} catch (ScriptException e) {

				log.error("ScriptException -> {} caught for processId -> {} and batchId -> {}", e,
						bulkSendMessageDefinition.getProcessId(), bulkSendMessageDefinition.getBatchId());
				e.printStackTrace();

				throw new ListenerProcessingException(e.getMessage());
			}

		} else {

			log.error("Null or Empty record data returned for queryIdentifier {} for selectRecordDataQueryType {}",
					bulkSendMessageDefinition.getQueryIdentifier(),
					bulkSendMessageDefinition.getSelectRecordDataQueryType());

			throw new InvalidMessageException(
					"Null or Empty record data returned for queryIdentifier "
							+ bulkSendMessageDefinition.getQueryIdentifier() + " for selectRecordDataQueryType "
							+ bulkSendMessageDefinition.getSelectRecordDataQueryType(),
					FailureCode.ERROR_220, FailureStep.ASYNC_BULKSEND_PULLRECORDDATA);
		}

		return envelopeBatchItemResponse;

	}

	private List<String[]> createRowColumnArray(List<List<ReportData>> reportRowsList) {

		List<String[]> bulkSendRowList = new ArrayList<String[]>();
		reportRowsList.forEach(reportRows -> {

			List<String> eachColumnList = new ArrayList<String>();
			reportRows.forEach(report -> {

				if (null != report.getReportColumnValue()) {

					eachColumnList.add(report.getReportColumnValue().toString());
				} else {

					eachColumnList.add(null);
				}

			});

			String[] eachColumnArr = new String[eachColumnList.size()];
			bulkSendRowList.add(eachColumnList.toArray(eachColumnArr));
		});

		return bulkSendRowList;
	}

	private PageInformation preparePageInformation(BulkSendMessageDefinition bulkSendMessageDefinition) {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue(bulkSendMessageDefinition.getQueryIdentifier());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue(bulkSendMessageDefinition.getSelectRecordDataQueryType());

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(bulkSendMessageDefinition.getSelectRecordDataQueryTypePrimaryKeyName());

		List<String> recordIds = bulkSendMessageDefinition.getRecordIds();
		// proserv.send.{processId}.ignorelist.appids
		List<String> ignoreAppIds = sendQueryHelper.getIgnoreAppIds(bulkSendMessageDefinition.getProcessId(),
				PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		if (null != ignoreAppIds && !ignoreAppIds.isEmpty()) {

			recordIds.removeAll(ignoreAppIds);
		}

		String commaSeparatedStr = String.join(AppConstants.COMMA_DELIMITER, recordIds);

		pageQueryParam.setParamValue(commaSeparatedStr);
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}

	public PageInformation preparePageInformationForRecordValidation(List<Object> recordIds) {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RECORDIDS_PARAM_NAME);

		String commaSeparatedStr = recordIds.stream().map(Object::toString)
				.collect(Collectors.joining(AppConstants.COMMA_DELIMITER));

		pageQueryParam.setParamValue(commaSeparatedStr);
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		return pageInformation;
	}
}