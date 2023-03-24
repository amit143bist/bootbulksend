package com.ds.proserv.feign.cachedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.domain.CacheLogInformation;

public interface CoreCacheDataLogService {

	@PostMapping("/docusign/cachelog")
	ResponseEntity<CacheLogDefinition> saveCache(@RequestBody CacheLogDefinition cacheLogDefinition);

	@PutMapping("/docusign/cachelog/{cacheId}")
	ResponseEntity<CacheLogDefinition> updateCache(@PathVariable String cacheId,
			@RequestBody CacheLogDefinition cacheLogDefinition);

	@PostMapping("/docusign/cachelog/saveall")
	ResponseEntity<String> saveAllCache(@RequestBody CacheLogInformation cacheLogInformation);

	@PostMapping("/docusign/cachelog/updateall")
	ResponseEntity<CacheLogInformation> updateAllCache(@RequestBody CacheLogInformation cacheLogInformation);

	@DeleteMapping("/docusign/cachelog/{cacheId}")
	ResponseEntity<String> deleteByCacheId(@PathVariable String cacheId);

	@DeleteMapping("/docusign/cachelog/cachekey/{cacheKey}")
	ResponseEntity<String> deleteByCacheKey(@PathVariable String cacheKey);

	@DeleteMapping("/docusign/cachelog/cachekey/{cacheKey}/cachereference/{cacheReference}")
	ResponseEntity<String> deleteByCacheKeyAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheReference);

	@GetMapping("/docusign/cachelog/{cacheId}")
	ResponseEntity<CacheLogDefinition> findByCacheId(@PathVariable String cacheId);

	@GetMapping("/docusign/cachelog/cachekey/{cacheKey}")
	ResponseEntity<CacheLogDefinition> findByCacheKey(@PathVariable String cacheKey);

	@GetMapping("/docusign/cachelog/cachekey/{cacheKey}/cachevalue/{cacheValue}")
	ResponseEntity<CacheLogDefinition> findByCacheKeyAndCacheValue(@PathVariable String cacheKey,
			@PathVariable String cacheValue);

	@GetMapping("/docusign/cachelog/cachekey/{cacheKey}/cachereference/{cacheReference}")
	ResponseEntity<CacheLogDefinition> findByCacheKeyAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheReference);

	@GetMapping("/docusign/cachelog/cachekey/{cacheKey}/cachevalue/{cacheValue}/cachereference/{cacheReference}")
	ResponseEntity<CacheLogDefinition> findByCacheKeyValueAndCacheReference(@PathVariable String cacheKey,
			@PathVariable String cacheValue, @PathVariable String cacheReference);

	@GetMapping("/docusign/cachelog")
	ResponseEntity<CacheLogInformation> findAllCacheDataLog();

	@GetMapping("/docusign/cachelog/cachereference/{cacheReference}")
	ResponseEntity<CacheLogInformation> findAllByCacheReference(@PathVariable String cacheReference);

	@GetMapping("/docusign/cachelog/cachevalue/{cacheValue}/cachereference/{cacheReference}")
	ResponseEntity<CacheLogDefinition> findByCacheValueAndCacheReference(@PathVariable String cacheValue,
			@PathVariable String cacheReference);

	@GetMapping("/docusign/cachelog/cachevalue/{cacheValue}")
	ResponseEntity<CacheLogInformation> findByCacheValue(@PathVariable String cacheValue);

}