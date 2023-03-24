package com.ds.proserv.feign.envelopedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;

public interface DSEnvelopeService {

	/*
	 * @PutMapping("/docusign/envelopedata/envelope/saveupdate/bulkv2")
	 * CompletableFuture<ResponseEntity<DSEnvelopeInformation>>
	 * bulkUpdateSaveEnvelopeDataV2(
	 * 
	 * @RequestBody DSEnvelopeInformation dsEnvelopeInformation);
	 * 
	 * @GetMapping("/docusign/envelopedata/envelope/{envelopeId}")
	 * CompletableFuture<ResponseEntity<DSEnvelopeDefinition>>
	 * findEnvelopeByEnvelopeId(@PathVariable String envelopeId);
	 */
	
	@PutMapping("/docusign/envelopedata/envelope/saveupdate/bulkv2")
	ResponseEntity<DSEnvelopeInformation> bulkUpdateSaveEnvelopeDataV2(
			@RequestBody DSEnvelopeInformation dsEnvelopeInformation);

	@GetMapping("/docusign/envelopedata/envelope/{envelopeId}")
	ResponseEntity<DSEnvelopeDefinition> findEnvelopeByEnvelopeId(@PathVariable String envelopeId);

	@PutMapping("/docusign/envelopedata/envelopes/list")
	ResponseEntity<DSEnvelopeInformation> findEnvelopesByEnvelopeIds(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/envelopestree/list/envelopeids")
	ResponseEntity<DSEnvelopeInformation> findEnvelopesTreeListByEnvelopeIds(
			@RequestBody PageInformation pageInformation);

}