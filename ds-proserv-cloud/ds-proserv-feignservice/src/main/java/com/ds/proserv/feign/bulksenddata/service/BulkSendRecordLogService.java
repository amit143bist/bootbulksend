package com.ds.proserv.feign.bulksenddata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogInformation;

public interface BulkSendRecordLogService {

	@PostMapping("/docusign/bulksendrecordlog/bulksave")
	ResponseEntity<BulkSendRecordLogInformation> bulkSaveBulkSendRecordLogs(
			@RequestBody BulkSendRecordLogInformation bulkSendRecordLogInformation);

	@PutMapping("/docusign/bulksendrecordlog/bulkfindall")
	ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogs(
			@RequestBody BulkSendRecordLogInformation bulkSendRecordLogInformation);

	@GetMapping("/docusign/bulksendrecordlog/startdatetime/{startDateTime}/enddatetime/{endDateTime}")
	ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogsByDateRange(
			@PathVariable String startDateTime, @PathVariable String endDateTime);

	@PutMapping("/docusign/bulksendrecordlog/bulkfindall/recordtype/{recordType}")
	ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogs(@PathVariable String recordType,
			@RequestBody PageInformation pageInformation);
}