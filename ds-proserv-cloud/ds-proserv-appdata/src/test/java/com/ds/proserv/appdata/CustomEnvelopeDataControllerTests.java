package com.ds.proserv.appdata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorTestProvider")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@SqlGroup({
		@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sqlscripts/beforeTestRun.sql"),
		@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
@AutoConfigureMockMvc
@Slf4j
public class CustomEnvelopeDataControllerTests extends AbstractTests {

	@Test
	public void findAndUpdateEnvelopeByDateRange_validUser() throws Exception {

		String fromDate = "2021-01-01T16:29:27.387";
		String toDate = "2021-02-02T16:30:27.387";

		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/customdata/fromdate/" + fromDate + "/todate/" + toDate
						+ "/status/empty/count/1/pagenumber/0")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.nextUri").isNotEmpty()).andExpect(jsonPath("$.nextAvailable").value(true));
	}

	@Test
	public void findAndUpdateEnvelopeByDateRangeAndSenderIdentifier_validUser() throws Exception {

		String fromDate = "2021-01-01T16:29:27.387";
		String toDate = "2021-02-02T16:30:27.387";

		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/customdata/senderidentifier/testsender/fromdate/" + fromDate + "/todate/" + toDate
						+ "/status/empty/count/1/pagenumber/0")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.nextUri").isNotEmpty()).andExpect(jsonPath("$.nextAvailable").value(true));
	}

	@Test
	public void findAndUpdateEnvelopeByDateRangeAndSenderIdentifier_NotCompleted_validUser() throws Exception {

		String fromDate = "2021-01-01T16:29:27.387";
		String toDate = "2021-02-02T16:30:27.387";

		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/customdata/senderidentifier/testsender/fromdate/" + fromDate + "/todate/" + toDate
						+ "/status/notcompleted/count/1/pagenumber/0")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.nextUri").isNotEmpty()).andExpect(jsonPath("$.nextAvailable").value(true));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void findAndUpdateEnvelopeByDateRangeAndSenderIdentifier_Empty_validUser() throws Exception {

		String fromDate = "2021-01-01T16:29:27.387";
		String toDate = "2021-06-02T16:30:27.387";

		String nextUri = "/docusign/customdata/senderidentifier/65c366bf-e370-40cd-9812-a11481838b31/fromdate/"
				+ fromDate + "/todate/" + toDate + "/status/empty/count/20/pagenumber/0";

		nextUri = extractNextUri(fromDate, toDate, nextUri);

		while (!StringUtils.isEmpty(nextUri)) {

			nextUri = extractNextUri(fromDate, toDate, nextUri);
		}

	}

	private String extractNextUri(String fromDate, String toDate, String nextUri)
			throws Exception, UnsupportedEncodingException, JsonProcessingException, JsonMappingException {

		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.get(nextUri).with(httpBasic("docusignuser", "testing1"))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print()).andReturn();

		ObjectMapper obj = new ObjectMapper();
		CustomEnvelopeDataInformation customEnvelopeDataInformation = obj
				.readValue(result.getResponse().getContentAsString(), CustomEnvelopeDataInformation.class);

		nextUri = customEnvelopeDataInformation.getNextUri();

		log.info("NextUri is -> {}", customEnvelopeDataInformation.getNextUri());

		return nextUri;
	}

	@Test
	public void findAllDownloadedEnvelopesCountByDate() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/customdata/envelope/docdownload/count/bydate")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print())
				.andExpect(jsonPath("$.totalRecords").isNotEmpty());

	}

}