package com.ds.proserv.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableConfigServer
public class DSConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSConfigServerApplication.class, args);
	}

}