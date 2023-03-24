package com.ds.proserv.feign.envelopedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabInformation;

public interface DSTabService {

	@PostMapping("/docusign/envelopedata/tab")
	ResponseEntity<DSTabDefinition> saveTabData(@RequestBody DSTabDefinition dsTabDefinition);

	@PutMapping("/docusign/envelopedata/tab/{id}")
	ResponseEntity<DSTabDefinition> updateTabData(@RequestBody DSTabDefinition dsTabDefinition,
			@PathVariable String id);

	@GetMapping("/docusign/envelopedata/tab/{id}")
	ResponseEntity<DSTabDefinition> findTabByTabId(@PathVariable String id);

	@GetMapping("/docusign/envelopedata/tab/tabLabel/{tabLabel}/envelope/{envelopeId}")
	ResponseEntity<DSTabDefinition> findTabByTabLabelAndEnvelopeId(@PathVariable String tabLabel,
			@PathVariable String envelopeId);

	@GetMapping("/docusign/envelopedata/tabs/{recipientId}")
	ResponseEntity<DSTabInformation> findTabsByRecipientId(@PathVariable String recipientId);

	@GetMapping("/docusign/envelopedata/tabs/envelope/{envelopeId}")
	ResponseEntity<DSTabInformation> findTabsByEnvelopeId(@PathVariable String envelopeId);

	@PutMapping("/docusign/envelopedata/tabs")
	ResponseEntity<DSTabInformation> findTabsByEnvelopeIds(@RequestBody PageInformation pageInformation);
	
	@PutMapping("/docusign/envelopedata/tabs/tablabels")
	ResponseEntity<DSTabInformation> findTabsByEnvelopeIdsAndTabLabels(@RequestBody PageInformation pageInformation);
}