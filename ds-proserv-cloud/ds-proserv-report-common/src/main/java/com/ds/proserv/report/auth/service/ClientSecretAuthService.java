package com.ds.proserv.report.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

public class ClientSecretAuthService implements IAuthService {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.CLIENTSECRETAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		String fullUri = "";

		String msgBody = "{\"client_id\":" + "\"" + authenticationRequest.getClientId() + "\"" + ",\"client_secret\":"
				+ "\"" + authenticationRequest.getClientSecret() + "\"" + "}";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");

		HttpEntity<String> httpEntity = new HttpEntity<>(msgBody, headers);
		return callAPI(AuthenticationResponse.class, fullUri, HttpMethod.POST, httpEntity);
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return authenticationRequest.getBaseUrl();
	}

	private <T> T callAPI(Class<T> returnType, String fullUri, HttpMethod httpMethod, HttpEntity<String> httpEntity) {

		ResponseEntity<T> responseEntity = restTemplate.exchange(fullUri, httpMethod, httpEntity, returnType);
		return responseEntity.getBody();
	}
}