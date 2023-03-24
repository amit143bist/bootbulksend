package com.ds.proserv.dsapi.common.client;

import com.ds.proserv.cache.feign.FeignDSAuthenticationClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.authentication.service.AuthenticationService;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "authenticationClient", value = "dsauthentication", configuration = FeignDSAuthenticationClientConfiguration.class)
@RibbonClient(name = "dsauthentication", configuration = RibbonConfiguration.class)
public interface AuthenticationClient extends AuthenticationService {

}