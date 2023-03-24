package com.ds.proserv.connect;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.connect.client.DSEnvelopeClient;
import com.ds.proserv.connect.client.DSExceptionClient;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
@Slf4j
public class DSConnectListenerApplicationTests extends AbstractTests {

	@MockBean
	private DSEnvelopeClient dsEnvelopeClient;

	@MockBean
	private DSExceptionClient dsExceptionClient;

	@Test
	public void testConnectListener() {

		Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Test Check")).when(dsEnvelopeClient)
				.findEnvelopeByEnvelopeId("412b8d31-6d58-4e7c-9cfe-20e0b021cd00");

		try {
			String xml = new String(Files.readAllBytes(Paths.get("src/test/resources/testConnect.xml")),
					StandardCharsets.UTF_8);

			processConnectDataAsync(xml);
			processConnectDataAsync(xml);

			mockMvc.perform(
					MockMvcRequestBuilders.post("/connect/notification").with(httpBasic("connectuser", "connect@Test1"))
							.content(xml).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andExpect(jsonPath("$").isNotEmpty());

			Thread.sleep(60000);
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processConnectDataAsync(String connectXML) {

		Executors.newCachedThreadPool().submit(() -> {

			log.info("processConnectDataAsync triggered async");
			try {
				mockMvc.perform(MockMvcRequestBuilders.post("/connect/notification")
						.with(httpBasic("connectuser", "connect@Test1")).content(connectXML)
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});

	}

}