package com.ds.proserv.report.shell;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.report.shell.helper.DocuSignShellHelper;
import com.ds.proserv.report.shell.plugin.DocuSignInputReader;

@SpringBootApplication(scanBasePackages = { "com.ds.proserv" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = { "com.ds.proserv" })
public class DSProservReportShellApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(DSProservReportShellApplication.class)
				.bannerMode(Banner.Mode.CONSOLE).web(WebApplicationType.NONE).build().run(args);

		SpringApplication.exit(ctx, () -> 0);
	}

	@Bean
	public DocuSignInputReader inputReader(@Lazy LineReader lineReader, DocuSignShellHelper shellHelper) {
		return new DocuSignInputReader(lineReader, shellHelper);
	}

	@Bean
	public DocuSignShellHelper shellHelper(@Lazy Terminal terminal, @Autowired DSCacheManager dsCacheManager) {

		return new DocuSignShellHelper(terminal, dsCacheManager);
	}
}