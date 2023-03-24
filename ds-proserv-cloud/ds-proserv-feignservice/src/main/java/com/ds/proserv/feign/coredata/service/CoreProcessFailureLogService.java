package com.ds.proserv.feign.coredata.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogsInformation;

public interface CoreProcessFailureLogService {

	@PostMapping("/docusign/scheduledbatch/concurrentprocessfailure")
	ResponseEntity<ConcurrentProcessFailureLogDefinition> saveFailureLog(
			@RequestBody ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition);

	@PutMapping("/docusign/scheduledbatch/concurrentprocessfailure/processes/{processFailureId}")
	ResponseEntity<ConcurrentProcessFailureLogDefinition> updateFailureLog(
			@RequestBody ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition,
			@PathVariable String processFailureId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocessfailure")
	ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLog();

	@GetMapping("/docusign/scheduledbatch/concurrentprocessfailure/processes/{processId}")
	ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForConcurrentProcessId(
			@PathVariable String processId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/{failureRecordId}")
	ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForFailureRecordId(
			@PathVariable String failureRecordId);

	@PutMapping("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords")
	ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailuresByProcessIds(
			@RequestBody List<String> processIds);

	@PutMapping("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/processids/count")
	ResponseEntity<Long> countProcessFailuresByProcessIds(@RequestBody List<String> processIds);

	@GetMapping("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/count")
	ResponseEntity<Long> countProcessFailures();

	@PutMapping("/docusign/scheduledbatch/concurrentprocessfailure/failurerecords/batchids/count")
	ResponseEntity<Long> countProcessFailuresByBatchIds(@RequestBody List<String> batchIds);

	@GetMapping("/docusign/scheduledbatch/concurrentprocessfailure/batch/{batchId}")
	ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForBatchId(
			@PathVariable String batchId);

}