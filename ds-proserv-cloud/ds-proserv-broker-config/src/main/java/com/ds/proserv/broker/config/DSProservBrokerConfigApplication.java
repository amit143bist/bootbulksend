package com.ds.proserv.broker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
public class DSProservBrokerConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSProservBrokerConfigApplication.class, args);
	}

}