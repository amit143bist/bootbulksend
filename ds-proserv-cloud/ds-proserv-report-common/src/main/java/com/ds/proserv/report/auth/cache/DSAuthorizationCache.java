package com.ds.proserv.report.auth.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.auth.factory.AuthenticationFactory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSAuthorizationCache {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private AuthenticationFactory authenticationFactory;

	@Cacheable(value = "token", key = "#authenticationRequest.accountGuid + '_' + #authenticationRequest.apiCategory + '_' + #authenticationRequest.apiId", sync = true)
	public AuthenticationResponse requestToken(AuthenticationRequest authenticationRequest) {

		log.info("Fetching Token for accountId -> {} and apiId -> {}", authenticationRequest.getAccountGuid(),
				authenticationRequest.getApiId());
		return authenticationFactory.fetchAuthServiceByAPICategoryType(
				ReportAppUtil.getAPICategoryType(authenticationRequest.getApiCategory())).map(authService -> {

					return authService.requestOAuthToken(authenticationRequest);
				}).orElse(null);
	}

	@Cacheable(value = "baseUrl", key = "#authenticationRequest.accountGuid + '_' + #authenticationRequest.apiCategory + '_' + #authenticationRequest.apiId", sync = true)
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		log.info("Fetching baseUrl for accountId -> {} and apiId -> {}", authenticationRequest.getAccountGuid(),
				authenticationRequest.getApiId());

		return authenticationFactory.fetchAuthServiceByAPICategoryType(
				ReportAppUtil.getAPICategoryType(authenticationRequest.getApiCategory())).map(authService -> {

					return authService.requestBaseUrl(authenticationRequest);
				}).orElse(null);
	}

	public String clearCache() {

		log.info("ClearCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();

		return ValidationResult.SUCCESS.toString();
	}

	/**
	 * Clear all tokens from token cache, every
	 */
	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	public void evictAuthenticationCache() {

		log.info("EvictAuthenticationCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();
	}
}