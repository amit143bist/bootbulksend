package com.ds.proserv.cache.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSScheduler {

	@Autowired
	private CacheManager cacheManager;
	
	private String cacheName = "propertyCache";
	
	/**
	 * Clear all tokens from token cache, every
	 */
	@Scheduled(fixedRateString = "#{@getScheduleCacheEvictFixedRate}")
	public void evictAuthenticationCache() {

		log.info("evictAuthenticationCache scheduled called, now clearing the cache for {}", cacheName);
		cacheManager.getCache(cacheName).clear();
	}
}