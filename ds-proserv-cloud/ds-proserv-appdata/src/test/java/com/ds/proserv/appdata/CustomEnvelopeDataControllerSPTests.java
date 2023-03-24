package com.ds.proserv.appdata;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.ds.proserv.appdata.model.CustomEnvelopeData;
import com.ds.proserv.appdata.repository.CustomEnvelopeDataPagingAndSortingRepository;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
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
public class CustomEnvelopeDataControllerSPTests extends AbstractTests {

	@Autowired
	private CustomEnvelopeDataPagingAndSortingRepository customEnvelopeDataPagingAndSortingRepository;

	@Test
	@Transactional
	public void test_updateCustomEnvelopeDataProcessStatusStartTimeBySP() throws Exception {

		List<String> customEnvelopeDataList = new ArrayList<String>();
		customEnvelopeDataList.add("123");
		customEnvelopeDataList.add("291917A8-C554-4743-83C6-312229851239");

		customEnvelopeDataPagingAndSortingRepository
				.updateCustomEnvelopeDataProcessStatusStartTimeBySP(createRecordIdJSON(customEnvelopeDataList));

	}

	private String createRecordIdJSON(List<String> customEnvelopeDataList) {

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

		for (String recordId : customEnvelopeDataList) {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(recordId);

			processSPDefinitionList.add(processSPDefinition);
		}

		String recordIdsAsJSON;
		try {

			recordIdsAsJSON = new ObjectMapper().writeValueAsString(processSPDefinitionList);

			if (log.isDebugEnabled()) {

				log.debug("calling SP sucessfully with recordIdsAsJSON -> {}", recordIdsAsJSON);
			}
		} catch (JsonProcessingException e) {

			e.printStackTrace();
			throw new ResourceNotSavedException(e.getMessage());
		}
		return recordIdsAsJSON;
	}

	@Test
	@Transactional
	public void test_getCustomEnvelopeDataByIdBySP() throws Exception {

		CustomEnvelopeData customEnvelopeData = customEnvelopeDataPagingAndSortingRepository.getCustomEnvelopeDataById("00004BC3-DEB1-4FFA-BB77-DC16B31D826E");
		log.info("customEnvelopeData envId -> {}", customEnvelopeData.getEnvelopeId());

	}

}