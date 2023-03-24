package com.ds.proserv.appdata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorTestProvider")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
public class DrawApplicationControllerTests extends AbstractTests {

	@Test
	public void findByEnvelopeId1_validUser() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/draw/envelope/95db8758-9ce7-4d42-b407-42def5ffe5b4")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void findByEnvelopeId2_validUser() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/draw/envelope/95db8758-9ce7-4d42-b407-42def5ffe5b6")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void findByEnvelopeId3_validUser() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/draw/envelope/b925239b-cce3-41c9-af35-813011cab007")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void findByEnvelopeId4_validUser() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/docusign/draw/envelope/324112fa-4e03-4e16-8cc4-64f14ae80a7d")
				.with(httpBasic("docusignuser", "testing1")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(print());
	}

}