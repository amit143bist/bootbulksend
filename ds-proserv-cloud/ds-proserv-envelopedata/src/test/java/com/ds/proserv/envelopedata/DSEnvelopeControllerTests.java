package com.ds.proserv.envelopedata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.CustomFieldType;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.envelopedata.domain.EnvelopeSPRequest;
import com.ds.proserv.envelopedata.domain.RecipientSPRequest;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.repository.DSEnvelopeRepository;
import com.ds.proserv.envelopedata.repository.DSRecipientRepository;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorTestProvider")
@TestPropertySource(locations = "classpath:application-unittest.yml")
/*
 * @SqlGroup({
 * 
 * @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts =
 * "classpath:sqlscripts/beforeTestRun.sql"),
 * 
 * @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts =
 * "classpath:sqlscripts/afterTestRun.sql") })
 */
@AutoConfigureMockMvc
@Slf4j
public class DSEnvelopeControllerTests extends AbstractTests {

	@Autowired
	private DSEnvelopeRepository dsEnvelopeRepository;

	@Autowired
	private DSRecipientRepository dsRecipientRepository;

	@Test
	public void testDSTabController_validUser_findTabByTabLabelAndEnvelopeIds() throws Exception {

		DSEnvelopeDefinition dsEnvelopeDefinition = createEnvelopeDefinition();
		String envelopeId1 = dsEnvelopeDefinition.getEnvelopeId();

		List<DSEnvelopeDefinition> dsEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
		dsEnvelopeDefinitionList.add(dsEnvelopeDefinition);

		dsEnvelopeDefinition = createEnvelopeDefinition();
		String envelopeId2 = dsEnvelopeDefinition.getEnvelopeId();

		dsEnvelopeDefinitionList.add(dsEnvelopeDefinition);

		DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();
		dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitionList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/envelope/saveupdate/bulkv2")
				.content(asJsonString(dsEnvelopeInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalRecords").isNotEmpty());

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue(envelopeId1 + AppConstants.COMMA_DELIMITER + envelopeId2);

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();
		pageQueryParams.add(pageQueryParam);
		pageInformation.setPageQueryParams(pageQueryParams);
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/envelopestree/list/envelopeids")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalRecords").isNotEmpty());
	}

	private DSEnvelopeDefinition createEnvelopeDefinition() {

		String envelopeId = UUID.randomUUID().toString();
		String recipientId = UUID.randomUUID().toString();

		DSEnvelopeDefinition dsEnvelopeDefinition = new DSEnvelopeDefinition();

		List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();

		dsTabDefinitionList.add(createTab(envelopeId, recipientId, "Label1", "Value1"));
		dsTabDefinitionList.add(createTab(envelopeId, recipientId, "Label2", "Value2"));

		DSRecipientDefinition dsRecipientDefinition = new DSRecipientDefinition();
		dsRecipientDefinition.setRecipientId(recipientId);
		dsRecipientDefinition.setEnvelopeId(envelopeId);
		dsRecipientDefinition.setRoutingOrder(1L);
		dsRecipientDefinition.setRecipientName("Test");
		dsRecipientDefinition.setRecipientEmail("email@email.com");
		dsRecipientDefinition.setStatus("Test");

		dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);

		List<DSRecipientDefinition> dsRecipientDefinitions = new ArrayList<DSRecipientDefinition>();

		dsRecipientDefinitions.add(dsRecipientDefinition);

		recipientId = UUID.randomUUID().toString();
		dsRecipientDefinition = new DSRecipientDefinition();
		dsRecipientDefinition.setRecipientId(recipientId);
		dsRecipientDefinition.setEnvelopeId(envelopeId);
		dsRecipientDefinition.setRoutingOrder(2L);
		dsRecipientDefinition.setRecipientName("Test2");
		dsRecipientDefinition.setRecipientEmail("email2@email.com");
		dsRecipientDefinition.setStatus("Test2");

		dsTabDefinitionList = new ArrayList<DSTabDefinition>();
		dsTabDefinitionList.add(createTab(envelopeId, recipientId, "Label3", "Value3"));
		dsTabDefinitionList.add(createTab(envelopeId, recipientId, "Label4", "Value4"));
		dsTabDefinitionList.add(createTab(envelopeId, recipientId, "Label5", "Value5"));

		dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);

		dsRecipientDefinitions.add(dsRecipientDefinition);

		dsEnvelopeDefinition.setEnvelopeId(envelopeId);
		dsEnvelopeDefinition.setSenderName("Test Name");
		dsEnvelopeDefinition.setSenderEmail("sender@sender.com");
		dsEnvelopeDefinition.setStatus("Test Env Status");
		dsEnvelopeDefinition.setSentDateTime(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setTimeGenerated(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setEnvelopeSubject("Test Subject");
		dsEnvelopeDefinition.setDsRecipientDefinitions(dsRecipientDefinitions);

		DSCustomFieldDefinition dsCustomFieldDefinition = new DSCustomFieldDefinition();
		dsCustomFieldDefinition.setFieldName("TestFieldName1");
		dsCustomFieldDefinition.setFieldValue("TestFieldValue1");
		dsCustomFieldDefinition.setEnvelopeId(envelopeId);
		dsCustomFieldDefinition.setFieldType(CustomFieldType.ECF.toString());

		List<DSCustomFieldDefinition> dsCustomFieldDefinitionList = new ArrayList<DSCustomFieldDefinition>();
		dsCustomFieldDefinitionList.add(dsCustomFieldDefinition);

		dsCustomFieldDefinition = new DSCustomFieldDefinition();
		dsCustomFieldDefinition.setFieldName("TestFieldName2");
		dsCustomFieldDefinition.setFieldValue("TestFieldValue2");
		dsCustomFieldDefinition.setEnvelopeId(envelopeId);
		dsCustomFieldDefinition.setFieldType(CustomFieldType.ECF.toString());

		dsCustomFieldDefinitionList.add(dsCustomFieldDefinition);

		dsEnvelopeDefinition.setFileNames("Abc.pdf,TestLinks.docx");
		dsEnvelopeDefinition.setDsCustomFieldDefinitions(dsCustomFieldDefinitionList);

		return dsEnvelopeDefinition;
	}

	private DSTabDefinition createTab(String envelopeId, String recipientId, String tabLabel, String tabValue) {

		DSTabDefinition dsTabDefinition = new DSTabDefinition();
		dsTabDefinition.setTabLabel(tabLabel);
		dsTabDefinition.setTabValue(tabValue);
		dsTabDefinition.setTabStatus("Active");
		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientId);
		return dsTabDefinition;
	}

	@Test
	public void testDSEnvelopeInsertUpdateSP() throws JsonProcessingException {

		DSEnvelope dsEnvelope = new DSEnvelope();

		dsEnvelope.setCompletedDateTime(LocalDateTime.now());
		dsEnvelope.setDeclinedDateTime(LocalDateTime.now());
		dsEnvelope.setDeliveredDateTime(LocalDateTime.now());
		dsEnvelope.setEnvelopeId(UUID.randomUUID().toString());
		dsEnvelope.setEnvelopeSubject("Test Subject");
		dsEnvelope.setFileNames("Abc.pdf,TestLinks.docx");
		dsEnvelope.setSenderName("Test Name");
		dsEnvelope.setSenderEmail("sender@sender.com");
		dsEnvelope.setSentDateTime(LocalDateTime.now());
		dsEnvelope.setStatus("Test Env Status");
		dsEnvelope.setTerminalReason("Terminal Test");
		dsEnvelope.setTimeGenerated(LocalDateTime.now());

		dsEnvelope.setTimeZone("Pacific");
		dsEnvelope.setTimeZoneoffset(-1L);

		List<DSEnvelope> dsEnvelopeList = new ArrayList<DSEnvelope>();
		dsEnvelopeList.add(dsEnvelope);

		EnvelopeSPRequest envelopeSPRequest = new EnvelopeSPRequest();
		envelopeSPRequest.setDsEnvelopes(dsEnvelopeList);

		ObjectMapper obj = new ObjectMapper();
		obj.registerModule(new JavaTimeModule());
		obj.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		log.info("CreateUpdate Request is {}", obj.writeValueAsString(envelopeSPRequest));

		String result = dsEnvelopeRepository.insertUpdate(obj.writeValueAsString(envelopeSPRequest));
		log.info("result -> {}", result);

	}

	@Test
	public void testDSRecipientInsertUpdateSP() throws JsonProcessingException {

		List<DSRecipient> dsRecipientList = new ArrayList<DSRecipient>();

		String envelopeId = UUID.randomUUID().toString();
		DSRecipient dsRecipient = new DSRecipient();
		dsRecipient.setClientUserId("Test123");
		dsRecipient.setDeclinedDateTime(LocalDateTime.now());
		dsRecipient.setDeclineReason("Decline Reason Test");
		dsRecipient.setDeliveredDateTime(LocalDateTime.now());
		dsRecipient.setEnvelopeId(envelopeId);
		dsRecipient.setRecipientEmail("email@email.com");
		dsRecipient.setRecipientIPAddress("11:11:11:11:11:11");
		dsRecipient.setRecipientName("Test");
		dsRecipient.setRoutingOrder(1L);
		dsRecipient.setRecipientId(UUID.randomUUID().toString());

		dsRecipient.setSentDateTime(LocalDateTime.now());
		dsRecipient.setSignedDateTime(LocalDateTime.now());
		dsRecipient.setStatus("Test");

		dsRecipientList.add(dsRecipient);

		dsRecipient = new DSRecipient();
		dsRecipient.setClientUserId("Test124");
		dsRecipient.setDeclinedDateTime(LocalDateTime.now());
		dsRecipient.setDeclineReason("Decline Reason Test2");
		dsRecipient.setDeliveredDateTime(LocalDateTime.now());
		dsRecipient.setEnvelopeId(envelopeId);
		dsRecipient.setRecipientEmail("email2@email.com");
		dsRecipient.setRecipientIPAddress("11:11:11:11:11:11");
		dsRecipient.setRecipientName("Test2");
		dsRecipient.setRoutingOrder(2L);

		dsRecipient.setSentDateTime(LocalDateTime.now());
		dsRecipient.setSignedDateTime(LocalDateTime.now());
		dsRecipient.setStatus("Test");
		dsRecipient.setRecipientId(UUID.randomUUID().toString());

		dsRecipientList.add(dsRecipient);

		RecipientSPRequest recipientSPRequest = new RecipientSPRequest();
		recipientSPRequest.setDsRecipients(dsRecipientList);

		ObjectMapper obj = new ObjectMapper();
		obj.registerModule(new JavaTimeModule());
		obj.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		log.info("CreateUpdate Request is {}", obj.writeValueAsString(recipientSPRequest));

		String result = dsRecipientRepository.insertUpdate(obj.writeValueAsString(recipientSPRequest));
		log.info("result -> {}", result);

	}
}