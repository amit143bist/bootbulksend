package com.ds.proserv.report.auth.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ConsentRequiredException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.authentication.service.AuthenticationService;
import com.ds.proserv.report.util.JWTUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDSAuthService implements IAuthService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	protected DSCacheManager dsCacheManager;

	@Autowired
	private AuthenticationService authenticationService;

	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {
		// pass the path to the file as a parameter

		if (null != authenticationService) {

			validateAuthRequest(authenticationRequest);

			return authenticationService.requestJWTUserToken(authenticationRequest).getBody();
		} else {

			AtomicReference<String> jwtUserId = new AtomicReference<String>();
			String jwtScopes = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DSAUTH_SCOPES, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);
			try {

				log.info("Generating Access Token in requestOauthToken method for accountId {}",
						authenticationRequest.getAccountGuid());

				if (!StringUtils.isEmpty(authenticationRequest.getUser())) {

					jwtUserId.set(authenticationRequest.getUser());
				} else {
					jwtUserId.set(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.DSAUTH_USERID, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));
				}

				if (!StringUtils.isEmpty(authenticationRequest.getScopes())) {

					jwtScopes = authenticationRequest.getScopes();
				}

				String assertion = JWTUtil.generateJWTAssertion(
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_RSA_PUBLICKEY_PATH,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_RSA_PRIVATEKEY_PATH,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAUTH_AUD,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),

						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
						jwtUserId.get(),
						Long.valueOf(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_EXPIRY_SECONDS,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)),
						jwtScopes);

				log.info("JWT assertion {} for user {} with IntegratorKey {} and accountId {}", assertion,
						jwtUserId.get(),
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
						authenticationRequest.getAccountGuid());

				Assert.notNull(assertion, "assertion was empty");

				MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
				form.add("assertion", assertion);
				form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				headers.setCacheControl(CacheControl.noStore());
				headers.setPragma("no-cache");

				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
				return Optional.ofNullable(

						restTemplate
								.exchange(
										"https://"
												+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
														PropertyCacheConstants.DSAUTH_AUD,
														PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
												+ "/oauth/token",
										HttpMethod.POST, request, AuthenticationResponse.class))
						.map(authenticationToken -> {

							Assert.notNull(authenticationToken.getBody(),
									"authenticationToken is null for user " + jwtUserId.get());
							Assert.isTrue(authenticationToken.getStatusCode().is2xxSuccessful(),
									"AuthenticationToken is not returned with 200 status code");

							log.debug("Returning Access Token -> {} for user {} with IntegratorKey {} and accountId {}",
									authenticationToken, jwtUserId.get(),
									dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
											PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
											PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
									authenticationRequest.getAccountGuid());

							return authenticationToken.getBody();
						}).orElseThrow(() -> new ResourceNotFoundException(
								"Token was not retrieved from authenticationToken"));
			} catch (HttpClientErrorException e) {

				log.error(
						"HttpClientErrorException {} happened in generating Access Token for user {} with IntegratorKey {} and accountId {}",
						e.getMessage(), jwtUserId.get(),
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME),
						authenticationRequest.getAccountGuid());

				log.error("HttpClientErrorException statusCode is {} and ResponseBody is  {}", e.getStatusCode(),
						e.getResponseBodyAsString());

				if (e.getStatusCode() == HttpStatus.BAD_REQUEST
						&& e.getResponseBodyAsString().contains("consent_required")) {

					String consentUrl = "https://"
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.DSAUTH_AUD, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
							+ "/oauth/auth?response_type=code&scope="
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.DSAUTH_SCOPES,
									PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
							+ "&client_id="
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
									PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME)
							+ "&redirect_uri=https://www.docusign.com";
					throw new ConsentRequiredException(
							"Unable to Obtain token for user: " + jwtUserId.get() + ". " + "Error description: "
									+ e.getResponseBodyAsString() + " Obtain token by launching " + consentUrl);
				}

				throw e;
			} catch (IOException e) {

				log.error("IOException {} happened in generating Access Token for user {} with IntegratorKey {}",
						e.getMessage(), jwtUserId.get(),
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.DSAUTH_INTEGRATORKEY,
								PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));

				throw new ConsentRequiredException("Unable to read key " + e.getMessage());
			}
		}

	}

	private void validateAuthRequest(AuthenticationRequest authenticationRequest) {

		if (StringUtils.isEmpty(authenticationRequest.getUser())) {

			authenticationRequest.setUser(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DSAUTH_USERID, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));
		}
		if (StringUtils.isEmpty(authenticationRequest.getScopes())) {

			authenticationRequest.setScopes(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DSAUTH_SCOPES, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));
		}
	}
}