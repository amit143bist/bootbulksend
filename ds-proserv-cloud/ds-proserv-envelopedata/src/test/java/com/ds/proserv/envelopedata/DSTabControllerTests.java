package com.ds.proserv.envelopedata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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

import com.ds.proserv.common.constant.CustomFieldType;
import com.ds.proserv.envelopedata.domain.CustomFieldSPRequest;
import com.ds.proserv.envelopedata.domain.TabSPRequest;
import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSCustomFieldPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class DSTabControllerTests extends AbstractTests {

	@Autowired
	private DSTabRepository dsTabRepository;

	@Autowired
	private DSCustomFieldPagingAndSortingRepository dsCustomFieldPagingAndSortingRepository;

	@Test
	public void testDSTabController_validUser_findTabByTabLabelAndEnvelopeIda() throws Exception {

		// "id", "exceptionReason", "envelopeId", "envelopeXml", "retryStatus",
		// "retryCount", "retryDateTime",
		// "exceptionCode", "exceptionStep", "exceptionDateTime"
		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/envelopedata/tab/tabLabel/templatetype/envelope/12d08eb0dc-8333-4edb-b5ca-4cca453cef3c")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNotEmpty());
	}

	@Test
	public void testtDSTabController_saveEnvelopeData() throws Exception {

		String envelopeId = UUID.randomUUID().toString();
		String recipientId = UUID.randomUUID().toString();

		DSEnvelopeDefinition dsEnvelopeDefinition = new DSEnvelopeDefinition();

		List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();

		DSTabDefinition dsTabDefinition = new DSTabDefinition();
		dsTabDefinition.setTabLabel("Label1");
		dsTabDefinition.setTabValue("Value1");
		dsTabDefinition.setTabStatus("Active");
		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientId);

		dsTabDefinitionList.add(dsTabDefinition);

		dsTabDefinition = new DSTabDefinition();
		dsTabDefinition.setTabLabel("Label2");
		dsTabDefinition.setTabValue("Value2");
		dsTabDefinition.setTabStatus("Active");
		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientId);

		dsTabDefinitionList.add(dsTabDefinition);

		DSRecipientDefinition dsRecipientDefinition = new DSRecipientDefinition();
		dsRecipientDefinition.setRecipientId(recipientId);
		dsRecipientDefinition.setEnvelopeId(envelopeId);
		dsRecipientDefinition.setRecipientName("Test");
		dsRecipientDefinition.setRecipientEmail("email@email.com");
		dsRecipientDefinition.setStatus("Test");

		dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);

		List<DSRecipientDefinition> dsRecipientDefinitions = new ArrayList<DSRecipientDefinition>();

		dsRecipientDefinitions.add(dsRecipientDefinition);

		dsEnvelopeDefinition.setEnvelopeId(envelopeId);
		dsEnvelopeDefinition.setSenderName("Test Name");
		dsEnvelopeDefinition.setSenderEmail("sender@sender.com");
		dsEnvelopeDefinition.setStatus("Test Env Status");
		dsEnvelopeDefinition.setSentDateTime(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setTimeGenerated(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setEnvelopeSubject("Test Subject");
		dsEnvelopeDefinition.setDsRecipientDefinitions(dsRecipientDefinitions);

		List<DSEnvelopeDefinition> dsEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
		dsEnvelopeDefinitionList.add(dsEnvelopeDefinition);

		DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();
		dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitionList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/envelope/saveupdate/bulkv2")
				.content(asJsonString(dsEnvelopeInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())/* .andExpect(jsonPath("$.envelopeId").isNotEmpty()) */;

		dsTabDefinitionList = new ArrayList<DSTabDefinition>();

		dsTabDefinition = new DSTabDefinition();
		dsTabDefinition.setTabLabel("Label1");
		dsTabDefinition.setTabValue("UpdatedValue1");
		dsTabDefinition.setTabStatus("Active");
		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientId);

		dsTabDefinitionList.add(dsTabDefinition);

		dsTabDefinition = new DSTabDefinition();
		dsTabDefinition.setTabLabel("Label2");
		dsTabDefinition.setTabValue("UpdatedValue2");
		dsTabDefinition.setTabStatus("Active");
		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientId);

		dsTabDefinitionList.add(dsTabDefinition);

		dsRecipientDefinition = new DSRecipientDefinition();
		dsRecipientDefinition.setRecipientId(recipientId);
		dsRecipientDefinition.setEnvelopeId(envelopeId);
		dsRecipientDefinition.setRecipientName("Test");
		dsRecipientDefinition.setRecipientEmail("email@email.com");
		dsRecipientDefinition.setStatus("Test");

		dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);

		dsRecipientDefinitions = new ArrayList<DSRecipientDefinition>();

		dsRecipientDefinitions.add(dsRecipientDefinition);

		dsEnvelopeDefinition.setEnvelopeId(envelopeId);
		dsEnvelopeDefinition.setSenderName("Test Name");
		dsEnvelopeDefinition.setSenderEmail("sender@sender.com");
		dsEnvelopeDefinition.setStatus("Test Env Status");
		dsEnvelopeDefinition.setSentDateTime(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setTimeGenerated(LocalDateTime.now().toString());
		dsEnvelopeDefinition.setEnvelopeSubject("Test Subject");
		dsEnvelopeDefinition.setDsRecipientDefinitions(dsRecipientDefinitions);

		dsEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
		dsEnvelopeDefinitionList.add(dsEnvelopeDefinition);

		dsEnvelopeInformation = new DSEnvelopeInformation();
		dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitionList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/envelope/saveupdate/bulkv2")
				.content(asJsonString(dsEnvelopeInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())/* .andExpect(jsonPath("$.envelopeId").isNotEmpty()) */;

	}

	@Test
	public void testSP1() throws JsonProcessingException {

		DSTab dsTab = new DSTab();
		dsTab.setId(UUID.randomUUID().toString());
		dsTab.setEnvelopeId(UUID.randomUUID().toString());
		dsTab.setRecipientId(UUID.randomUUID().toString());
		dsTab.setTabLabel("TabLabel_1");
		dsTab.setTabName("TabName_1");
		dsTab.setTabOriginalValue("TABOV_1");
		dsTab.setTabStatus("Active");
		dsTab.setTabValue("TV_1");

		List<DSTab> dsTabList = new ArrayList<>();
		dsTabList.add(dsTab);

		dsTab = new DSTab();
		dsTab.setEnvelopeId(UUID.randomUUID().toString());
		dsTab.setRecipientId(UUID.randomUUID().toString());
		dsTab.setTabLabel("TabLabel_2");
		dsTab.setTabName("TabName_2");
		dsTab.setTabOriginalValue("TABOV_2");
		dsTab.setTabStatus("Signed");
		dsTab.setTabValue("TV_2");

		dsTabList.add(dsTab);

		TabSPRequest tabSPRequest = new TabSPRequest();
		tabSPRequest.setDsTabs(dsTabList);

		ObjectMapper obj = new ObjectMapper();
		log.info(obj.writeValueAsString(tabSPRequest));

		String result = dsTabRepository.insertUpdate(obj.writeValueAsString(tabSPRequest));
		log.info("result after call SP is {}", result);

	}

	public void testSP2() throws JsonProcessingException {

		ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
		processSPDefinition.setRecordId("86446dd8-b10d-4b65-85fa-9a9b6fed1298");

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
		processSPDefinitionList.add(processSPDefinition);
		String json = new ObjectMapper().writeValueAsString(processSPDefinitionList);

		log.info("json -> {}", json);

		Iterable<DSTab> results = dsTabRepository.getAllTabsByEnvelopeIdsAfterSentDateTime(json,
				LocalDateTime.parse("2021-04-08T17:29:50.910"));

		log.info("results -> {}", results);
	}

	@Test
	public void testSP3() throws JsonProcessingException {

		DSTab dsTab = new DSTab();
		dsTab.setId(UUID.randomUUID().toString());
		dsTab.setEnvelopeId(UUID.randomUUID().toString());
		dsTab.setRecipientId(UUID.randomUUID().toString());
		dsTab.setTabLabel("TabLabel_1");
		dsTab.setTabName("TabName_1");
		dsTab.setTabOriginalValue("TABOV_1");
		dsTab.setTabStatus("Active");
		dsTab.setTabValue("TV_1");

		List<DSTab> dsTabList = new ArrayList<>();
		dsTabList.add(dsTab);

		dsTab = new DSTab();
		dsTab.setEnvelopeId(UUID.randomUUID().toString());
		dsTab.setRecipientId(UUID.randomUUID().toString());
		dsTab.setTabLabel("TabLabel_2");
		dsTab.setTabName("TabName_2");
		dsTab.setTabOriginalValue("TABOV_2");
		dsTab.setTabStatus("Signed");
		dsTab.setTabValue("TV_2");

		dsTabList.add(dsTab);

		TabSPRequest tabSPRequest = new TabSPRequest();
		tabSPRequest.setDsTabs(dsTabList);

		ObjectMapper obj = new ObjectMapper();
		log.info(obj.writeValueAsString(tabSPRequest));

		String result = dsTabRepository.update(obj.writeValueAsString(tabSPRequest));
		log.info("result -> {}", result);

		/*
		 * dsTabRepository.insert(obj.writeValueAsString(tabSPRequest)).handle((result,
		 * exp) -> {
		 * 
		 * log.info("result -> {}", result); if (null != exp) {
		 * 
		 * exp.printStackTrace(); } return result; });
		 */

	}

	@Test
	public void testSP4() throws JsonProcessingException {

		DSCustomField dsCustomFieldDefinition = new DSCustomField();
		dsCustomFieldDefinition.setFieldName("TestFieldName1");
		dsCustomFieldDefinition.setFieldValue("TestFieldValue1");
		dsCustomFieldDefinition.setEnvelopeId(UUID.randomUUID().toString());
		dsCustomFieldDefinition.setFieldType(CustomFieldType.ECF.toString());

		List<DSCustomField> dsCustomFieldDefinitionList = new ArrayList<DSCustomField>();
		dsCustomFieldDefinitionList.add(dsCustomFieldDefinition);

		CustomFieldSPRequest customSPRequest = new CustomFieldSPRequest();
		customSPRequest.setDsCustomFields(dsCustomFieldDefinitionList);

		ObjectMapper obj = new ObjectMapper();
		log.info(obj.writeValueAsString(customSPRequest));

		String result = dsCustomFieldPagingAndSortingRepository.insertUpdate(obj.writeValueAsString(customSPRequest));
		log.info("result -> {}", result);

	}
}