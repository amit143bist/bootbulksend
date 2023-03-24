package com.ds.proserv.feign.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
public class DSFeignServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSFeignServiceApplication.class, args);
	}
}