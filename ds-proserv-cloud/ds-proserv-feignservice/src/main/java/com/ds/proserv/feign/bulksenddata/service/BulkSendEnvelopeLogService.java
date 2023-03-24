package com.ds.proserv.feign.bulksenddata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogInformation;

public interface BulkSendEnvelopeLogService {

	@PostMapping("/docusign/bulksendenvelopelog")
	ResponseEntity<BulkSendEnvelopeLogDefinition> saveBulkSendEnvelopeLog(
			@RequestBody BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition);

	@PostMapping("/docusign/bulksendenvelopelog/bulksave")
	ResponseEntity<BulkSendEnvelopeLogInformation> bulkSaveBulkSendEnvelopeLog(
			@RequestBody BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation);

	@PutMapping("/docusign/bulksendenvelopelog/{id}")
	ResponseEntity<BulkSendEnvelopeLogDefinition> updateBulkSendEnvelopeLog(
			@RequestBody BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition, @PathVariable String id);

	@GetMapping("/docusign/bulksendenvelopelog/{id}")
	ResponseEntity<BulkSendEnvelopeLogDefinition> findById(@PathVariable String id);

	@PutMapping("/docusign/bulksendenvelopelog/envelope/{envelopeId}")
	ResponseEntity<BulkSendEnvelopeLogDefinition> findByEnvelopeId(@PathVariable String envelopeId);

	@GetMapping("/docusign/bulksendenvelopelog/bulkbatch/{bulkBatchId}/count")
	ResponseEntity<Long> countByBulkBatchId(@PathVariable String bulkBatchId);

	@PutMapping("/docusign/bulksendenvelopelog/bulkbatches/count")
	ResponseEntity<Long> countByBulkBatchIdIn(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/bulksendenvelopelog/bulkbatches/allbatchids")
	ResponseEntity<BulkSendEnvelopeLogInformation> findAllByBulkBatchIdIn(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/bulksendenvelopelog/bulkbatches/allenvelopeids")
	ResponseEntity<BulkSendEnvelopeLogInformation> findAllByEnvelopeIdIn(@RequestBody PageInformation pageInformation);
}