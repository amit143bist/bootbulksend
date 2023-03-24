package com.ds.proserv.report.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSReportDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.report.service.CoreReportDataService;

@FeignClient(contextId = "coreReportDataClient", value = "dsreportdata", configuration = FeignDSReportDataClientConfiguration.class)
@RibbonClient(name = "dsreportdata", configuration = RibbonConfiguration.class)
public interface CoreReportDataClient extends CoreReportDataService {

}