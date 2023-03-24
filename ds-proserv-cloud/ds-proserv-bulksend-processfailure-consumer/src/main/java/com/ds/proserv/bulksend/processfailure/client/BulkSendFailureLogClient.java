package com.ds.proserv.bulksend.processfailure.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSBulkSendDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.bulksenddata.service.BulkSendFailureLogService;

@FeignClient(contextId = "bulkSendFailureLogClient", value = "dsbulksenddata", configuration = FeignDSBulkSendDataClientConfiguration.class)
@RibbonClient(name = "dsbulksenddata", configuration = RibbonConfiguration.class)
public interface BulkSendFailureLogClient extends BulkSendFailureLogService {

}