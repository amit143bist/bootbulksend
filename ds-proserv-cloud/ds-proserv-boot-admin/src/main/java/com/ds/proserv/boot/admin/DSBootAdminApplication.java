package com.ds.proserv.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class DSBootAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSBootAdminApplication.class, args);
	}

}