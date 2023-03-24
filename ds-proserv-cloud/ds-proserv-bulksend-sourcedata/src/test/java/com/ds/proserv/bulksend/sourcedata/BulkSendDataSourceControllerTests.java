package com.ds.proserv.bulksend.sourcedata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;

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
public class BulkSendDataSourceControllerTests extends AbstractTests {

	@Test
	public void findRecordsByDateRange_validUser() throws Exception {

		String fromDate = "2020-01-01T16:29:27.387";
		String toDate = "2022-04-02T16:30:27.387";

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue("landlord");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue("fetchRecordIds");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName("inputFromDate");
		pageQueryParam.setParamValue(fromDate);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName("inputToDate");
		pageQueryParam.setParamValue(toDate);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGENUMBER_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(2));

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PAGINATIONLIMIT_PARAM_NAME);
		pageQueryParam.setParamValue(String.valueOf(2));

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		log.info("Calling recordids service");
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/bulksend/recordids")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalRecords").isNotEmpty());
	}

	@Test
	public void findRecordDataByIds_validUser() throws Exception {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue("landlord");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue("fetchRecordData");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName("LandlordAppIDs");
		pageQueryParam.setParamValue("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26");
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		log.info("Calling selectedrows service");
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/bulksend/selectedrows")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(print()).andExpect(jsonPath("$.totalRecords").isNotEmpty());
	}

	public void updateRecordDataByIds_validUser() throws Exception {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue("landlord");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue("updateRecordData");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName("LandlordAppIDs");
		pageQueryParam.setParamValue("2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26");
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		log.info("Calling update service");
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/bulksend/update/selectedrows")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void updateRecordDataByIds_validUser2() throws Exception {

		List<PageQueryParam> pageQueryParams = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_IDENTIFIER);
		pageQueryParam.setParamValue("landlord");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_QUERY_TYPE);
		pageQueryParam.setParamValue("updateRecordData");

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName("LandlordAppIDs");
		pageQueryParam.setParamValue("100001,100002");
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APP_PROCESS_STATUS);
		pageQueryParam.setParamValue(AppConstants.SUCCESS_VALUE);
		pageQueryParam.setDelimitedList(true);

		pageQueryParams.add(pageQueryParam);

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageQueryParams(pageQueryParams);

		log.info("Calling update service");
		mockMvc.perform(MockMvcRequestBuilders.put("/docusign/bulksend/update/selectedrows")
				.content(asJsonString(pageInformation)).with(httpBasic("docusignuser", "testing1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());
	}
}