package com.ds.proserv.appdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.appdata.domain.ApplicationEnvelopeSPRequest;
import com.ds.proserv.appdata.repository.ApplicationEnvelopeDataPagingAndSortingRepository;
import com.ds.proserv.appdata.transformer.ApplicationEnvelopeDataTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;
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
@AutoConfigureMockMvc
@Slf4j
public class ApplicationEnvelopeControllerTests extends AbstractTests {

	@Autowired
	private ApplicationEnvelopeDataTransformer applicationEnvelopeDataTransformer;

	@Autowired
	private ApplicationEnvelopeDataPagingAndSortingRepository applicationEnvelopeDataPagingAndSortingRepository;

	@Test
	public void saveAppEnvData_validUser() throws Exception {

		String applicationId = UUID.randomUUID().toString();
		String envelopeId = UUID.randomUUID().toString();

		ApplicationEnvelopeDefinition applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

		applicationEnvelopeDefinition.setApplicationId(applicationId);
		applicationEnvelopeDefinition.setEnvelopeId(envelopeId);
		applicationEnvelopeDefinition.setEnvelopeSentTimestamp(LocalDateTime.now().toString());

		List<String> emails = Stream.of("abc@gmail.com,test@gmail.com".split(",", -1)).collect(Collectors.toList());
		applicationEnvelopeDefinition.setRecipientEmails(emails);

		applicationEnvelopeDefinition.setApplicationType("Test");
		applicationEnvelopeDefinition.setCommunityPartnerCode("12333");

		saveApplicationEnvelopeData(applicationId, applicationEnvelopeDefinition);

		log.info("Trying again and this time it should not save");

		applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

		applicationId = UUID.randomUUID().toString();

		applicationEnvelopeDefinition.setApplicationId(applicationId);
		applicationEnvelopeDefinition.setEnvelopeId(envelopeId);
		applicationEnvelopeDefinition.setEnvelopeSentTimestamp(LocalDateTime.now().toString());

		emails = Stream.of("abc@gmail.com,test@gmail.com".split(",", -1)).collect(Collectors.toList());
		applicationEnvelopeDefinition.setRecipientEmails(emails);

		applicationEnvelopeDefinition.setApplicationType("Test");
		applicationEnvelopeDefinition.setCommunityPartnerCode("12333");

		saveApplicationEnvelopeData(applicationId, applicationEnvelopeDefinition);

	}

	@Test
	public void saveAppEnvDataFailureScenario_validUser() throws Exception {

		String applicationId = UUID.randomUUID().toString();

		ApplicationEnvelopeDefinition applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

		applicationEnvelopeDefinition.setApplicationId(applicationId);
		applicationEnvelopeDefinition.setFailureTimestamp(LocalDateTime.now().toString());
		applicationEnvelopeDefinition.setFailureReason("Reason1");

		List<String> emails = Stream.of("failabc@gmail.com,failabc@gmail.com".split(",", -1))
				.collect(Collectors.toList());
		applicationEnvelopeDefinition.setRecipientEmails(emails);

		applicationEnvelopeDefinition.setApplicationType("Test");
		applicationEnvelopeDefinition.setCommunityPartnerCode("12333");

		saveApplicationEnvelopeData(applicationId, applicationEnvelopeDefinition);

		log.info("Trying again and this time it should save");

		applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

		applicationId = UUID.randomUUID().toString();

		applicationEnvelopeDefinition.setApplicationId(applicationId);
		applicationEnvelopeDefinition.setFailureTimestamp(LocalDateTime.now().toString());
		applicationEnvelopeDefinition.setFailureReason("Reason2");

		emails = Stream.of("failabcabc@gmail.com,failabctest@gmail.com".split(",", -1)).collect(Collectors.toList());
		applicationEnvelopeDefinition.setRecipientEmails(emails);

		applicationEnvelopeDefinition.setApplicationType("Test");
		applicationEnvelopeDefinition.setCommunityPartnerCode("12333");

		saveApplicationEnvelopeData(applicationId, applicationEnvelopeDefinition);

	}

	private void saveApplicationEnvelopeData(String applicationId,
			ApplicationEnvelopeDefinition applicationEnvelopeDefinition) {

		log.info("ApplicationId is {}", applicationId);
		List<ApplicationEnvelopeSPRequest> applicationEnvelopeSPRequestList = new ArrayList<ApplicationEnvelopeSPRequest>();
		applicationEnvelopeSPRequestList.add(applicationEnvelopeDataTransformer
				.transformToApplicationEnvelopeSPRequest(applicationEnvelopeDefinition));

		try {

			String json = new ObjectMapper().writeValueAsString(applicationEnvelopeSPRequestList);

			String result = applicationEnvelopeDataPagingAndSortingRepository.insert(json);

			if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

				throw new ResourceNotSavedException("ApplicationEnvelopeData not saved for " + applicationId);
			}
		} catch (JsonProcessingException exp) {
			exp.printStackTrace();
			throw new ResourceNotSavedException("ApplicationEnvelopeData not saved for " + applicationId);
		}
	}

}