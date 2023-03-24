package com.ds.proserv.bulksend.sourcedata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.bulksend.sourcedata.domain.BulkSendSqlDefinition;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.security.config.DSSecurityConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableWebSecurity
@EnableSwagger2
@EnableRetry
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservBulkSendSourceDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservBulkSendSourceDataApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {

		return new ObjectMapper();
	}
	
	@Bean
	public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
		return new DSSecurityConfig();
	}

	@Bean
	public BulkSendSqlDefinition bulkSendSqlDefinition(@Autowired DSCacheManager dsCacheManager,
			@Autowired ObjectMapper objectMapper)
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		return objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSBULKSEND_SQL_DEFINITION_PATH))),
				BulkSendSqlDefinition.class);

	}

}