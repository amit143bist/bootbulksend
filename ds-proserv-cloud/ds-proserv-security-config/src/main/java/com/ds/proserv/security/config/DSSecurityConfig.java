package com.ds.proserv.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

import lombok.extern.slf4j.Slf4j;

@Profile({ "!unittest" })
@Slf4j
public class DSSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	protected void configureGlobal(AuthenticationManagerBuilder auth, @Autowired DSCacheManager dsCacheManager)
			throws Exception {

		String basicAuthUserName = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_APP_USERNAME);
		String basicAuthUserPassword = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_APP_PASSWORD);

		log.info("Configure (auth) method called with username {} and password {}", basicAuthUserName,
				basicAuthUserPassword);
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		auth.inMemoryAuthentication().withUser(basicAuthUserName).password(basicAuthUserPassword).roles("USER");

	}

	// Secure the endpoints with HTTP Basic authentication
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		log.info("configure(http) method called");
		http
				// HTTP Basic authentication
				.httpBasic().and().authorizeRequests().antMatchers(HttpMethod.GET, "/docusign/**").hasRole("USER")
				.antMatchers(HttpMethod.POST, "/docusign/**").hasRole("USER")
				.antMatchers(HttpMethod.PUT, "/docusign/**").hasRole("USER")
				.antMatchers(HttpMethod.PATCH, "/docusign/**").hasRole("USER")
				.antMatchers(HttpMethod.DELETE, "/docusign/**").hasRole("USER").and().csrf().disable().formLogin()
				.disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

}