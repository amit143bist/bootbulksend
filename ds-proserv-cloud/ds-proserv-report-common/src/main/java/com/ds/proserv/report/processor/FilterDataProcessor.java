package com.ds.proserv.report.processor;

import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.JSFunctionProcessException;
import com.ds.proserv.feign.report.domain.Filter;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.util.FilterUtil;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilterDataProcessor {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	public List<String> createPathList(Filter filter, Configuration pathConfiguration, String json,
			Map<String, Object> inputParams) {

		return FilterUtil.createPathList(filter, pathConfiguration, json, inputParams);
	}

	public Object processFilterData(Configuration pathConfiguration, List<String> pathList, Filter filter, String json,
			Map<String, Object> inputParams, DocumentContext docContext, String accountId, String batchId,
			String nextUri, List<ReportData> reportColumnsData, String processId) {

		try {

			return FilterUtil.processFilterData(pathConfiguration, pathList, filter, json, inputParams, docContext,
					accountId, batchId, nextUri, reportColumnsData, processId, scriptEngine);

		} catch (NoSuchMethodException exp) {

			log.error("NoSuchMethodException -> {} caught for processId -> {} and batchId -> {}", exp, processId,
					batchId);
			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(accountId, batchId, processId, exp, FailureCode.ERROR_103,
						FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION);

			} else {

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_103.toString(),
						exp.getMessage(), FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp, processId);
			}

			exp.printStackTrace();

			throw new JSFunctionProcessException("NoSuchMethodException " + FailureCode.ERROR_103.toString()
					+ FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp);
		} catch (ScriptException exp) {

			log.error("ScriptException -> {} caught for processId -> {} and batchId -> {}", exp, processId, batchId);
			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(accountId, batchId, processId, exp, FailureCode.ERROR_104,
						FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION);
			} else {

				batchDataService.createFailureRecord(accountId, batchId, FailureCode.ERROR_104.toString(),
						exp.getMessage(), FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp, processId);
			}
			exp.printStackTrace();

			throw new JSFunctionProcessException("ScriptException " + FailureCode.ERROR_104.toString()
					+ FailureStep.EVALUATE_JS_FUNCTION_EXPRESSION.toString(), exp);
		}
	}

}