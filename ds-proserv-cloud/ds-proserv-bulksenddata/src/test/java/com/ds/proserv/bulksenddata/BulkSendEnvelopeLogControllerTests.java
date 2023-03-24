package com.ds.proserv.bulksenddata;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.bulksenddata.repository.BulkSendEnvelopeLogPagingAndSortingRepositoryRepository;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
@Slf4j
public class BulkSendEnvelopeLogControllerTests extends AbstractTests {

	@Autowired
	private BulkSendEnvelopeLogPagingAndSortingRepositoryRepository bulkSendEnvelopeLogPagingAndSortingRepositoryRepository;

	@Test
	public void saveAllBulkSendEnvelopeLogsSP_validUser() throws Exception {

		List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList = new ArrayList<BulkSendEnvelopeLogDefinition>();

		BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition = new BulkSendEnvelopeLogDefinition();
		bulkSendEnvelopeLogDefinition.setBulkBatchId(UUID.randomUUID().toString());
		bulkSendEnvelopeLogDefinition.setEnvelopeId(UUID.randomUUID().toString());

		bulkSendEnvelopeLogDefinitionList.add(bulkSendEnvelopeLogDefinition);

		bulkSendEnvelopeLogDefinition = new BulkSendEnvelopeLogDefinition();
		bulkSendEnvelopeLogDefinition.setBulkBatchId(UUID.randomUUID().toString());
		bulkSendEnvelopeLogDefinition.setEnvelopeId(UUID.randomUUID().toString());

		bulkSendEnvelopeLogDefinitionList.add(bulkSendEnvelopeLogDefinition);

		try {

			String spJSON = new ObjectMapper().writeValueAsString(bulkSendEnvelopeLogDefinitionList);
			String result = bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.insert(spJSON);

			log.info("Calling SP for recordType -> {} with spJSON -> {} and result is {}", "tenant", spJSON, result);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}
	}
}