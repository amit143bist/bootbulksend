package com.ds.proserv.report.data;

import java.util.Optional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@Profile({ "!unittest" })
@EnableTransactionManagement
@EnableJpaAuditing
@EnableCaching
@EnableScheduling // This enables scheduling to clear the cache
public class AuditConfiguration {

	@Bean
	public AuditorAware<String> auditorProvider(@Autowired DSCacheManager dsCacheManager) {
		return new AuditorAwareImpl();
	}

	@Bean
	public Pbkdf2PasswordEncoder pbkdf2PasswordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
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
		return Long.toString(Long.valueOf(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAUTH_CACHE_EXPIRY_SECS, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME))
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

class AuditorAwareImpl implements AuditorAware<String> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Override
	public Optional<String> getCurrentAuditor() {

		return Optional.of(dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_DB_AUDITOR_NAME));
	}
}