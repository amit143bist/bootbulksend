package com.ds.proserv.feign.notificationdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialRequest;

public interface ClientCredentialService {

	@PostMapping("/docusign/credentials/vendor/save")
	ResponseEntity<ClientCredentialDefinition> saveClientCredential(
			@RequestBody ClientCredentialDefinition clientCredentialDefinition);

	@PutMapping("/docusign/credentials/vendor/find/bycredrequest")
	ResponseEntity<ClientCredentialDefinition> findByClientCredentialRequest(
			@RequestBody ClientCredentialRequest clientCredentialRequest);

	@PutMapping("/docusign/credentials/vendor/update/accesstoken/byid/{credentialId}")
	ResponseEntity<String> updateClientCredentialAccessToken(@PathVariable String credentialId,
			@RequestBody ClientCredentialRequest clientCredentialRequest);
}