package com.ds.proserv.bulksendmonitor.batch;

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
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class DSProservBulkSendMonitorBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservBulkSendMonitorBatchApplication.class, args);
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
	public String getScheduleFixedRate(@Autowired DSCacheManager dsCacheManager) {
		return Long.toString(Long.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DS_TRIGGER_RATE_IN_SECS)) * 1000);
	}

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}

}