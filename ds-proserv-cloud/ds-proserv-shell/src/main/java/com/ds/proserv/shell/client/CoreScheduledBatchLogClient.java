package com.ds.proserv.shell.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSCoreDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.coredata.service.CoreScheduledBatchLogService;

@FeignClient(contextId = "coreScheduledBatchLogClient", value = "dscoredata", configuration = FeignDSCoreDataClientConfiguration.class)
@RibbonClient(name = "dscoredata", configuration = RibbonConfiguration.class)
public interface CoreScheduledBatchLogClient extends CoreScheduledBatchLogService {

}