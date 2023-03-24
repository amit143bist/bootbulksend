package com.ds.proserv.envelopeapi.consumer;

import java.io.File;
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservEnvelopeApiConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservEnvelopeApiConsumerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public HttpHeaders httpHeaders() {
		return new HttpHeaders();
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
	public RuleEngineDefinition landlordRuleEngineDefinition(@Autowired DSCacheManager dsCacheManager,
			@Autowired ObjectMapper objectMapper) throws IOException {
		return objectMapper.readValue(
				new FileReader(new File(dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.API_LANDLORD_RULE_ENGINE))),
				RuleEngineDefinition.class);
	}

	@Bean
	public RuleEngineDefinition tenantRuleEngineDefinition(@Autowired DSCacheManager dsCacheManager,
			@Autowired ObjectMapper objectMapper) throws IOException {
		return objectMapper.readValue(
				new FileReader(new File(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.API_TENANT_RULE_ENGINE))),
				RuleEngineDefinition.class);
	}
}