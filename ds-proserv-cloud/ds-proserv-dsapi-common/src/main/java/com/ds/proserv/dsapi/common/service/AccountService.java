package com.ds.proserv.dsapi.common.service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.dsapi.common.client.AuthenticationClient;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.account.domain.LoginUserInfoDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AccountService {

	@Autowired
	private AuthenticationClient authenticationClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSCacheManager dsCacheManager;

	public AccountDefinition getAccount(String accountGuid, AuthenticationRequest authenticationRequest) {

		log.info("Get BaseUri for accountGuid -> {} with authenticationRequest", accountGuid);

		return extractFilteredAccount(accountGuid, getTokenForUser(authenticationRequest));
	}

	public AccountDefinition getAccount(String accountGuid) {

		log.info("Get BaseUri for accountGuid -> {}", accountGuid);

		String mockEnabled =
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ENABLE_TEST_MOCK,
						PropertyCacheConstants.DS_API_REFERENCE_NAME);
		AccountDefinition accountDefinition = null;
		if (!StringUtils.isEmpty(mockEnabled)) {
			log.info("Running is test mode => mockEnabled -> {}", mockEnabled);
			String eSignBaseUrl =
					dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_MOCK_BASEURL,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);
			log.info("Using test mock DocuSign service -> {}", eSignBaseUrl);
			if (!StringUtils.isEmpty(eSignBaseUrl)) {
				accountDefinition = new AccountDefinition();
				accountDefinition.setBaseUri(eSignBaseUrl);
			}
		} else {

			AuthenticationResponse authenticationResponse = getTokenForSystemUser();
			accountDefinition = extractFilteredAccount(accountGuid, authenticationResponse);
		}

		return accountDefinition;
	}

	private AccountDefinition extractFilteredAccount(String accountGuid,
			AuthenticationResponse authenticationResponse) {

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.AUTHORIZATION,
				authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());
		httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<String> entity = new HttpEntity<String>(httpHeaders);

		ResponseEntity<LoginUserInfoDefinition> loginUserInfoResponse = restTemplate.exchange(
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.DSAPI_USER_INFO_ENDPOINT, PropertyCacheConstants.DS_API_REFERENCE_NAME),
				HttpMethod.GET, entity, LoginUserInfoDefinition.class);

		AccountDefinition filteredAccount = loginUserInfoResponse.getBody().getAccounts().stream()
				.filter(account -> accountGuid.equalsIgnoreCase(account.getAccountId())).findAny().orElse(null);

		return filteredAccount;
	}

	public AuthenticationResponse getTokenForSystemUser() {
		AuthenticationRequest authenticationRequest = new AuthenticationRequest();

		authenticationRequest.setUser(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_USERID, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));
		authenticationRequest.setScopes(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_SCOPES, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));
		return getTokenForUser(authenticationRequest);
	}

	public AuthenticationResponse getTokenForUser(AuthenticationRequest authenticationRequest) {

		log.info("obtaining token for user {}, with the following scopes {}", authenticationRequest.getUser(),
				authenticationRequest.getScopes());

		return authenticationClient.requestJWTUserToken(authenticationRequest).getBody();
	}

}