package com.ds.proserv.report.dsapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.dsapi.domain.AccountUser;
import com.ds.proserv.feign.dsapi.domain.AccountUserInformation;
import com.ds.proserv.feign.dsapi.domain.UserAccount;
import com.ds.proserv.feign.dsapi.domain.UserInformation;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.ReportRunArgs;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.auth.cache.DSAuthorizationCache;
import com.ds.proserv.report.auth.domain.JWTParams;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSAccountService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSAuthorizationCache dsAuthorizationCache;

	public List<String> getAllAccountIds(AuthenticationRequest authenticationRequest) {

		UserInformation userInformation = getUserInfo(authenticationRequest);

		List<String> accountIds = new ArrayList<String>();
		if (null != userInformation) {

			List<UserAccount> userAccounts = userInformation.getUserAccounts();

			userAccounts.forEach(account -> {

				accountIds.add(account.getAccountId());
			});
		}

		return accountIds;
	}

	public UserInformation getUserInfo(AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving userinfo account information.");

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(
				dsAuthorizationCache.requestToken(authenticationRequest), MediaType.APPLICATION_JSON_VALUE,
				MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional
					.ofNullable(restTemplate.exchange("https://"
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.DSAUTH_AUD, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
							+ "/oauth/userinfo", HttpMethod.GET, httpEntity, UserInformation.class))
					.map(userInfo -> {

						Assert.isTrue(userInfo.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(userInfo.getBody(), "UserInfo is null");
						return userInfo.getBody();

					})
					.orElseThrow(() -> new ResourceNotFoundException(
							"UserInfo not retured for accountAdmin " + null != authenticationRequest.getUser()
									? authenticationRequest.getUser()
									: dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
											PropertyCacheConstants.DSAUTH_USERID,
											PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)));
		} catch (HttpClientErrorException e) {

			log.info("Calling DSAccountService.getUserInfo: Receive HttpClientErrorException {}, responseBody -> {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to retrieve userinfo", e);

		}
	}

	public String getUserInfo(String accountGuid, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving userinfo account information for accountId -> {}", accountGuid);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);
		Assert.notNull(authenticationResponse, "AuthenticationResponse is empty");

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional
					.ofNullable(restTemplate.exchange("https://"
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.DSAUTH_AUD, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
							+ "/oauth/userinfo", HttpMethod.GET, httpEntity, UserInformation.class))
					.map(userInfo -> {

						Assert.isTrue(userInfo.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(userInfo.getBody(), "UserInfo is null");

						UserInformation userInformation = userInfo.getBody();
						if (null != userInformation && null != userInformation.getUserAccounts()
								&& !userInformation.getUserAccounts().isEmpty()) {

							List<UserAccount> userAccountList = userInformation.getUserAccounts();
							for (UserAccount userAccount : userAccountList) {

								if (accountGuid.equalsIgnoreCase(userAccount.getAccountId())) {

									return userAccount.getBaseUri();
								}
							}
						}

						log.error("Could not find matching AccountId in userInfo call with accountGuid -> {}",
								accountGuid);
						return null;

					})
					.orElseThrow(() -> new ResourceNotFoundException(
							"UserInfo not retured for accountAdmin " + null != authenticationRequest.getUser()
									? authenticationRequest.getUser()
									: dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
											PropertyCacheConstants.DSAUTH_USERID,
											PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)));
		} catch (HttpClientErrorException e) {

			log.info("Calling DSAccountService.getUserInfo: Receive HttpClientErrorException {}, responseBody -> {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to retrieve userinfo", e);

		}
	}

	public List<AccountUser> fetchAllAccountUsers(String accountGuid, AuthenticationRequest authenticationRequest) {

		log.debug("Retrieving all account users from accountId -> {}", accountGuid);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		String baseUrl = dsAuthorizationCache.requestBaseUrl(authenticationRequest);

		Integer apiThresholdLimitPercent = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME));
		List<AccountUser> accountUserList = new ArrayList<AccountUser>();
		try {
			Optional.ofNullable(restTemplate.exchange(
					baseUrl + "/accounts/" + accountGuid + "/"
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.REPORT_ESIGN_API_USERS_ENDPOINT,
									PropertyCacheConstants.DS_API_REFERENCE_NAME),
					HttpMethod.GET, httpEntity, AccountUserInformation.class)).map(accountUserResponse -> {

						Assert.isTrue(accountUserResponse.getStatusCode().is2xxSuccessful(),
								"Account userInfo data is not returned with 2xx status code");
						Assert.notNull(accountUserResponse.getBody(), "accountUserResponse is null");

						accountUserList.addAll(accountUserResponse.getBody().getUsers());

						ApiLimitUtil.readApiHourlyLimitData(accountUserResponse.getHeaders(), apiThresholdLimitPercent);
						processPaginationData(accountGuid, baseUrl, httpEntity, accountUserList, accountUserResponse);

						return accountUserList;

					}).orElseThrow(() -> new ResourceNotFoundException(
							"accountUserResponse not retured for accountId " + accountGuid));
		} catch (HttpClientErrorException exp) {

			ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
					apiThresholdLimitPercent);

			if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

				log.info("For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
						apiHourlyLimitData.getDocuSignTraceToken());
			}

			log.info(
					"Calling DSAccountService.fetchAllAccountUsers: Receive HttpClientErrorException {}, responseBody -> {}",
					exp.getStatusCode(), exp.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to call DSAccountService.fetchAllAccountUsers", exp);

		}

		return accountUserList;
	}

	private void processPaginationData(String accountGuid, String baseUrl, HttpEntity<String> httpEntity,
			List<AccountUser> accountUserList, ResponseEntity<AccountUserInformation> accountUserResponse) {

		String nextUri = accountUserResponse.getBody().getNextUri();
		Integer apiThresholdLimitPercent = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME));

		while (!StringUtils.isEmpty(nextUri)) {

			ResponseEntity<AccountUserInformation> accountUserInformationResponse = restTemplate.exchange(
					baseUrl + "/accounts/" + accountGuid + "/" + nextUri, HttpMethod.GET, httpEntity,
					AccountUserInformation.class);

			nextUri = accountUserInformationResponse.getBody().getNextUri();
			accountUserList.addAll(accountUserInformationResponse.getBody().getUsers());

			ApiLimitUtil.readApiHourlyLimitData(accountUserResponse.getHeaders(), apiThresholdLimitPercent);
		}
	}

	public List<String> getAllUserIds(ReportRunArgs apiRunArgs, String accountId, String apiCategory, Integer apiId) {

		log.info("Fetching getAllUserIds to be processed by this batch");

		List<String> toProcessUserIdList = null;

		PathParam filterUserIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.FILTER_USER_IDS);

		JWTParams jwtParams = new JWTParams(apiRunArgs);

		AuthenticationRequest authenticationRequest = new AuthenticationRequest(accountId, jwtParams.getJwtScopes(),
				jwtParams.getJwtUserId(), apiCategory, jwtParams.getAuthClientId(), jwtParams.getAuthClientSecret(),
				jwtParams.getAuthUserName(), jwtParams.getAuthPassword(), jwtParams.getAuthBaseUrl(), apiId);

		if (null != filterUserIdsParam && !StringUtils.isEmpty(filterUserIdsParam.getParamValue())) {

			String filterUserIds = filterUserIdsParam.getParamValue();

			List<String> filterUserIdList = Stream.of(filterUserIds.split(",")).map(String::trim)
					.collect(Collectors.toList());

			toProcessUserIdList = fetchAllAccountUsers(accountId, authenticationRequest).stream()
					.map(AccountUser::getUserId).filter(userId -> !filterUserIdList.contains(userId))
					.collect(Collectors.toList());

			return toProcessUserIdList;

		} else {

			PathParam selectUserIdsParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
					AppConstants.SELECT_USER_IDS);

			if (null != selectUserIdsParam && !StringUtils.isEmpty(selectUserIdsParam.getParamValue())) {

				toProcessUserIdList = Stream.of(selectUserIdsParam.getParamValue().split(","))
						.collect(Collectors.toList());

				return toProcessUserIdList;
			}

			toProcessUserIdList = fetchAllAccountUsers(accountId, authenticationRequest).stream()
					.map(AccountUser::getUserId).collect(Collectors.toList());

			return toProcessUserIdList;
		}

	}
}