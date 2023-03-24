package com.ds.proserv.cache.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignDSCacheDataClientConfiguration {

	@Value("${proserv.dscachedata.username}")
	private String basicAuthUserName;

	@Value("${proserv.dscachedata.password}")
	private String basicAuthUserPassword;

	@Bean
	public BasicAuthRequestInterceptor basicDSCacheDataAuthRequestInterceptor(
			@Autowired DSCacheManager dsCacheManager) {

		log.info("To call DSCACHEDATA, basicAuthUserName is {}, basicAuthUserPassword is {}", basicAuthUserName,
				basicAuthUserPassword);
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