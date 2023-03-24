package com.ds.proserv.authentication.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.authentication.service.DSOAuthService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.authentication.service.AuthenticationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
public class AuthenticationController implements AuthenticationService {

	@Autowired
	private DSOAuthService dsOauthService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private DSCacheManager dsCacheManager;

	/**
	 * Generates a JWT assertion and sign it then use that assertion to obtain a
	 * oauth token.
	 *
	 * @param authenticationRequest This is the object with the user and teh scopes
	 *                              for the assertion
	 * @return a {@link AuthenticationResponse} containing the oAuth2 token
	 */
	@Override
	@Cacheable(value = "token", key = "#authenticationRequest.user")
	public ResponseEntity<AuthenticationResponse> requestJWTUserToken(AuthenticationRequest authenticationRequest) {

		Assert.notNull(authenticationRequest.getUser(), "authenticationRequest.user was empty");
		Assert.notNull(authenticationRequest.getScopes(), "authenticationRequest.scopes was empty");

		log.debug("AuthenticationController.requestJWTUserToken() user -> {} scopes -> {}",
				authenticationRequest.getUser(), authenticationRequest.getScopes());

		String integratorKey = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_INTEGRATORKEY, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

		String expirationSeconds = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_EXPIRY_SECONDS, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

		String rsaPrivateKeyPath = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_RSA_PRIVATEKEY_PATH,
				PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

		String rsaPublicKeyPath = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_RSA_PUBLICKEY_PATH,
				PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

		String dsEnvironment = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_AUD, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

		log.info("Calling dsOauthService.requestJWTUserToken rsaPublicKeyPath -> {}, rsaPrivateKeyPath -> {},"
				+ "dsEnvironment -> {}, integratorKey -> {}, user -> {}, expirationSeconds -> {}, scopyes -> {}",
				rsaPublicKeyPath, rsaPrivateKeyPath, dsEnvironment, integratorKey, authenticationRequest.getUser(),
				Long.valueOf(expirationSeconds), authenticationRequest.getScopes());

		return dsOauthService.requestOAuthToken(rsaPublicKeyPath, rsaPrivateKeyPath, dsEnvironment, integratorKey,
				authenticationRequest.getUser(), Long.valueOf(expirationSeconds), authenticationRequest.getScopes());

	}

	@Override
	public String clearCache() {

		log.info("clearCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();

		return ValidationResult.SUCCESS.toString();
	}

	/**
	 * Clear all tokens from token cache, every
	 */
	@Scheduled(fixedRateString = "#{@getScheduleFixedRate}")
	public void evictAuthenticationCache() {
		log.info("evictAuthenticationCache scheduled called, now clearing the tokens for all users");
		cacheManager.getCache("token").clear();
	}
}