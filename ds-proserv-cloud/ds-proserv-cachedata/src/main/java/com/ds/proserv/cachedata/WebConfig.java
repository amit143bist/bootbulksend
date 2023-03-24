package com.ds.proserv.cachedata;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {

		registry.addMapping("/**").allowedOrigins("*")
				.allowedMethods("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
				.allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
				.exposedHeaders("Origin", "Content-Type", "Accept", "Authorization").allowCredentials(true)
				.maxAge(3600);
	}

}