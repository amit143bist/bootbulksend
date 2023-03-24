package com.ds.proserv.appdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.feign.domain.ProcessSPDefinition;
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
public class MigrationDataControllerTests extends AbstractTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void readReportData_validUser() throws Exception {

		List<String> recordIds = new ArrayList<String>();
		recordIds.add("49AFEED0-8229-4329-82CD-85B670DF31E0");
		recordIds.add("17B8E0BC-95EE-4C37-9A3D-DC9DA29EC1AE");

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

		for (String recordId : recordIds) {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(recordId);

			processSPDefinitionList.add(processSPDefinition);
		}

		String spName = "{call sproc_getrppmigrationdataforenvid(?)}";
		String spJSON = new ObjectMapper().writeValueAsString(processSPDefinitionList);

		List<Map<String, Object>> selectDataMapList = jdbcTemplate.queryForList(spName, spJSON);

		selectDataMapList.forEach(selectDataMap -> {

			selectDataMap.forEach((key, value) -> {

				log.info("Key {} - > value {}", key, value);
			});

		});
	}

}