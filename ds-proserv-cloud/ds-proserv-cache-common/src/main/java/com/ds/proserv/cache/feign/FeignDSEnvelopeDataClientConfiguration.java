package com.ds.proserv.cache.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignDSEnvelopeDataClientConfiguration {

	@Bean
	public BasicAuthRequestInterceptor basicDSEnvelopeDataAuthRequestInterceptor(
			@Autowired DSCacheManager dsCacheManager) {

		String basicAuthUserName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.BASICAUTH_DSENVELOPEDATA_USERNAME,
				PropertyCacheConstants.DBSERVICE_REFERENCE_NAME);
		String basicAuthUserPassword = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.BASICAUTH_DSENVELOPEDATA_PASSWORD,
				PropertyCacheConstants.DBSERVICE_REFERENCE_NAME);

		log.info("To call DSENVELOPEDATA, basicAuthUserName is {}, basicAuthUserPassword is {}", basicAuthUserName,
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