package com.ds.proserv.docmigration.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSAppDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.appdata.service.CustomEnvelopeDataService;

@FeignClient(contextId = "dsCustomEnvelopeDataClient", value = "dsappdata", configuration = FeignDSAppDataClientConfiguration.class)
@RibbonClient(name = "dsappdata", configuration = RibbonConfiguration.class)
public interface CustomEnvelopeDataClient extends CustomEnvelopeDataService {

}