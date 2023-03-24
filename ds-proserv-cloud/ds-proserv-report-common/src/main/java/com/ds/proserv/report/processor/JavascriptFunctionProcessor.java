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
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.util.JSValueUtil;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.queue.service.ReportQueueService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavascriptFunctionProcessor {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	public Boolean evaluateJSFunction(Map<String, Object> inputParams, List<PathParam> pathParams, String parentData,
			ScriptEngine engine, String expression, Object pathValue, String accountId, String batchId, String nextUri,
			List<ReportData> reportColumnsData, String processId) {

		log.info(
				"In evaluateJSFunction inputParams -> {}, pathParams -> {}, parentData -> {}, expression -> {} and pathValue -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				inputParams, pathParams, parentData, expression, pathValue, nextUri, accountId, batchId);

		return extractJSExpression(inputParams, pathParams, parentData, engine, expression, pathValue, accountId,
				batchId, nextUri, reportColumnsData, Boolean.class, processId);
	}

	public Object evaluateJSFunctionExpression(Map<String, Object> inputParams, List<PathParam> pathParams,
			String parentData, ScriptEngine engine, String expression, Object pathValue, String accountId,
			String batchId, String nextUri, List<ReportData> reportColumnsData, String processId) {

		log.info(
				"In evaluateJSFunctionExpression inputParams -> {}, pathParams -> {}, parentData -> {}, expression -> {} and pathValue -> {} for nextUri -> {}, accountId -> {} and batchId -> {}",
				inputParams, pathParams, parentData, expression, pathValue, nextUri, accountId, batchId);

		return extractJSExpression(inputParams, pathParams, parentData, engine, expression, pathValue, accountId,
				batchId, nextUri, reportColumnsData, Object.class, processId);

	}

	private <T> T extractJSExpression(Map<String, Object> inputParams, List<PathParam> pathParams, String parentData,
			ScriptEngine engine, String expression, Object pathValue, String accountId, String batchId, String nextUri,
			List<ReportData> reportColumnsData, Class<T> returnType, String processId) {

		T expressionResult = null;

		try {

			expressionResult = JSValueUtil.extractJSExpression(inputParams, pathParams, parentData, engine, expression,
					pathValue, accountId, batchId, nextUri, reportColumnsData, returnType, processId);

		} catch (NoSuchMethodException exp) {

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
		return expressionResult;
	}

}