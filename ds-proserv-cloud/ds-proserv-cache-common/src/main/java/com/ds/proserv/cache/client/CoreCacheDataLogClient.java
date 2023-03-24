package com.ds.proserv.cache.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSCacheDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.cachedata.service.CoreCacheDataLogService;

@FeignClient(contextId = "dsCacheClient", value = "dscachedata", configuration = FeignDSCacheDataClientConfiguration.class)
@RibbonClient(name = "dscachedata", configuration = RibbonConfiguration.class)
public interface CoreCacheDataLogClient extends CoreCacheDataLogService {

}