package com.ds.proserv.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

@EnableCaching
@EnableScheduling // This enables scheduling to clear the cache
@Configuration
public class DSCacheConfiguration {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("token", "baseUrl", "propertyCache");
	}

	@Bean
	public String getScheduleCacheEvictFixedRate(@Autowired DSCacheManager dsCacheManager) {

		return Long.toString(Long
				.valueOf(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.DS_COMMON_CACHE_EXPIRY_SECS, PropertyCacheConstants.PROPERTY_CACHE_NAME))
				* 1000);
	}

}