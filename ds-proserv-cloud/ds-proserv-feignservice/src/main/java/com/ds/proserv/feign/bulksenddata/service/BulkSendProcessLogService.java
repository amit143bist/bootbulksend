package com.ds.proserv.feign.bulksenddata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogIdResult;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogInformation;

public interface BulkSendProcessLogService {

	@PostMapping("/docusign/bulksendprocesslog")
	ResponseEntity<BulkSendProcessLogDefinition> saveBulkSendProcessLog(
			@RequestBody BulkSendProcessLogDefinition bulkSendProcessLogDefinition);

	@PutMapping("/docusign/bulksendprocesslog/{batchId}")
	ResponseEntity<BulkSendProcessLogDefinition> updateBulkSendProcessLog(
			@RequestBody BulkSendProcessLogDefinition bulkSendProcessLogDefinition, @PathVariable String batchId);

	@PutMapping("/docusign/bulksendprocesslog/bulksave")
	ResponseEntity<BulkSendProcessLogInformation> bulkSaveBulkSendProcessLog(
			@RequestBody BulkSendProcessLogInformation bulkSendProcessLogInformation);

	@GetMapping("/docusign/bulksendprocesslog/{batchId}")
	ResponseEntity<BulkSendProcessLogDefinition> findById(@PathVariable String batchId);

	@GetMapping("/docusign/bulksendprocesslog/batchstatus/{batchStatus}/count")
	ResponseEntity<Long> countByBatchStatus(@PathVariable String batchStatus);

	@PutMapping("/docusign/bulksendprocesslog/batchstatuses") // batchStatus
	ResponseEntity<BulkSendProcessLogInformation> findAllByBatchStatuses(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/bulksendprocesslog/batchname/{batchName}/count") // batchName
	ResponseEntity<Long> countByBatchName(@PathVariable String batchName);

	@PutMapping("/docusign/bulksendprocesslog/batchnames")
	ResponseEntity<BulkSendProcessLogInformation> findAllByBatchNames(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/bulksendprocesslog/startdatetime/{startDateTime}/enddatetime/{endDateTime}/count")
	ResponseEntity<Long> countByBatchSubmittedDateTimeBetween(@PathVariable String startDateTime,
			@PathVariable String endDateTime);

	@PutMapping("/docusign/bulksendprocesslog/submitdatetime")
	ResponseEntity<BulkSendProcessLogInformation> findAllByBatchSubmittedDateTimeBetween(
			@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/bulksendprocesslog/startdatetime/{startDateTime}/enddatetime/{endDateTime}/batchstatus/{batchStatus}/count")
	ResponseEntity<Long> countByBatchSubmittedDateTimeBetweenAndBatchStatus(@PathVariable String startDateTime,
			@PathVariable String endDateTime, @PathVariable String batchStatus);

	@PutMapping("/docusign/bulksendprocesslog/submitdatetime/batchstatus")
	ResponseEntity<BulkSendProcessLogInformation> findAllByBatchSubmittedDateTimeBetweenAndBatchStatus(
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/bulksendprocesslog/batchids/bystatuses") // List<String> batchIds
	ResponseEntity<BulkSendProcessLogIdResult> findAllBatchIdsByBatchStatuses(
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/bulksendprocesslog/batchids") // batchId
	ResponseEntity<BulkSendProcessLogInformation> findAllByBatchIds(@RequestBody PageInformation pageInformation);

}