package com.ds.proserv.bulksendenvelopelog.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSEnvelopeDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.envelopedata.service.DSCustomFieldService;

@FeignClient(contextId = "dsCustomFieldClient", value = "dsenvelopedata", configuration = FeignDSEnvelopeDataClientConfiguration.class)
@RibbonClient(name = "dsenvelopedata", configuration = RibbonConfiguration.class)
public interface DSCustomFieldClient extends DSCustomFieldService {

}