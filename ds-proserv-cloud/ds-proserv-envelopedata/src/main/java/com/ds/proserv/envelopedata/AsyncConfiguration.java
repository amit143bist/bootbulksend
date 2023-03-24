package com.ds.proserv.envelopedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfiguration {

	@Autowired
	private DSCacheManager dsCacheManager;

	private static final String TASK_EXECUTOR_NAME_PREFIX_PROCESSOR = "processorTaskExecutor-";

	public static final String TASK_EXECUTOR_PROCESSOR = "processorAsyncExecutor";

	@Bean(name = TASK_EXECUTOR_PROCESSOR)
	@Primary
	public ThreadPoolTaskExecutor recordTaskExecutor() {
		return createTaskExecutor(TASK_EXECUTOR_NAME_PREFIX_PROCESSOR);
	}

	@Bean
	public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor delegate) {
		return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
	}

	private ThreadPoolTaskExecutor createTaskExecutor(final String taskExecutorNamePrefix) {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_CORE_POOLSIZE, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setMaxPoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_MAX_POOLSIZE, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setQueueCapacity(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_QUEUE_CAPACITY, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setThreadNamePrefix(taskExecutorNamePrefix);
		return executor;
	}
}