package com.ds.proserv.connect.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSEnvelopeDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.envelopedata.service.DSExceptionService;

@FeignClient(contextId = "dsExceptionClient", value = "dsenvelopedata", configuration = FeignDSEnvelopeDataClientConfiguration.class)
@RibbonClient(name = "dsenvelopedata", configuration = RibbonConfiguration.class)
public interface DSExceptionClient extends DSExceptionService {

}