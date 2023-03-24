package com.ds.proserv.bulksend.common.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSBulkSendSourceDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.bulksend.sourcedata.service.BulkSendDataSourceService;

@FeignClient(contextId = "bulkSendDataSourceClient", value = "dsbulksendsourcedata", configuration = FeignDSBulkSendSourceDataClientConfiguration.class)
@RibbonClient(name = "dsbulksendsourcedata", configuration = RibbonConfiguration.class)
public interface BulkSendDataSourceClient extends BulkSendDataSourceService {

}