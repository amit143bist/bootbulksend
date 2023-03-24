package com.ds.proserv.feign.bulksenddata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogInformation;

public interface BulkSendFailureLogService {

	@PostMapping("/docusign/bulksendfailure")
	ResponseEntity<BulkSendFailureLogDefinition> saveBulkSendFailure(
			@RequestBody BulkSendFailureLogDefinition bulkSendFailureLogDefinition);

	@PutMapping("/docusign/bulksendfailure/{failureId}")
	ResponseEntity<BulkSendFailureLogDefinition> updateBulkSendFailure(
			@RequestBody BulkSendFailureLogDefinition bulkSendFailureLogDefinition, @PathVariable String failureId);

	@GetMapping("/docusign/bulksendfailure/{failureId}")
	ResponseEntity<BulkSendFailureLogDefinition> findByBulkSendFailureId(@PathVariable String failureId);

	@GetMapping("/docusign/bulksendfailure/startdateTime/{startDateTime}/enddatetime/{endDateTime}/count")
	ResponseEntity<Long> countByBatchFailureDateTimeBetween(@PathVariable String startDateTime,
			@PathVariable String endDateTime);

	@PutMapping("/docusign/bulksendfailure/failuredatetime")
	ResponseEntity<BulkSendFailureLogInformation> findAllByBatchFailureDateTimeBetween(
			@RequestBody PageInformation pageInformation);
}