package com.ds.proserv.connect;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;
import com.ds.proserv.security.config.ConnectSecurityConfig;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableWebSecurity
@EnableScheduling
@EnableSwagger2
@EnableFeignClients(basePackages = { "com.ds.proserv" })
@Slf4j
public class DSConnectListenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DSConnectListenerApplication.class, args);
	}

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}

	@Bean
	public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter2() {
		return new ConnectSecurityConfig();
	}

	@Bean
	public Pbkdf2PasswordEncoder pbkdf2PasswordEncoder() {
		return new Pbkdf2PasswordEncoder();
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