package com.ds.proserv.feign.authentication.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

public interface AuthenticationService {

	@PostMapping(value = "/docusign/authentication/token")
	ResponseEntity<AuthenticationResponse> requestJWTUserToken(
			@RequestBody AuthenticationRequest authenticationRequest);

	@PutMapping(value = "/docusign/authentication/token/evictcache")
	String clearCache();
}