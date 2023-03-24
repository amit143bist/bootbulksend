package com.ds.proserv.batch.common.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSCoreDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.coredata.service.CoreConcurrentProcessLogService;

@FeignClient(contextId = "coreConcurrentProcessLogClient", value = "dscoredata", configuration = FeignDSCoreDataClientConfiguration.class)
@RibbonClient(name = "dscoredata", configuration = RibbonConfiguration.class)
public interface CoreConcurrentProcessLogClient extends CoreConcurrentProcessLogService {

}