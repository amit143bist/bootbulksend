package com.ds.proserv.feign.envelopedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientInformation;

public interface DSRecipientService {

	@PostMapping("/docusign/envelopedata/recipient")
	ResponseEntity<DSRecipientDefinition> saveRecipientData(@RequestBody DSRecipientDefinition dsRecipientDefinition);

	@PutMapping("/docusign/envelopedata/recipient/{recipientId}")
	ResponseEntity<DSRecipientDefinition> updateRecipientData(@RequestBody DSRecipientDefinition dsRecipientDefinition,
			@PathVariable String recipientId);

	@GetMapping("/docusign/envelopedata/recipient/{recipientId}")
	ResponseEntity<DSRecipientDefinition> findRecipientByRecipientId(@PathVariable String recipientId);

	@GetMapping("/docusign/envelopedata/recipients/{envelopeId}")
	ResponseEntity<DSRecipientInformation> findRecipientsByEnvelopeId(@PathVariable String envelopeId);

	@GetMapping("/docusign/envelopedata/recipienttree/{recipientId}")
	ResponseEntity<DSRecipientDefinition> findRecipientTreeByRecipientId(@PathVariable String recipientId);

	@GetMapping("/docusign/envelopedata/recipientstree/envelope/{envelopeId}")
	ResponseEntity<DSRecipientInformation> findRecipientsTreeByEnvelopeId(@PathVariable String envelopeId);

	@PutMapping("/docusign/envelopedata/recipients")
	ResponseEntity<DSRecipientInformation> findRecipientsByEnvelopeIds(@RequestBody PageInformation pageInformation);
}