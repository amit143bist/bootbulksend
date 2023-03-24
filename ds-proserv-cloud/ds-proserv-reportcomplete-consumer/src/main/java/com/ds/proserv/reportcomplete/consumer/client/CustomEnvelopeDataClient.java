package com.ds.proserv.reportcomplete.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSReportDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.appdata.service.CustomEnvelopeDataService;

@FeignClient(contextId = "customEnvelopeDataClient", value = "dsappdata", configuration = FeignDSReportDataClientConfiguration.class)
@RibbonClient(name = "dsappdata", configuration = RibbonConfiguration.class)
public interface CustomEnvelopeDataClient extends CustomEnvelopeDataService {

}