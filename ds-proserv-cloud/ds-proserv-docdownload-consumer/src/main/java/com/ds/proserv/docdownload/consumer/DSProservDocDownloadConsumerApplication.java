package com.ds.proserv.docdownload.consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservDocDownloadConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservDocDownloadConsumerApplication.class, args);
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
	public String getScheduleFixedRate(@Autowired DSCacheManager dsCacheManager) {
		return Long.toString(Long.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAUTH_CACHE_EXPIRY_SECS, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME))
				* 1000);
	}
	
	@Bean(name = "recordTaskExecutor")
	public TaskExecutor recordTaskExecutor(@Autowired DSCacheManager dsCacheManager) {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer
				.valueOf(dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CORE_POOL_SIZE_CONST)));
		executor.setMaxPoolSize(Integer
				.valueOf(dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MAX_POOL_SIZE_CONST)));
		executor.setQueueCapacity(Integer.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_QUEUE_CAPACITY_CONST)));
		executor.setThreadNamePrefix(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_EXECUTOR_NAME_CONST));
		return executor;
	}

}