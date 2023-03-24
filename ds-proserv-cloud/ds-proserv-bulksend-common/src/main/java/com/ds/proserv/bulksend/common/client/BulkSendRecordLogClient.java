package com.ds.proserv.bulksend.common.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSBulkSendDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.bulksenddata.service.BulkSendRecordLogService;

@FeignClient(contextId = "bulkSendRecordLogClient", value = "dsbulksenddata", configuration = FeignDSBulkSendDataClientConfiguration.class)
@RibbonClient(name = "dsbulksenddata", configuration = RibbonConfiguration.class)
public interface BulkSendRecordLogClient extends BulkSendRecordLogService {

}