package com.ds.proserv.feign.coredata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogsInformation;

public interface CoreConcurrentProcessLogService {

	@PostMapping("/docusign/scheduledbatch/concurrentprocess")
	ResponseEntity<ConcurrentProcessLogDefinition> saveConcurrentProcess(
			@RequestBody ConcurrentProcessLogDefinition concurrentProcessLogDefinition);

	@PutMapping("/docusign/scheduledbatch/concurrentprocess/processes/{processId}")
	ResponseEntity<ConcurrentProcessLogDefinition> updateConcurrentProcess(
			@RequestBody ConcurrentProcessLogDefinition concurrentProcessLogDefinition, @PathVariable String processId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/countprocesses/batchid/{batchId}")
	ResponseEntity<Long> countPendingConcurrentProcessInBatch(@PathVariable String batchId);
	
	@GetMapping("/docusign/scheduledbatch/concurrentprocess/countprocesses/groupid/{groupId}")
	ResponseEntity<Long> countPendingConcurrentProcessInGroup(@PathVariable String groupId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/processes/{batchId}")
	ResponseEntity<ConcurrentProcessLogsInformation> findAllProcessesForBatchId(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/process/{processId}")
	ResponseEntity<ConcurrentProcessLogDefinition> findProcessByProcessId(@PathVariable String processId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/incompleteprocesses/{batchId}")
	ResponseEntity<ConcurrentProcessLogsInformation> findAllInCompleteProcessesForBatchId(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/completeprocesses/{batchId}")
	ResponseEntity<ConcurrentProcessLogsInformation> findAllCompleteProcessesForBatchId(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/totalrecords/group/{groupId}")
	ResponseEntity<Long> countTotalRecordsInGroup(@PathVariable String groupId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/allparentgroup/{batchId}")
	ResponseEntity<ConcurrentProcessLogsInformation> findAllParentGroups(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/successparentgroup/{batchId}")
	ResponseEntity<ConcurrentProcessLogsInformation> findAllSuccessParentGroups(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/concurrentprocess/totalrecords/{batchId}")
	ResponseEntity<Long> countTotalRecordsInBatch(@PathVariable String batchId);

}