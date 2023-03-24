package com.ds.proserv.envelopeapi.consumer.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.envelopeapi.domain.CreateEnvelopeResponse;
import com.ds.proserv.feign.envelopeapi.domain.GenericEnvelopeMessageDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false")
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
public class DocuSignEnvelopeServiceTest {

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AccountService accountService;

	@MockBean
	private QueueService queueService;

//	@MockBean @Qualifier("landlordRuleEngineDefinition")
//	private RuleEngineDefinition landlordRuleEngineDefinition;
//
//	@MockBean @Qualifier("tenantRuleEngineDefinition")
//	RuleEngineDefinition tenantRuleEngineDefinition;

	@Autowired
	private DocuSignEnvelopeService docuSignEnvelopeService;

	@Value("${app.authorization.token.testtoken}")
	private String testtoken;

	@Value("${app.authorization.accountguid}")
	private String accountguid;

	@Test
	public void createEnvelope() throws IOException {
		GenericEnvelopeMessageDefinition genericEnvelopeDefinition = objectMapper.readValue(
				new FileReader(new File("src/test/resources/testdata/test1.json")),
				GenericEnvelopeMessageDefinition.class);
		AuthenticationResponse authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setAccessToken(testtoken);

		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUser("user");
		authenticationRequest.setScopes("scope");
		Mockito.doReturn(authenticationResponse).when(accountService).getTokenForUser(authenticationRequest);
		Mockito.doReturn(authenticationResponse).when(accountService).getTokenForSystemUser();

		AccountDefinition accountDefinition = new AccountDefinition();
		accountDefinition.setBaseUri("https://demo.docusign.net");
		accountDefinition.setAccountId(accountguid);
		Mockito.doReturn(accountDefinition).when(accountService).getAccount(accountguid);

		Mockito.doNothing().when(queueService).createFailureMessageAndSend(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(Throwable.class), Mockito.any(FailureCode.class),
				Mockito.any(FailureStep.class));
		CreateEnvelopeResponse createEnvelopeResponse = docuSignEnvelopeService
				.createEnvelope(genericEnvelopeDefinition, "created");
		Assert.assertNotNull(createEnvelopeResponse);
		Assert.assertNotNull(createEnvelopeResponse.getEnvelopeId());
		Assert.assertNotNull(createEnvelopeResponse.getStatusDateTime());
		Assert.assertNotNull(createEnvelopeResponse.getUri());
	}
}