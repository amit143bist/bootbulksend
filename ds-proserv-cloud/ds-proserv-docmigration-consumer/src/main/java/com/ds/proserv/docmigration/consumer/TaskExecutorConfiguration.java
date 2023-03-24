package com.ds.proserv.docmigration.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

@Configuration
public class TaskExecutorConfiguration {

	public static final String TASK_EXECUTOR_RECORD = "recordTaskExecutor";
	public static final String TASK_EXECUTOR_XML = "xmlTaskExecutor";

	@Bean(name = "recordTaskExecutor")
	public TaskExecutor recordTaskExecutor(@Autowired DSCacheManager dsCacheManager) {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_CORE_POOLSIZE, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setMaxPoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_MAX_POOLSIZE, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setQueueCapacity(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_QUEUE_CAPACITY, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setThreadNamePrefix(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_EXECUTORNAME_PREFIX,
				PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME));
		return executor;
	}

	@Bean(name = "xmlTaskExecutor")
	public TaskExecutor xmlTaskExecutor(@Autowired DSCacheManager dsCacheManager) {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_XML_CORE_POOLSIZE,
				PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setMaxPoolSize(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_XML_MAX_POOLSIZE, PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setQueueCapacity(Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_XML_QUEUE_CAPACITY,
				PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME)));
		executor.setThreadNamePrefix(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_ASYNC_XML_EXECUTORNAME_PREFIX,
				PropertyCacheConstants.DS_ASYNC_JOB_REFERENCE_NAME));
		return executor;
	}
}