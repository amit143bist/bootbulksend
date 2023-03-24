package com.ds.proserv.feign.util;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.feign.report.domain.CommonPathData;
import com.ds.proserv.feign.report.domain.DecorateOutput;
import com.ds.proserv.feign.report.domain.Filter;
import com.ds.proserv.feign.report.domain.OutputColumn;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class ReportDataRuleUtil {

	private static Configuration docContextPathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	private static Configuration pathConfiguration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS,
			Option.AS_PATH_LIST, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	public static List<String> preparePathList(String batchId, String processId, String responseJson,
			RuleEngineDefinition ruleEngineDefinition, ScriptEngine scriptEngine, String defaultPathListNode)
			throws NoSuchMethodException, ScriptException {

		List<String> pathList = null;
		List<Filter> commonFilters = ruleEngineDefinition.getCommonFilters();

		DocumentContext docContext = JsonPath.using(docContextPathConfiguration).parse(responseJson);

		if (null != commonFilters && !commonFilters.isEmpty()) {

			for (int i = 0; i < commonFilters.size(); i++) {

				if (i == 0 && null == pathList) {

					pathList = FilterUtil.createPathList(commonFilters.get(i), pathConfiguration, responseJson, null);
				} else {

					FilterUtil.processFilterData(docContextPathConfiguration, pathList, commonFilters.get(i),
							responseJson, null, docContext, null, batchId, null, null, processId, scriptEngine);
				}
			}
		} else {

			pathList = JsonPath.using(pathConfiguration).parse(responseJson).read(defaultPathListNode);

		}

		return pathList;
	}

	public static List<ReportData> prepareColumnDataMap(List<OutputColumn> outputColumnList, String responseJson,
			String path, String processId, String batchId, ScriptEngine engine) {

		List<ReportData> reportColumnsDataList = new ArrayList<ReportData>();

		DocumentContext docContext = JsonPath.using(docContextPathConfiguration).parse(responseJson);

		outputColumnList.forEach(outputColumn -> {

			CommonPathData commonPathData = new CommonPathData();
			commonPathData.setOuterPath(path);
			commonPathData.setColumnDataType(outputColumn.getColumnDataType());
			commonPathData.setColumnPath(outputColumn.getColumnPath());
			commonPathData.setColumnDataPattern(outputColumn.getColumnDataPattern());
			commonPathData.setArrayIndex(outputColumn.getColumnDataArrayIndex());
			commonPathData.setMapKey(outputColumn.getColumnDataMapKey());
			commonPathData.setOutputDataPattern(outputColumn.getColumnOutputDataPattern());
			commonPathData.setStartIndex(outputColumn.getStartIndex());
			commonPathData.setEndIndex(outputColumn.getEndIndex());
			commonPathData.setOutputDelimiter(outputColumn.getOutputDelimiter());
			commonPathData.setTimeZone(outputColumn.getTimeZone());

			Object pathValue = PathValueUtil.evaluateValueFromJSONPath(commonPathData, docContext, null);

			ReportData reportData = new ReportData();
			reportData.setReportColumnName(outputColumn.getColumnName());

			if (null != outputColumn.getDecorateOutput()) {

				DecorateOutput decorateOutput = outputColumn.getDecorateOutput();
				String functionExpression = decorateOutput.getOutputPatternExpression();
				try {
					Object columnDecoratedValue = JSValueUtil.extractJSExpression(null, decorateOutput.getPathParams(),
							null, engine, functionExpression, pathValue, null, batchId, null, reportColumnsDataList,
							Object.class, processId);

					reportData.setReportColumnValue(columnDecoratedValue);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();

					throw new ListenerProcessingException(e.getMessage());
				} catch (ScriptException e) {
					e.printStackTrace();

					throw new ListenerProcessingException(e.getMessage());
				}

			} else {

				reportData.setReportColumnValue(pathValue);
			}

			reportColumnsDataList.add(reportData);
		});

		return reportColumnsDataList;
	}
}