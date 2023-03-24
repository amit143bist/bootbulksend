package com.ds.proserv.report.auth.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

@Service
public class CLMOAuthService extends AbstractDSAuthService implements IAuthService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.CLMAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		if (!StringUtils.isEmpty(authenticationRequest.getScopes())) {

			return super.requestOAuthToken(authenticationRequest);
		} else {

			String fullUri = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.REPORT_CLM_AUTH_API_BASEURL, PropertyCacheConstants.DS_API_REFERENCE_NAME)
					+ AppConstants.FORWARD_SLASH
					+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_CLM_AUTH_API_VERSION,
							PropertyCacheConstants.DS_API_REFERENCE_NAME)
					+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_CLM_API_USER_ENDPOINT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);

			String msgBody = "{\"client_id\":" + "\"" + authenticationRequest.getClientId() + "\""
					+ ",\"client_secret\":" + "\"" + authenticationRequest.getClientSecret() + "\"" + "}";

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");

			HttpEntity<String> httpEntity = new HttpEntity<>(msgBody, headers);
			return callAPI(AuthenticationResponse.class, fullUri, HttpMethod.POST, httpEntity);

		}
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.REPORT_CLM_API_BASEURL,
				PropertyCacheConstants.DS_API_REFERENCE_NAME) + AppConstants.FORWARD_SLASH
				+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.REPORT_CLM_API_VERSION, PropertyCacheConstants.DS_API_REFERENCE_NAME);
	}

	private <T> T callAPI(Class<T> returnType, String fullUri, HttpMethod httpMethod, HttpEntity<String> httpEntity) {

		ResponseEntity<T> responseEntity = restTemplate.exchange(fullUri, httpMethod, httpEntity, returnType);
		return responseEntity.getBody();
	}

}