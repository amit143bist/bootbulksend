package com.ds.proserv.coredata;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;

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
public class CoreProcessFailureLogControllerTests extends AbstractTests {

	@Test
	public void testCoreProcessFailureLogController_inValidUser_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure")
				.with(httpBasic("docusignuser", "invalidPassword")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "TESTUSER")
	public void testCoreProcessFailureLogController_inValidRoleUser_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testCoreProcessFailureLogController_validRole_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLogForConcurrentProcessId()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/scheduledbatch/concurrentprocessfailure/processes/84a3a1d3-02e0-4ca5-a5bc-590f37e0834e")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_listAllProcessFailureLogForFailureRecordId()
			throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/1234")
						.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").exists())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_saveBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/docusign/scheduledbatch/concurrentprocessfailure")
				.content(asJsonString(new ConcurrentProcessFailureLogDefinition(null,
						"84a3a1d3-02e0-4ca5-a5bc-590f37e0834f", "84a3a1d3-02e0-4ca5-a5bc-590f37e0834e", "ERROR_13",
						"Test Error", DateTimeUtil.convertToString(LocalDateTime.now()), null, "9999",
						"FETCH_PRONTO_DOC", null, null)))
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.processFailureId").isNotEmpty());
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_1() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.F.toString());
		concurrentProcessFailureLogRequest.setRetryCount(1l);
		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.message").value(
						"FailureDateTime is null for processFailureId# f228254a-6f82-4ec1-9c8a-44a1a20577f1 when RetryStatus is F"));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_2() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.F.toString());
		concurrentProcessFailureLogRequest.setRetryCount(1l);
		concurrentProcessFailureLogRequest.setFailureDateTime(DateTimeUtil.convertToString(LocalDateTime.now()));
		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.failureDateTime").isNotEmpty());
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_3() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.S.toString());
		concurrentProcessFailureLogRequest.setRetryCount(1l);
		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.message").value(
						"SuccessDateTime is null for processFailureId# f228254a-6f82-4ec1-9c8a-44a1a20577f1 when RetryStatus is S"));
	}

	@Test
	public void testCoreProcessFailureLogController_validUser_updateFailureLog_4() throws Exception {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest = new ConcurrentProcessFailureLogDefinition();
		concurrentProcessFailureLogRequest.setRetryStatus(RetryStatus.S.toString());
		concurrentProcessFailureLogRequest.setRetryCount(1l);
		concurrentProcessFailureLogRequest.setSuccessDateTime(DateTimeUtil.convertToString(LocalDateTime.now()));
		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/scheduledbatch/concurrentprocessfailure/processes/f228254a-6f82-4ec1-9c8a-44a1a20577f1")
				.content(asJsonString(concurrentProcessFailureLogRequest)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.successDateTime").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_listAllProcessFailureLog() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_listAllProcessFailuresByProcessIds() throws Exception {

		List<String> processIds = new ArrayList<String>();
		processIds.add("84a3a1d3-02e0-4ca5-a5bc-590f37e0835e");
		processIds.add("84a3a1d3-02e0-4ca5-a5bc-590f37e0836e");

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords")
				.content(asJsonString(processIds)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isNotEmpty())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions").isArray())
				.andExpect(jsonPath("$.concurrentProcessFailureLogDefinitions", hasSize(2)));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_countProcessFailuresByProcessIds() throws Exception {

		List<String> processIds = new ArrayList<String>();
		processIds.add("84a3a1d3-02e0-4ca5-a5bc-590f37e0835e");
		processIds.add("84a3a1d3-02e0-4ca5-a5bc-590f37e0836e");

		mockMvc.perform(MockMvcRequestBuilders
				.put("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/processids/count")
				.content(asJsonString(processIds)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$").value(2));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreProcessFailureLogController_countProcessFailures() throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.get("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/count")
						.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isNotEmpty()).andExpect(jsonPath("$").value(4));
	}
}