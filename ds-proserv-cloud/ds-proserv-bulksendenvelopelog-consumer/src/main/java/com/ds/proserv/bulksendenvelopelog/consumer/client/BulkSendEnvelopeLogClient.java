package com.ds.proserv.bulksendenvelopelog.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSBulkSendDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.bulksenddata.service.BulkSendEnvelopeLogService;

@FeignClient(contextId = "bulkSendEnvelopeLogClient", value = "dsbulksenddata", configuration = FeignDSBulkSendDataClientConfiguration.class)
@RibbonClient(name = "dsbulksenddata", configuration = RibbonConfiguration.class)
public interface BulkSendEnvelopeLogClient extends BulkSendEnvelopeLogService {

}