package com.ds.proserv.shell;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableDiscoveryClient // This will register this service with Eureka
@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableSwagger2
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSShellApplication {

	@Value("${ds.async.queuecapacity}")
	private int queueCapacity;

	@Value("${ds.async.maxpoolsize}")
	private int maxPoolSize;

	@Value("${ds.async.corepoolsize}")
	private int corePoolSize;

	@Value("${ds.async.executornameprefix}")
	private String executorNamePrefix;

	public static void main(String[] args) {
		SpringApplication.run(DSShellApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public  HttpHeaders httpHeaders() {
		return new HttpHeaders();
	}

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}

	@Bean(name = "recordTaskExecutor")
	public TaskExecutor recordTaskExecutor() {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix(executorNamePrefix);
		return executor;
	}

}