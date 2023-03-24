package com.ds.proserv.feign.cachedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;

public interface CoreCacheRefreshService {

	@PutMapping("/docusign/cachelog/evict")
	ResponseEntity<String> evictCache();
}