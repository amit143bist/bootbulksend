package com.ds.proserv.commondata.client;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;

import com.ds.proserv.cache.feign.FeignDSAuthenticationClientConfiguration;
import com.ds.proserv.cache.feign.RibbonConfiguration;
import com.ds.proserv.feign.authentication.service.AuthenticationService;

@FeignClient(contextId = "authenticationClient", value = "dsauthentication", configuration = FeignDSAuthenticationClientConfiguration.class)
@RibbonClient(name = "dsauthentication", configuration = RibbonConfiguration.class)
public interface AuthenticationClient extends AuthenticationService {

}