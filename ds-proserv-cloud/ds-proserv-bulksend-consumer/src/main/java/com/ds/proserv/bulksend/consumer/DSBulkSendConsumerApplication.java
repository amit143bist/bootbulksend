package com.ds.proserv.bulksend.consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSBulkSendConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSBulkSendConsumerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
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
}