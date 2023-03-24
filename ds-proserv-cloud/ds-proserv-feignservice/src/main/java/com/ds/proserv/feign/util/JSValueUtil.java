package com.ds.proserv.feign.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.PathParamDataType;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.ReportData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSValueUtil {

	public static <T> T extractJSExpression(Map<String, Object> inputParams, List<PathParam> pathParams,
			String parentData, ScriptEngine engine, String expression, Object pathValue, String accountId,
			String batchId, String nextUri, List<ReportData> reportColumnsData, Class<T> returnType, String processId)
			throws ScriptException, NoSuchMethodException {

		T expressionResult = null;

		List<Object> objectFunctionParamList = new ArrayList<Object>(pathParams.size());

		if (null != pathValue && !StringUtils.isEmpty(pathValue)) {

			objectFunctionParamList.add(pathValue);
		}

		String functionName = extractPathParams(inputParams, pathParams, parentData, objectFunctionParamList,
				reportColumnsData);

		try {

			engine.eval(expression);
			Invocable invocable = (Invocable) engine;

			if (null != objectFunctionParamList && !objectFunctionParamList.isEmpty()) {

				Object[] objectFunctionParamArr = new Object[objectFunctionParamList.size()];
				expressionResult = returnType.cast(invocable.invokeFunction(functionName,
						objectFunctionParamList.toArray(objectFunctionParamArr)));
			} else {
				expressionResult = returnType.cast(invocable.invokeFunction(functionName));
			}

		} catch (NoSuchMethodException exp) {

			log.error(
					"NoSuchMethodException thrown for functionName {}, nextUri -> {}, accountId -> {} and batchId -> {}",
					functionName, nextUri, accountId, batchId);
			exp.printStackTrace();

			throw exp;
		} catch (ScriptException exp) {

			log.error("ScriptException thrown for functionName {}, nextUri -> {}, accountId -> {} and batchId -> {}",
					functionName, nextUri, accountId, batchId);
			throw exp;
		}
		return expressionResult;
	}

	public static String extractPathParams(Map<String, Object> inputParams, List<PathParam> pathParams,
			String parentData, List<Object> objectFunctionParamList, List<ReportData> reportColumnsData) {

		String functionName = null;
		for (PathParam pathParam : pathParams) {

			if ("functionName".equalsIgnoreCase(pathParam.getParamName())) {

				functionName = pathParam.getParamValue();
				continue;
			}

			Object paramValue = null;
			ReportData reportData = null;

			if (null != reportColumnsData && !reportColumnsData.isEmpty()) {

				reportData = findReportData(reportColumnsData, pathParam.getParamName());
			}

			if (pathParam.getParamName().toLowerCase().contains("input") && null != inputParams) {

				paramValue = inputParams.get(pathParam.getParamName());
			} else if (!StringUtils.isEmpty(pathParam.getParamValue())) {

				paramValue = pathParam.getParamValue();
			} else if (null != reportData) {

				paramValue = reportData.getReportColumnValue();
			} else if (!StringUtils.isEmpty(parentData)) {

				paramValue = parentData;
			}

			if (!StringUtils.isEmpty(pathParam.getParamDataType()) && !StringUtils.isEmpty(paramValue)) {

				PathParamDataType pathParamDataTypeEnum = EnumUtils.getEnum(PathParamDataType.class,
						pathParam.getParamDataType().toUpperCase());

				switch (pathParamDataTypeEnum) {

				case DATEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATETIMEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATETIME:

					paramValue = DateTimeUtil.convertToSQLDateTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATE:

					paramValue = DateTimeUtil.convertToSQLDateFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case ARRAY:
					String[] paramArray = ((String) paramValue).split(",");
					paramValue = paramArray;
					break;
				default:
					log.warn(
							"<<<<<<<<<<<<<<<<<<<< Wrong ParamDataType -> {} in evaluateJSFunction >>>>>>>>>>>>>>>>>>>>",
							pathParam.getParamDataType());

				}
			}

			if (!StringUtils.isEmpty(paramValue)) {

				objectFunctionParamList.add(paramValue);
			}

		}
		return functionName;
	}

	private static ReportData findReportData(List<ReportData> reportColumnsData, String paramName) {

		return reportColumnsData.stream()
				.filter(columnsData -> paramName.equalsIgnoreCase(columnsData.getReportColumnName())).findAny()
				.orElse(null);
	}
}