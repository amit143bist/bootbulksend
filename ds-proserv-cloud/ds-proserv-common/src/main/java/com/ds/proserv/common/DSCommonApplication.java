package com.ds.proserv.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
public class DSCommonApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSCommonApplication.class, args);
	}

}