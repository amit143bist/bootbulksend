package com.ds.proserv.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.authentication.controller.AuthenticationController;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false")
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@Slf4j
public class DSOAuthServiceTest {

	@Autowired
	CacheManager manager;

	@Autowired
	private AuthenticationController authenticationController;

	@MockBean
	private HttpHeaders httpHeaders;

	@MockBean
	private RestTemplate restTemplate;

	@MockBean
	private DSOAuthService dsOauthService;

	@Value("${app.authorization.aud}")
	private String dsEnvironment;

	@Value("${app.authorization.integratorKey}")
	private String integratorKey;

	@Value("${app.authorization.token.expirationSeconds}")
	private String expirationSeconds;

	@Value("${app.authorization.rsaPrivateKeyPath}")
	private String rsaPrivateKeyPath;

	@Value("${app.authorization.rsaPublicKeyPath}")
	private String rsaPublicKeyPath;

	@Test
	public void testTokenIsCached() {

		AuthenticationResponse authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setAccessToken("token1");
		ResponseEntity<AuthenticationResponse> obj1 = new ResponseEntity<>(authenticationResponse, HttpStatus.OK);

		authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setAccessToken("token2");
		ResponseEntity<AuthenticationResponse> obj2 = new ResponseEntity<>(authenticationResponse, HttpStatus.OK);

		Mockito.doReturn(obj1).when(dsOauthService).requestOAuthToken(rsaPublicKeyPath, rsaPrivateKeyPath,
				dsEnvironment, integratorKey, "user1", Long.valueOf(expirationSeconds), "scopes");
		Mockito.doReturn(obj2).when(dsOauthService).requestOAuthToken(rsaPublicKeyPath, rsaPrivateKeyPath,
				dsEnvironment, integratorKey, "user2", Long.valueOf(expirationSeconds), "scopes");
		AuthenticationRequest authenticationRequestUser1 = new AuthenticationRequest();
		authenticationRequestUser1.setUser("user1");
		authenticationRequestUser1.setScopes("scopes");

		AuthenticationRequest authenticationRequestUser2 = new AuthenticationRequest();
		authenticationRequestUser2.setUser("user2");
		authenticationRequestUser2.setScopes("scopes");

		// First invocation returns object returned by the method
		ResponseEntity<AuthenticationResponse> result = authenticationController
				.requestJWTUserToken(authenticationRequestUser1);

		log.info("resultBody -> {}, obj1Body -> {}", result.getBody(), obj1.getBody());
		assertThat(result.getBody().getAccessToken()).isEqualTo(obj1.getBody().getAccessToken());

		// Second invocation should return cached value, *not* second (as set up above)
		result = authenticationController.requestJWTUserToken(authenticationRequestUser1);
		assertThat(result.getBody().getAccessToken()).isEqualTo(obj1.getBody().getAccessToken());

		// Verify repository method was invoked once
		Mockito.verify(dsOauthService, Mockito.times(1)).requestOAuthToken(rsaPublicKeyPath, rsaPrivateKeyPath,
				dsEnvironment, integratorKey, "user1", Long.valueOf(expirationSeconds), "scopes");
		assertThat(manager.getCache("token").get("user1")).isNotNull();

		// Third invocation with different key is triggers the second invocation of the
		// repo method
		result = authenticationController.requestJWTUserToken(authenticationRequestUser2);
		assertThat(result.getBody().getAccessToken()).isEqualTo(obj2.getBody().getAccessToken());

	}

	@Test
	public void testInvalidateCache() throws InterruptedException {

		AuthenticationResponse authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setAccessToken("token1");
		ResponseEntity<AuthenticationResponse> obj1 = new ResponseEntity<>(authenticationResponse, HttpStatus.OK);

		Mockito.doReturn(obj1).when(dsOauthService).requestOAuthToken(rsaPublicKeyPath, rsaPrivateKeyPath,
				dsEnvironment, integratorKey, "user1", Long.valueOf(expirationSeconds), "scopes");

		AuthenticationRequest authenticationRequestUser1 = new AuthenticationRequest();
		authenticationRequestUser1.setUser("user1");
		authenticationRequestUser1.setScopes("scopes");

		// First invocation returns object returned by the method
		authenticationController.requestJWTUserToken(authenticationRequestUser1);
		assertThat(manager.getCache("token").get("user1")).isNotNull();

		Thread.sleep(Long.valueOf(6000));
		assertThat(manager.getCache("token").get("user1")).isNull();
	}
}