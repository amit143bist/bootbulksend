package com.ds.proserv.cache.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignClientConfiguration {

	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor(@Autowired DSCacheManager dsCacheManager) {

		String basicAuthUserName = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.BASICAUTH_APP_USERNAME);
		String basicAuthUserPassword = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.BASICAUTH_APP_PASSWORD);

		log.info("basicAuthUserName is {}, basicAuthUserPassword is {}", basicAuthUserName, basicAuthUserPassword);

		return new BasicAuthRequestInterceptor(basicAuthUserName, basicAuthUserPassword);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public FeignErrorDecoder errorDecoder() {
		return new FeignErrorDecoder(objectMapper());
	}
}