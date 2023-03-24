package com.ds.proserv.notification;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableScheduling // This enables scheduling to clear the cache
@EnableSwagger2
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservNotificationBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservNotificationBatchApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean(name = "httpTransport")
	public HttpTransport HttpTransport() {

		HttpTransport HTTP_TRANSPORT = null;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}

		return HTTP_TRANSPORT;
	}

	@Bean(name = "jsonFactory")
	public JsonFactory JsonFactory() {

		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
		return JSON_FACTORY;
	}

	@Bean
	public String getScheduleDeadQueueFixedRate(@Autowired DSCacheManager dsCacheManager) {
		return Long.toString(Long.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.NOTIFICATION_DEADQUEUE_RATE_IN_SECS)) * 1000);
	}
	
	@Bean
	public String getScheduleMigrationReadyFixedRate(@Autowired DSCacheManager dsCacheManager) {
		return Long.toString(Long.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.NOTIFICATION_MIGRATIONREADY_RATE_IN_SECS)) * 1000);
	}
	
	@Bean
	public PrepareReportDefinition prepareReportDefinition(@Autowired DSCacheManager dsCacheManager, @Autowired ObjectMapper objectMapper)
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		return objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CSVDOWNLOAD_RULE_ENGINE_PATH))),
				PrepareReportDefinition.class);
	}

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}
}