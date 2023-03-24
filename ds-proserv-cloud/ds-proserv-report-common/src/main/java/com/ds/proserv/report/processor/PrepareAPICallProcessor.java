package com.ds.proserv.report.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.dsapi.domain.DSErrorMessage;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.auth.cache.DSAuthorizationCache;
import com.ds.proserv.report.auth.domain.JWTParams;
import com.ds.proserv.report.db.service.BatchDataService;
import com.ds.proserv.report.queue.service.ReportQueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrepareAPICallProcessor {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private DSAuthorizationCache dsAuthorizationCache;

	@Autowired
	private ReportQueueService queueService;

	@Autowired
	private BatchDataService batchDataService;

	public <T> T callPrepareAPI(PrepareDataAPI prepareAPI, String accountId, String nextUri, String batchId,
			Class<T> returnType, String processId) {

		return callPrepareAPI(prepareAPI, accountId, null, batchId, nextUri, returnType, processId);
	}

	public <T> T callPrepareAPI(PrepareDataAPI prepareAPI, String accountId, Map<String, Object> inputParams,
			String batchId, String nextUri, Class<T> returnType, String processId) {

		return callPrepareAPI(prepareAPI, accountId, inputParams, null, nextUri, batchId, returnType, processId);
	}

	public <T> T callPrepareAPI(PrepareDataAPI prepareAPI, String accountId, Map<String, Object> inputParams,
			Object pathValue, String batchId, Class<T> returnType, String processId) {

		return callPrepareAPI(prepareAPI, accountId, inputParams, pathValue, null, batchId, returnType, processId);
	}

	public <T> T callPrepareAPI(PrepareDataAPI prepareAPI, String accountId, Map<String, Object> inputParams,
			Object pathValue, String nextUri, String batchId, Class<T> returnType, String processId,
			String reqPropertyValue) {

		JWTParams jwtParams = new JWTParams(prepareAPI.getApiRunArgs());
		AuthenticationRequest authenticationRequest = new AuthenticationRequest(accountId, jwtParams.getJwtScopes(),
				jwtParams.getJwtUserId(), prepareAPI.getApiCategory(), jwtParams.getAuthClientId(),
				jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(), jwtParams.getAuthPassword(),
				jwtParams.getAuthBaseUrl(), prepareAPI.getApiId());

		APICategoryType apiCategoryType = ReportAppUtil.getAPICategoryType(authenticationRequest.getApiCategory());

		if ((StringUtils.isEmpty(accountId) || AppConstants.NOT_AVAILABLE_CONST.equalsIgnoreCase(accountId))) {

			PathParam accountIdParam = ReportAppUtil.findPathParam(prepareAPI.getApiParams(),
					AppConstants.DS_ACCOUNT_ID);

			if (null != accountIdParam && !StringUtils.isEmpty(accountIdParam.getParamValue())) {

				authenticationRequest.setAccountGuid(accountIdParam.getParamValue());
			} else {

				if ((apiCategoryType == APICategoryType.ESIGNAPI || apiCategoryType == APICategoryType.CLICKAPI
						|| apiCategoryType == APICategoryType.ROOMSAPI)) {

					log.error("AccountId is not properly set for apiId -> {} and apiCategory -> {}",
							prepareAPI.getApiId(), prepareAPI.getApiCategory());
					throw new InvalidInputException("AccountId is not properly set for apiId -> "
							+ prepareAPI.getApiId() + " for apiCategory -> " + prepareAPI.getApiCategory());
				}
			}
		}

		String fullUri = createFullUriToCallDSApi(prepareAPI, authenticationRequest);

		log.info("FullUri -> {}, pathValue -> {} and inputParams -> {} for accountId -> {} and batchId -> {}", fullUri,
				pathValue, inputParams, accountId, batchId);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		String msgBody = "{" + "\"" + prepareAPI.getOutputApiReqProperty() + "\"" + ":" + "\"" + reqPropertyValue + "\""
				+ "}";

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				prepareAPI.getApiAcceptType(), prepareAPI.getApiContentType(), msgBody);

		ResponseEntity<T> responseEntity = null;
		try {
			responseEntity = prepareDataForDSApiCall(prepareAPI, accountId, inputParams, pathValue, nextUri, returnType,
					fullUri, HttpMethod.valueOf(prepareAPI.getApiOperationType()), httpEntity);

			checkAPIHourlyLimit(responseEntity);
			return responseEntity.getBody();
		} catch (HttpClientErrorException exp) {

			log.error("HttpClientErrorException -> {} happened while calling update for primaryIds -> {}",
					exp.getMessage(), reqPropertyValue);
			return processDSApiCallErrorException(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, fullUri, exp, processId, httpEntity);

		} catch (Exception exp) {

			log.error("Exception -> {} happened while calling update for primaryIds -> {}", exp.getMessage(),
					reqPropertyValue);
			log.error(
					"Exception -> {} occurred with cause -> {} and message -> {} for accountId -> {} and fullUri -> {}",
					exp, exp, exp.getMessage(), accountId, fullUri);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(accountId + "_" + fullUri, batchId, processId, exp,
						FailureCode.ERROR_107, FailureStep.PREPARE_API_URI);

			} else {

				batchDataService.createFailureRecord(accountId + "_" + fullUri, batchId,
						FailureCode.ERROR_107.toString(), exp.getMessage(), FailureStep.PREPARE_API_URI.toString(), exp,
						processId);

			}

			exp.printStackTrace();

			throw exp;

		}

	}

	private String createFullUriToCallDSApi(PrepareDataAPI prepareAPI, AuthenticationRequest authenticationRequest) {

		if (!ReportAppUtil.isValidURL(prepareAPI.getOutputApiUri())) {

			return dsAuthorizationCache.requestBaseUrl(authenticationRequest) + prepareAPI.getOutputApiUri();

		} else {

			return prepareAPI.getApiUri();
		}

	}

	public <T> ResponseEntity<T> callPrepareAPIWithRespHeader(PrepareDataAPI prepareAPI, String accountId,
			Map<String, Object> inputParams, Object pathValue, String nextUri, String batchId, Class<T> returnType,
			String processId) {

		String fullUri = null;
		HttpEntity<String> httpEntity = null;
		ResponseEntity<T> responseEntity = null;

		try {

			JWTParams jwtParams = new JWTParams(prepareAPI.getApiRunArgs());
			AuthenticationRequest authenticationRequest = new AuthenticationRequest(accountId, jwtParams.getJwtScopes(),
					jwtParams.getJwtUserId(), prepareAPI.getApiCategory(), jwtParams.getAuthClientId(),
					jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(), jwtParams.getAuthPassword(),
					jwtParams.getAuthBaseUrl(), prepareAPI.getApiId());

			fullUri = createFullUriToCallDSApi(prepareAPI, nextUri, authenticationRequest);

			log.info("FullUri -> {}, pathValue -> {} and inputParams -> {} for accountId -> {} and batchId -> {}",
					fullUri, pathValue, inputParams, accountId, batchId);

			AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

			httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse, prepareAPI.getApiAcceptType(),
					prepareAPI.getApiContentType(), null);

			responseEntity = prepareDataForDSApiCall(prepareAPI, accountId, inputParams, pathValue, nextUri, returnType,
					fullUri, HttpMethod.valueOf(prepareAPI.getApiOperationType()), httpEntity);

			if (null != responseEntity) {

				// Below code is for checking API hourly limit
				checkAPIHourlyLimit(responseEntity);

			}
		} catch (HttpClientErrorException exp) {

			return processAPIHourlyLimitCheckWithHeader(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, fullUri, exp, processId);
		} catch (Exception exp) {

			log.error(
					"Exception -> {} occurred with cause -> {} and message -> {} for accountId -> {} and fullUri -> {}",
					exp, exp, exp.getMessage(), accountId, fullUri);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(accountId + "_" + fullUri, batchId, processId, exp,
						FailureCode.ERROR_107, FailureStep.PREPARE_API_URI);

			} else {

				batchDataService.createFailureRecord(accountId + "_" + fullUri, batchId,
						FailureCode.ERROR_107.toString(), exp.getMessage(), FailureStep.PREPARE_API_URI.toString(), exp,
						processId);

			}

			exp.printStackTrace();
			throw exp;

		}

		return responseEntity;
	}

	private <T> void checkAPIHourlyLimit(ResponseEntity<T> responseEntity) {

		String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);
		if (StringUtils.isEmpty(apiThresholdLimitPercentAsStr)) {

			ApiLimitUtil.readApiHourlyLimitData(responseEntity.getHeaders(), 90);
		} else {

			ApiLimitUtil.readApiHourlyLimitData(responseEntity.getHeaders(),
					Integer.valueOf(apiThresholdLimitPercentAsStr));
		}
	}

	public <T> T callPrepareAPI(PrepareDataAPI prepareAPI, String accountId, Map<String, Object> inputParams,
			Object pathValue, String nextUri, String batchId, Class<T> returnType, String processId) {

		T json = null;
		String fullUri = null;
		HttpEntity<String> httpEntity = null;
		ResponseEntity<T> responseEntity = null;

		try {

			JWTParams jwtParams = new JWTParams(prepareAPI.getApiRunArgs());
			AuthenticationRequest authenticationRequest = new AuthenticationRequest(accountId, jwtParams.getJwtScopes(),
					jwtParams.getJwtUserId(), prepareAPI.getApiCategory(), jwtParams.getAuthClientId(),
					jwtParams.getAuthClientSecret(), jwtParams.getAuthUserName(), jwtParams.getAuthPassword(),
					jwtParams.getAuthBaseUrl(), prepareAPI.getApiId());

			fullUri = createFullUriToCallDSApi(prepareAPI, nextUri, authenticationRequest);

			log.info("FullUri -> {}, pathValue -> {} and inputParams -> {} for accountId -> {} and batchId -> {}",
					fullUri, pathValue, inputParams, accountId, batchId);

			AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

			httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse, prepareAPI.getApiAcceptType(),
					prepareAPI.getApiContentType(), null);

			responseEntity = prepareDataForDSApiCall(prepareAPI, accountId, inputParams, pathValue, nextUri, returnType,
					fullUri, HttpMethod.valueOf(prepareAPI.getApiOperationType()), httpEntity);

			if (null != responseEntity) {

				json = responseEntity.getBody();

				// Below code is for checking API hourly limit
				checkAPIHourlyLimit(responseEntity);

			}
		} catch (HttpClientErrorException exp) {

			return processDSApiCallErrorException(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, fullUri, exp, processId, httpEntity);
		} catch (Exception exp) {

			log.error(
					"Exception -> {} occurred with cause -> {} and message -> {} for accountId -> {} and fullUri -> {}",
					exp, exp, exp.getMessage(), accountId, fullUri);

			if (!StringUtils.isEmpty(
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

				queueService.createFailureMessageAndSend(accountId + "_" + fullUri, batchId, processId, exp,
						FailureCode.ERROR_107, FailureStep.PREPARE_API_URI);

			} else {

				batchDataService.createFailureRecord(accountId + "_" + fullUri, batchId,
						FailureCode.ERROR_107.toString(), exp.getMessage(), FailureStep.PREPARE_API_URI.toString(), exp,
						processId);

			}

			exp.printStackTrace();
			throw exp;

		}

		if (!(returnType == byte[].class)) {

			log.debug("json {} successfully created for fullUri -> {}", json, fullUri);
		}

		return json;
	}

	private String createFullUriToCallDSApi(PrepareDataAPI prepareAPI, String nextUri,
			AuthenticationRequest authenticationRequest) {

		if (ReportAppUtil.isValidURL(nextUri)) {

			return nextUri;
		} else {

			if (!ReportAppUtil.isValidURL(prepareAPI.getApiUri())) {

				if (StringUtils.isEmpty(nextUri)) {

					return dsAuthorizationCache.requestBaseUrl(authenticationRequest) + prepareAPI.getApiUri();
				} else {

					return dsAuthorizationCache.requestBaseUrl(authenticationRequest) + nextUri;
				}

			} else {

				return prepareAPI.getApiUri();
			}

		}

	}

	@SuppressWarnings("unchecked")
	private <T> ResponseEntity<T> prepareDataForDSApiCall(PrepareDataAPI prepareAPI, String accountId,
			Map<String, Object> inputParams, Object pathValue, String nextUri, Class<T> returnType, String fullUri,
			HttpMethod httpMethod, HttpEntity<String> httpEntity) {

		List<Object> paramList = null;
		ResponseEntity<T> responseEntity = null;
		List<PathParam> apiParamList = prepareAPI.getApiParams();

		if (null != apiParamList && !apiParamList.isEmpty() && StringUtils.isEmpty(nextUri)) {

			paramList = new ArrayList<Object>(apiParamList.size());

			for (PathParam apiParam : apiParamList) {

				Object paramValue = null;
				if (null != inputParams && !inputParams.isEmpty()
						&& apiParam.getParamName().toLowerCase().contains("input")) {

					paramValue = (String) inputParams.get(apiParam.getParamName());

				} else if (null != inputParams && !inputParams.isEmpty()
						&& null != inputParams.get(apiParam.getParamName())) {

					paramValue = (String) inputParams.get(apiParam.getParamName());
				} else if (!StringUtils.isEmpty(apiParam.getParamValue())) {

					paramValue = apiParam.getParamValue();
				} else if (null != pathValue && pathValue instanceof List) {

					paramValue = (String) ((Map<String, Object>) ((List<?>) pathValue).get(0))
							.get(apiParam.getParamName());
				} else {

					paramValue = pathValue;
				}

				paramList.add(paramValue);
			}

			Object[] paramArr = new String[paramList.size()];
			paramList.toArray(paramArr);

			log.info("paramList is {}, httpEntity is {} and fullUri is {}", paramList, httpEntity, fullUri);

			responseEntity = restTemplate.exchange(fullUri, httpMethod, httpEntity, returnType, paramArr);
		} else {

			responseEntity = restTemplate.exchange(fullUri, httpMethod, httpEntity, returnType, accountId);
		}
		return responseEntity;
	}

	private <T> T processDSApiCallErrorException(PrepareDataAPI prepareAPI, String accountId,
			Map<String, Object> inputParams, Object pathValue, String nextUri, String batchId, Class<T> returnType,
			String fullUri, HttpClientErrorException exp, String processId, HttpEntity<String> httpEntity) {

		log.error(
				"HttpClientErrorException -> {} occurrred with Status Code returned is {}, httpEntity -> {}, rawStatusCode returned is {}, responseHeaders() is {}, responseBody is {} for accountId -> {} and fullUri -> {}",
				exp, exp.getStatusCode(), exp.getRawStatusCode(), httpEntity, exp.getResponseHeaders(),
				exp.getResponseBodyAsString(), accountId, fullUri);

		DSErrorMessage dsErrorMessage = null;
		try {

			dsErrorMessage = objectMapper.readValue(exp.getResponseBodyAsString(), DSErrorMessage.class);
			if (exp.getStatusCode() == HttpStatus.BAD_REQUEST && null != dsErrorMessage
					&& "USER_DOES_NOT_EXIST_IN_SYSTEM".equalsIgnoreCase(dsErrorMessage.getErrorCode())) {

				log.error("USER_DOES_NOT_EXIST_IN_SYSTEM thrown for accountId -> {} and fullUri -> {}", accountId,
						fullUri);

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId + "_" + fullUri, batchId, processId, exp,
							FailureCode.ERROR_108, FailureStep.PROCESSDSAPICALLERROREXCEPTION);

				} else {

					batchDataService.createFailureRecord(accountId + "_" + fullUri, batchId,
							FailureCode.ERROR_108.toString(), exp.getResponseBodyAsString(),
							FailureStep.PROCESSDSAPICALLERROREXCEPTION.toString(), exp, processId);
				}

				throw exp;
			} else {

				if (!StringUtils.isEmpty(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE))) {

					queueService.createFailureMessageAndSend(accountId + "_" + fullUri, batchId, processId, exp,
							FailureCode.ERROR_106, FailureStep.PROCESSDSAPICALLERROREXCEPTION);

				} else {

					batchDataService.createFailureRecord(accountId + "_" + fullUri, batchId,
							FailureCode.ERROR_106.toString(), exp.getResponseBodyAsString(),
							FailureStep.PROCESSDSAPICALLERROREXCEPTION.toString(), exp, processId);
				}

			}

			return processAPIHourlyLimitCheck(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, fullUri, exp, processId);
		} catch (JsonMappingException e) {

			log.error(
					"JsonMappingException for converting DS Error message to DSErrorMessage for accountId -> {} and fullUri -> {}",
					accountId, fullUri);
			e.printStackTrace();
		} catch (JsonProcessingException e) {

			log.error(
					"JsonProcessingException for converting DS Error message to DSErrorMessage for accountId -> {} and fullUri -> {}",
					accountId, fullUri);
			e.printStackTrace();
		}

		return null;
	}

	private <T> ResponseEntity<T> processAPIHourlyLimitCheckWithHeader(PrepareDataAPI prepareAPI, String accountId,
			Map<String, Object> inputParams, Object pathValue, String nextUri, String batchId, Class<T> returnType,
			String fullUri, HttpClientErrorException exp, String processId) {

		ApiHourlyLimitData apiHourlyLimitData = createAPIHourlyLimit(exp);

		if (null != apiHourlyLimitData && apiHourlyLimitData.isSleepThread()) {

			log.info(
					" <<<<<<<<<<<<<<<<<<<< Thread already slept by apiHourlyLimitData so retrying message again in fullUri {}, prepareAPI.getApiUri -> {} for accountId -> {}, processId -> {} and batchId -> {} >>>>>>>>>>>>>>>>>>>> ",
					fullUri, prepareAPI.getApiUri(), accountId, processId, batchId);

			return callPrepareAPIWithRespHeader(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, processId);

		} else if (exp.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || exp.getRawStatusCode() == 429) {

			validateResponseForTooManyRequests(fullUri, accountId, processId, batchId);

			log.info(
					" <<<<<<<<<<<<<<<<<<<< Retrying message after receiving 429 for fullUri {}, prepareAPI.getApiUri -> {} for accountId -> {}, processId -> {} and batchId -> {} >>>>>>>>>>>>>>>>>>>> ",
					fullUri, prepareAPI.getApiUri(), accountId, processId, batchId);
			return callPrepareAPIWithRespHeader(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId,
					returnType, processId);
		} else {

			throw exp;
		}

	}

	private <T> T processAPIHourlyLimitCheck(PrepareDataAPI prepareAPI, String accountId,
			Map<String, Object> inputParams, Object pathValue, String nextUri, String batchId, Class<T> returnType,
			String fullUri, HttpClientErrorException exp, String processId) {

		ApiHourlyLimitData apiHourlyLimitData = createAPIHourlyLimit(exp);

		if (null != apiHourlyLimitData && apiHourlyLimitData.isSleepThread()) {

			log.info(
					" <<<<<<<<<<<<<<<<<<<< Thread already slept by apiHourlyLimitData so retrying message again in fullUri {}, prepareAPI.getApiUri -> {} for accountId -> {}, processId -> {} and batchId -> {} >>>>>>>>>>>>>>>>>>>> ",
					fullUri, prepareAPI.getApiUri(), accountId, processId, batchId);
			return callPrepareAPI(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId, returnType,
					processId);

		} else if (exp.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || exp.getRawStatusCode() == 429) {

			validateResponseForTooManyRequests(fullUri, accountId, processId, batchId);

			log.info(
					" <<<<<<<<<<<<<<<<<<<< Retrying message after receiving 429 for fullUri {}, prepareAPI.getApiUri -> {} for accountId -> {}, processId -> {} and batchId -> {} >>>>>>>>>>>>>>>>>>>> ",
					fullUri, prepareAPI.getApiUri(), accountId, processId, batchId);
			return callPrepareAPI(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId, returnType,
					processId);
		} else if ((!StringUtils.isEmpty(exp.getMessage()) && exp.getMessage().toLowerCase().contains("lock"))
				|| (!StringUtils.isEmpty(exp.getResponseBodyAsString())
						&& exp.getResponseBodyAsString().toLowerCase().contains("lock"))
				|| (!StringUtils.isEmpty(exp.getLocalizedMessage())
						&& exp.getLocalizedMessage().toLowerCase().contains("lock"))) {

			validateResponseForTooManyRequests(fullUri, accountId, processId, batchId);
			log.info(
					" <<<<<<<<<<<<<<<<<<<< Retrying message after receiving Lock error for fullUri {}, prepareAPI.getApiUri -> {} for accountId -> {}, processId -> {} and batchId -> {} >>>>>>>>>>>>>>>>>>>> ",
					fullUri, prepareAPI.getApiUri(), accountId, processId, batchId);
			return callPrepareAPI(prepareAPI, accountId, inputParams, pathValue, nextUri, batchId, returnType,
					processId);
		} else {

			throw exp;
		}

	}

	private ApiHourlyLimitData createAPIHourlyLimit(HttpClientErrorException exp) {

		ApiHourlyLimitData apiHourlyLimitData = null;
		String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);
		if (StringUtils.isEmpty(apiThresholdLimitPercentAsStr)) {

			apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(), 90);
		} else {

			apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
					Integer.valueOf(apiThresholdLimitPercentAsStr));
		}

		if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

			log.info("For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
					apiHourlyLimitData.getDocuSignTraceToken());
		}
		return apiHourlyLimitData;
	}

	private void validateResponseForTooManyRequests(String fullUri, String accountId, String processId,
			String batchId) {

		log.warn(
				" ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Too many request sent for fullUri -> {}, accountId -> {}, processId -> {} and batchId -> {} so putting thread to sleep ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ",
				fullUri, accountId, processId, batchId);

		LocalDateTime currentDatetime = LocalDateTime.now();
		LocalDateTime newDatetime = currentDatetime.plusMinutes(60 - currentDatetime.getMinute() + 1);

		long sleepMillis = Math.abs(Duration.between(currentDatetime, newDatetime).toMillis());

		ApiLimitUtil.sleepThread(newDatetime, sleepMillis);
	}

}