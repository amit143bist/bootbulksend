package com.ds.proserv.reportcomplete.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservReportCompleteConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservReportCompleteConsumerApplication.class, args);
	}

}