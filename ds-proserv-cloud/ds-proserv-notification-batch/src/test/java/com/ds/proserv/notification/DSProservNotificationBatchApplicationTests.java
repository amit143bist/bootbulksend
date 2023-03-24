package com.ds.proserv.notification;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ds.proserv.batch.common.client.CoreScheduledBatchLogClient;
import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.email.common.client.ClientCredentialClient;
import com.ds.proserv.email.common.client.NotificationDetailClient;
import com.ds.proserv.notification.client.CustomEnvelopeDataClient;
import com.ds.proserv.notification.client.FolderNotificationClient;
import com.ds.proserv.notification.client.MigrationDataClient;
import com.ds.proserv.notification.service.CreateCSVService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
@Slf4j
public class DSProservNotificationBatchApplicationTests extends AbstractTests {

	@MockBean
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	@MockBean
	private FolderNotificationClient folderNotificationClient;

	@MockBean
	private MigrationDataClient migrationDataClient;

	@MockBean
	private ClientCredentialClient clientCredentialClient;

	@MockBean
	private NotificationDetailClient notificationDetailClient;

	@MockBean
	private CoreScheduledBatchLogClient coreScheduledBatchLogClient;

	@MockBean
	private CreateCSVService createCSVService;

	@MockBean
	private CoreBatchDataService coreBatchDataService;

	@Test
	public void test_GmailAuth() throws Exception {

		log.info("Inside gmailauth check");
		mockMvc.perform(MockMvcRequestBuilders.get("/gmailauth").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void test_fetchGoogleToken() throws Exception {

		log.info("Inside GoogleToken check");
		mockMvc.perform(MockMvcRequestBuilders.get(
				"/fetchgoogletoken?authCode=4/0AY0e-g4eUubVM5PQSlbWFe0dWyqTxxLC06_S9mcWaIiKxgyAYk1t1CHReJz3OhgahU4Qwg")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isPreconditionFailed());
	}

}