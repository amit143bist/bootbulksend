package com.ds.proserv.feign.util;

import java.util.*;

import javax.script.Bindings;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.PathParamDataType;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.report.domain.PathParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BindingParamDataUtil {

	public static void populateBindingParams(Map<String, Object> inputParams, List<PathParam> pathParams,
			Bindings bindings, String parentData, String nextUri, String accountId, String batchId) {

		log.info(
				"InputParams -> {}, parentData -> {}, nextUri -> {}, accountId -> {}, batchId -> {} and PathParams - {}",
				inputParams, parentData, nextUri, accountId, batchId, pathParams);

		for (PathParam pathParam : pathParams) {

			Object paramValue = null;

			if (pathParam.getParamName().toLowerCase().contains("input")) {

				paramValue = inputParams.get(pathParam.getParamName());
			} else if (!StringUtils.isEmpty(pathParam.getParamValue())) {

				paramValue = pathParam.getParamValue();
			} else if (!StringUtils.isEmpty(parentData)) {

				paramValue = pathParam.getParamValue();
			}

			if (!StringUtils.isEmpty(pathParam.getParamDataType())) {

				PathParamDataType pathParamDataTypeEnum = EnumUtils.getEnum(PathParamDataType.class,
						pathParam.getParamDataType().toUpperCase());
				switch (pathParamDataTypeEnum) {

				case DATETIMEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateTimeAsString((String) paramValue,
							pathParam.getParamPattern());
					break;
				case DATEASEPOCHTIME:

					paramValue = DateTimeUtil.convertToEpochTimeFromDateAsString((String) paramValue,
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
				default:
					log.warn(
							"<<<<<<<<<<<<<<<<<<<< Wrong ParamDataType -> {} in populateBindingParams >>>>>>>>>>>>>>>>>>>>",
							pathParam.getParamDataType());
				}
			}

			bindings.put(pathParam.getParamName(), paramValue);
		}

	}

}