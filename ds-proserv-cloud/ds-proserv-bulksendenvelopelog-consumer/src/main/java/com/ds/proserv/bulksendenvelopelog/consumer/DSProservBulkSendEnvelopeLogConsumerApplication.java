package com.ds.proserv.bulksendenvelopelog.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservBulkSendEnvelopeLogConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservBulkSendEnvelopeLogConsumerApplication.class, args);
	}

}