package com.ds.proserv.feign.envelopeapi.service;

import com.ds.proserv.feign.envelopeapi.domain.GenericEnvelopeMessageDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface DSEnvelopeApiService {

	@PostMapping("/docusign/api/envelope")
	ResponseEntity<String> createEnvelope(
			@RequestBody GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition);

	@PutMapping("/docusign/api/update/envelope/{envelopeId}")
	ResponseEntity<String> updateEnvelope(@PathVariable String envelopeId, @RequestBody String  updateEnvelopeRequest);

}