package com.ds.proserv.feign.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.report.domain.PathParam;

public class PathParamUtil {

	public static Map<String, Object> prepareParamValues(Map<String, Object> inputParams, List<PathParam> sqlParamList) {

		Map<String, Object> paramsMap = new HashMap<String, Object>(sqlParamList.size());

		for (PathParam pathParam : sqlParamList) {

			Object paramValue = null;
			if ("DateEpoch".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("DateTimeEpoch".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("Date".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToISOLocalDate((String) inputParams.get(pathParam.getParamName()),
							pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToISOLocalDate((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("DateTime".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = DateTimeUtil.convertToISOLocalDateTime(
							(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern());

				} else {

					paramValue = DateTimeUtil.convertToISOLocalDateTime((String) pathParam.getParamValue(),
							pathParam.getParamPattern());
				}

			} else if ("SqlDate".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
							DateTimeUtil.convertToEpochTimeFromDateAsString(
									(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern()),
							0, ZoneOffset.UTC));

				} else {

					paramValue = Timestamp
							.valueOf(
									LocalDateTime.ofEpochSecond(
											DateTimeUtil.convertToEpochTimeFromDateAsString(
													(String) pathParam.getParamValue(), pathParam.getParamPattern()),
											0, ZoneOffset.UTC));
				}

			} else if ("SqlDateTime".equalsIgnoreCase(pathParam.getParamDataType())) {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
							DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
									(String) inputParams.get(pathParam.getParamName()), pathParam.getParamPattern()),
							0, ZoneOffset.UTC));

				} else {

					paramValue = Timestamp
							.valueOf(LocalDateTime.ofEpochSecond(
									DateTimeUtil.convertToEpochTimeFromDateTimeAsString(
											(String) pathParam.getParamValue(), pathParam.getParamPattern()),
									0, ZoneOffset.UTC));

				}

			} else {

				if (pathParam.getParamName().toLowerCase().contains("input")) {

					paramValue = inputParams.get(pathParam.getParamName());
				} else {

					paramValue = pathParam.getParamValue();
				}
			}

			paramsMap.put(pathParam.getParamName(), paramValue);
		}
		return paramsMap;
	}
}