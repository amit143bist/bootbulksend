package com.ds.proserv.exception;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

//@EnableFeignClients(basePackages = { "com.ds.proserv.connect.client", "com.ds.proserv.exception.client" })
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableScheduling
@EnableFeignClients(basePackages = { "com.ds.proserv" })
@Slf4j
public class DSExceptionBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSExceptionBatchApplication.class, args);
	}

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}

	@Bean
	public String getScheduleFixedRate(@Autowired DSCacheManager dsCacheManager) {
		return Long.toString(Long.valueOf(
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DS_TRIGGER_RATE_IN_SECS)) * 1000);
	}

	@Bean
	public JAXBContext jaxbContext() {

		JAXBContext jaxbContext = null;
		try {

			jaxbContext = JAXBContext.newInstance(DocuSignEnvelopeInformation.class);

		} catch (JAXBException e) {

			log.error("Exception {} occurred in jaxbContext initiation", e);
			e.printStackTrace();
		}

		return jaxbContext;
	}
}