package com.ds.proserv.coredata;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.report.domain.BatchStartParams;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class CoreScheduledBatchLogControllerTests extends AbstractTests {

	@Test
	public void testCoreScheduledBatchLogController_inValidUser_findInCompleteBatch1() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "test")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "TESTUSER")
	public void testCoreScheduledBatchLogController_inValidUser_findInCompleteBatch2() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batchtype/docusignbatch")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testCoreScheduledBatchLogController_validRole_findInCompleteBatch3() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batchtype/docusignbatch")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402d"));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findInCompleteBatch4() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402d"));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findAllInCompleteBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/incompletebatches/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(5)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllCompleteBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/completebatches/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(2)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllBatchesByBatchType() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batches/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(3)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findAllBatches() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/batches")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(4)));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_findLatestBatchByBatchType() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/scheduledbatch/latestbatch/batchtype/docusignbatch")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("e781ca58-dec7-44b7-a312-5c21fded402f"));
	}

	@Test
	public void testCoreScheduledBatchLogController_validUser_saveBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/docusign/scheduledbatch")
				.content(asJsonString(new ScheduledBatchLogRequest("docusignbatch",
						new ObjectMapper().writeValueAsString(new BatchStartParams(LocalDateTime.now().toString(),
								LocalDateTime.now().plusHours(5).toString(), 50)),
						20l)))
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.batchId").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_updateBatch() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/scheduledbatch/b4ad9898-dd2f-43d4-b685-dd08aebc5065")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").isNotEmpty()).andExpect(jsonPath("$.batchEndDateTime").isNotEmpty());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findBatchByBatchId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders
				.get("/docusign/scheduledbatch/latestbatch/batchid/b4ad9898-dd2f-43d4-b685-dd08aebc5063")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchId").exists()).andExpect(jsonPath("$.batchId").isNotEmpty())
				.andExpect(jsonPath("$.batchId").value("b4ad9898-dd2f-43d4-b685-dd08aebc5063"));
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
					"classpath:sqlscripts/beforeTestRun.sql", "classpath:sqlscripts/preMethodTestRun.sql" }),
			@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
	public void testCoreScheduledBatchLogController_validUser_findAllByBatchTypeAndBatchStartDateTimeBetween()
			throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get(
				"/docusign/scheduledbatch/batches/batchtype/docusignbatch/fromdate/2019-09-10T00:00:00.000/todate/2019-09-14T23:59:59.999")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").exists())
				.andExpect(jsonPath("$.scheduledBatchLogResponses").isArray())
				.andExpect(jsonPath("$.scheduledBatchLogResponses", hasSize(2)));
	}

}