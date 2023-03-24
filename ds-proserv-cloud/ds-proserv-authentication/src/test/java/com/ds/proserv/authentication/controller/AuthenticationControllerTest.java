package com.ds.proserv.authentication.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
public class AuthenticationControllerTest extends AbstractTests {

	@Test
	public void requestJWTUserTokenUserWithoutConsent() throws Exception {

		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUser("xxx1fa0e-xxxx-xxxx-xxxx-xxxc5860cxxx");
		authenticationRequest.setScopes("signature impersonation");
		mockMvc.perform(MockMvcRequestBuilders.post("/docusign/authentication/token")
				.with(httpBasic("docusignuser", "testing1")).content(asJsonString(authenticationRequest))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.message").isNotEmpty()).andExpect(jsonPath("$.message").isString());

	}

	@Test
	public void requestJWTUserTokenUserWithConsent() throws Exception {

		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUser("xxxc8f7b-xxxx-xxxx-xxxx-xxx5fb3abxxx");
		authenticationRequest.setScopes("signature impersonation");

		mockMvc.perform(MockMvcRequestBuilders.post("/docusign/authentication/token")
				.with(httpBasic("docusignuser", "testing1")).content(asJsonString(authenticationRequest))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$").isNotEmpty())
				.andExpect(jsonPath("$.access_token").isNotEmpty()).andExpect(jsonPath("$.token_type").value("Bearer"))
				.andExpect(jsonPath("$.expires_in").value(3600));

	}
	
	@Autowired
	DSCacheManager dsCacheManager;
	
	@Test
	public void testCache() throws Exception{
		
		for(int i=0; i< 5; i++) {
			
		
			dsCacheManager.prepareAndRequestCacheDataByKey("Test1");
		}
		
	}

}
