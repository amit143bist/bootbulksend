package com.ds.proserv.cache.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.domain.CacheLogInformation;
import com.ds.proserv.feign.cachedata.domain.CacheTokenRequest;
import com.ds.proserv.feign.cachedata.service.CoreCacheDataLogService;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class DSCacheService {

	@Autowired
	private CacheManager cacheManager;

	/*
	 * @Autowired private CoreCacheDataLogClient coreCacheDataLogClient;
	 */

	@Autowired
	private CoreCacheDataLogService coreCacheDataLogService;

	private String cacheName = "propertyCache";

	@Cacheable(value = "propertyCache", key = "#cacheTokenRequest.cacheReference", sync = true)
	public ResponseEntity<CacheLogInformation> requestCacheData(CacheTokenRequest cacheTokenRequest) {
		
		log.info("Trying to call database in DSCacheService.requestCacheData() for cacheReference -> {}",
				cacheTokenRequest.getCacheReference());

		Assert.notNull(cacheTokenRequest.getCacheReference(), "cacheTokenRequest.cacheReference cannot be empty");

		log.debug("~~~~~~~~~~~~~~~~~~~~ Either Direct DB Controller or DB Client is available ~~~~~~~~~~~~~~~~~~~~");
		return coreCacheDataLogService.findAllByCacheReference(cacheTokenRequest.getCacheReference());
	}

	public ResponseEntity<String> deleteByCacheKeyAndCacheReference(String cacheKey, String cacheReference) {

		return coreCacheDataLogService.deleteByCacheKeyAndCacheReference(cacheKey, cacheReference);
	}

	public ResponseEntity<CacheLogDefinition> findByCacheKeyAndCacheReference(String cacheKey, String cacheReference) {

		return coreCacheDataLogService.findByCacheKeyAndCacheReference(cacheKey, cacheReference);
	}

	public ResponseEntity<CacheLogDefinition> findByCacheValueAndCacheReference(String cacheValue,
			String cacheReference) {

		return coreCacheDataLogService.findByCacheKeyAndCacheReference(cacheValue, cacheReference);
	}

	public ResponseEntity<CacheLogDefinition> updateCache(String cacheId, CacheLogDefinition cacheLogDefinition) {

		return coreCacheDataLogService.updateCache(cacheId, cacheLogDefinition);
	}

	public ResponseEntity<CacheLogDefinition> saveCache(CacheLogDefinition cacheLogDefinition) {

		return coreCacheDataLogService.saveCache(cacheLogDefinition);
	}

	public ResponseEntity<CacheLogDefinition> findByCacheKey(String cacheKey) {

		return coreCacheDataLogService.findByCacheKey(cacheKey);
	}

	public String evictCache(String cacheReference) {

		log.info("evictCache called, now clearing the tokens for all cacheReference -> {}", cacheReference);
		cacheManager.getCache(cacheName).evictIfPresent(cacheReference);

		return ValidationResult.SUCCESS.toString();
	}

}