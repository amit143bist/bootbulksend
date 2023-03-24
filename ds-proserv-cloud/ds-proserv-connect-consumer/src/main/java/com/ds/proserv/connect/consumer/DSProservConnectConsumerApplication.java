package com.ds.proserv.connect.consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
@EnableAsync(proxyTargetClass = true)
@Slf4j
public class DSProservConnectConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservConnectConsumerApplication.class, args);
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