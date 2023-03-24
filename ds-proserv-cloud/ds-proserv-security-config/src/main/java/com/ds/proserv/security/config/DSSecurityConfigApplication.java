package com.ds.proserv.security.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
public class DSSecurityConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSSecurityConfigApplication.class, args);
	}

}