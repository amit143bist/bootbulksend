package com.ds.proserv.envelopedata;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.domain.PageSortParam;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;

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
public class DSExceptionControllerTests extends AbstractTests {

	@Test
	public void testDSExceptionController_validUser_saveExceptionData() throws Exception {

		// "id", "exceptionReason", "envelopeId", "envelopeXml", "retryStatus",
		// "retryCount", "retryDateTime",
		// "exceptionCode", "exceptionStep", "exceptionDateTime"
		String connectXML = new String(Files.readAllBytes(Paths.get("src/test/resources/testConnect.xml")),
				StandardCharsets.UTF_8);

		mockMvc.perform(MockMvcRequestBuilders.post("/docusign/envelopedata/exception")
				.content(asJsonString(new DSExceptionDefinition(null, "TESTSCRIPT Exception",
						"412b8d31-6d58-4e7c-9cfe-20e0b021cd09", connectXML, null, null, null, "TEST_ERROR_CODE",
						"TEST_ERROR_STEP", LocalDateTime.now().toString())))
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testDSExceptionController_validUser_updateBatch() throws Exception {

		String connectXML = new String(Files.readAllBytes(Paths.get("src/test/resources/testConnect.xml")),
				StandardCharsets.UTF_8);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/docusign/envelopedata/exception/a37e4daa-5c78-4db0-8abc-537f08f0d8b5")
						.content(asJsonString(new DSExceptionDefinition("a37e4daa-5c78-4db0-8abc-537f08f0d8b5",
								"org.springframework.web.server.ResponseStatusException: 500 INTERNAL_SERVER_ERROR",
								"412b8d31-6d58-4e7c-9cfe-20e0b021cd09", connectXML, RetryStatus.F.toString(), 1L,
								LocalDateTime.now().toString(), "TEST_ERROR_CODE", "TEST_ERROR_STEP",
								LocalDateTime.now().toString())))
						.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.retryStatus").isNotEmpty())
				.andExpect(jsonPath("$.retryStatus").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testDSExceptionController_validUser_updateExceptionRetryStatus() throws Exception {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(0);
		pageInformation.setRecordsPerPage(4);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.EXCEPTIONIDS_PARAM_NAME);
		pageQueryParam.setParamValue("a37e4daa-5c78-4db0-8abc-537f08f0d8b5");

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RETRYSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue("T");

		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PROCESSID_PARAM_NAME);
		pageQueryParam.setParamValue(UUID.randomUUID().toString());

		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/exception/bulk/retrystatus")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testDSExceptionController_validUser_findAllExceptionsByEnvelopeIds() throws Exception {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(0);
		pageInformation.setRecordsPerPage(4);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.ENVELOPEIDS_PARAM_NAME);
		pageQueryParam.setParamValue("412b8d31-6d58-4e7c-9cfe-20e0b021cd09");

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		PageSortParam pageSortParam = new PageSortParam();
		pageSortParam.setFieldName("exceptionDateTime");
		List<PageSortParam> pageSortParamList = new ArrayList<PageSortParam>();
		pageSortParamList.add(pageSortParam);

		pageInformation.setPageSortParams(pageSortParamList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/exceptions/envelopeids")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.dsExceptionDefinitions").exists())
				.andExpect(jsonPath("$.dsExceptionDefinitions").isArray())
				.andExpect(jsonPath("$.dsExceptionDefinitions", hasSize(4)))
				.andExpect(jsonPath("$.nextAvailable").isBoolean()).andExpect(jsonPath("$.nextAvailable").value(true));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testDSExceptionController_validUser_findAllExceptionIdsByRetryStatusesOrNullRetryStatus()
			throws Exception {

		PageInformation pageInformation = new PageInformation();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RETRYSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue(null);

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/envelopedata/exceptions/exceptionids/byretrystatusesincludingnull")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(MockMvcResultHandlers.print()).andExpect(jsonPath("$.dsEnvelopeIds").exists())
				.andExpect(jsonPath("$.dsEnvelopeIds").isArray()).andExpect(jsonPath("$.dsEnvelopeIds", hasSize(8)))
				.andExpect(jsonPath("$.totalRecords").exists()).andExpect(jsonPath("$.totalRecords").value(8));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testDSExceptionController_validUser_findAllExceptionsByDateRange() throws Exception {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(0);
		pageInformation.setRecordsPerPage(4);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.FROMDATETIME_PARAM_NAME);
		pageQueryParam.setParamValue("2020-07-01T10:15:30");

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.TODATETIME_PARAM_NAME);
		pageQueryParam.setParamValue("2020-07-28T10:15:30");
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		PageSortParam pageSortParam = new PageSortParam();
		pageSortParam.setFieldName("exceptionDateTime");
		pageSortParam.setSortDirection("desc");
		List<PageSortParam> pageSortParamList = new ArrayList<PageSortParam>();
		pageSortParamList.add(pageSortParam);

		pageInformation.setPageSortParams(pageSortParamList);

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/envelopedata/exceptions/daterange")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.dsExceptionDefinitions").exists())
				.andExpect(jsonPath("$.dsExceptionDefinitions").isArray())
				.andExpect(jsonPath("$.dsExceptionDefinitions", hasSize(4)))
				.andExpect(jsonPath("$.nextAvailable").isBoolean()).andExpect(jsonPath("$.nextAvailable").value(true));
	}
}