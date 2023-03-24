package com.ds.proserv.bulksenddata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.bulksenddata.model.BulkSendRecordLog;
import com.ds.proserv.bulksenddata.repository.BulkSendRecordLogPagingAndSortingRepository;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
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
@SqlGroup({
		@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sqlscripts/beforeTestRun.sql"),
		@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sqlscripts/afterTestRun.sql") })
@Slf4j
public class BulkSendRecordLogControllerTests extends AbstractTests {

	@Autowired
	private BulkSendRecordLogPagingAndSortingRepository bulkSendRecordLogPagingAndSortingRepository;

	@Test
	public void bulkFindAllBulkSendRecordLogs_validUser() throws Exception {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RECORDIDS_PARAM_NAME);
		pageQueryParam.setParamValue("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26");

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		log.info("Calling update service");
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/bulksendrecordlog/bulkfindall/recordtype/landlordcp")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void bulkFindAllBulkSendRecordLogsSP_validUser() throws Exception {

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

		List<String> recordIds = new ArrayList<String>();
		recordIds.add("1185");
		recordIds.add("2250");
		recordIds.add("704");
		recordIds.add("2245");
		recordIds.add("1095");
		recordIds.add("1151");
		recordIds.add("2151");
		recordIds.add("1680");
		recordIds.add("2205");
		recordIds.add("537");
		
//		1185,2250,704,2245,1095,1151,2151,1680,2205,537

		for (String recordId : recordIds) {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(recordId);

			processSPDefinitionList.add(processSPDefinition);
		}

		try {

			String spJSON = new ObjectMapper().writeValueAsString(processSPDefinitionList);
			Iterable<BulkSendRecordLog> savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
					.getAllRecordByRecordTypeAndRecordIds("tenant", spJSON);

			log.info("Calling SP for recordType -> {} with spJSON -> {} and result is {}", "tenant", spJSON,
					savedBulkSendRecordLogs);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}
	}
}