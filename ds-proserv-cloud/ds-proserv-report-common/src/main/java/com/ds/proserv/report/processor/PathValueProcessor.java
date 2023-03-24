package com.ds.proserv.report.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ds.proserv.feign.report.domain.CommonPathData;
import com.ds.proserv.feign.util.PathValueUtil;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PathValueProcessor {

	public Object evaluatePathValue(CommonPathData commonPathData, DocumentContext docContext, String accountId,
			String batchId, String nextUri, Map<String, Object> inputParams) {

		if (null == commonPathData.getColumnPath()) {

			return commonPathData.getColumnPath();
		}

		log.debug(
				"OuterPath -> {}, columnDataType -> {}, columnPath -> {}, columnDataPattern -> {} and fullPath -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				commonPathData.getOuterPath(), commonPathData.getColumnDataType(), commonPathData.getColumnPath(),
				commonPathData.getColumnDataPattern(), commonPathData.getOuterPath() + commonPathData.getColumnPath(),
				nextUri, accountId, batchId);

		return PathValueUtil.evaluateValueFromJSONPath(commonPathData, docContext, inputParams);
	}

}