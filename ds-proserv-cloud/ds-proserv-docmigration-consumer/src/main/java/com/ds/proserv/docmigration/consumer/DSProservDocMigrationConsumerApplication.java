package com.ds.proserv.docmigration.consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.report.domain.DownloadDocs;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservDocMigrationConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservDocMigrationConsumerApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public ScriptEngineManager scriptEngineManager() {

		return new ScriptEngineManager();
	}

	@Bean
	public ScriptEngine scriptEngine(@Autowired ScriptEngineManager scriptEngineManager) {

		return scriptEngineManager.getEngineByName("nashorn");
	}

	@Bean
	public RuleEngineDefinition ruleEngineDefinition(@Autowired DSCacheManager dsCacheManager,
			@Autowired ObjectMapper objectMapper)
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		return objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DOCMIGRATION_RULE_ENGINE_PATH))),
				RuleEngineDefinition.class);
	}

	@Bean
	public DownloadDocs downloadDocs(@Autowired DSCacheManager dsCacheManager, @Autowired ObjectMapper objectMapper)
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		return objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DOCDOWNLOAD_RULE_ENGINE_PATH))),
				DownloadDocs.class);
	}
}