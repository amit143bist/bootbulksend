package com.ds.proserv.email.common.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSNotificationDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.notificationdata.service.ClientCredentialService;

@FeignClient(contextId = "clientCredentialClient", value = "dsnotificationdata", configuration = FeignDSNotificationDataClientConfiguration.class)
@RibbonClient(name = "dsnotificationdata", configuration = RibbonConfiguration.class)
public interface ClientCredentialClient extends ClientCredentialService {

}