package com.ds.proserv.docdownload.consumer.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSAppDataClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.appdata.service.FolderNotificationService;

@FeignClient(contextId = "folderNotificationClient", value = "dsappdata", configuration = FeignDSAppDataClientConfiguration.class)
@RibbonClient(name = "dsappdata", configuration = RibbonConfiguration.class)
public interface FolderNotificationClient extends FolderNotificationService {

}