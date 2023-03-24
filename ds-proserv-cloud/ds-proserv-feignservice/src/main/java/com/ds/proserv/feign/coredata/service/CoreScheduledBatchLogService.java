package com.ds.proserv.feign.coredata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogsInformation;

public interface CoreScheduledBatchLogService {

	@PostMapping("/docusign/scheduledbatch")
	ResponseEntity<ScheduledBatchLogResponse> saveBatch(@RequestBody ScheduledBatchLogRequest scheduledBatchLogRequest);

	@PutMapping("/docusign/scheduledbatch/{batchId}")
	ResponseEntity<ScheduledBatchLogResponse> updateBatch(@PathVariable String batchId);

	@PutMapping("/docusign/scheduledbatch/{batchId}/totalrecordsinbatch/{totalRecordsInBatch}")
	ResponseEntity<ScheduledBatchLogResponse> updateBatch(@PathVariable String batchId,
			@PathVariable Long totalRecordsInBatch);

	@GetMapping("/docusign/scheduledbatch/batchtype/{batchType}")
	ResponseEntity<ScheduledBatchLogResponse> findInCompleteBatch(@PathVariable String batchType);

	@GetMapping("/docusign/scheduledbatch/incompletebatches/batchtype/{batchType}")
	ResponseEntity<ScheduledBatchLogsInformation> findAllInCompleteBatches(@PathVariable String batchType);

	@GetMapping("/docusign/scheduledbatch/completebatches/batchtype/{batchType}")
	ResponseEntity<ScheduledBatchLogsInformation> findAllCompleteBatches(@PathVariable String batchType);

	@GetMapping("/docusign/scheduledbatch/batches/batchtype/{batchType}")
	ResponseEntity<ScheduledBatchLogsInformation> findAllBatchesByBatchType(@PathVariable String batchType);

	@GetMapping("/docusign/scheduledbatch/batches/batchtype/{batchType}/fromdate/{fromDate}/todate/{toDate}")
	ResponseEntity<ScheduledBatchLogsInformation> findAllByBatchTypeAndBatchStartDateTimeBetween(
			@PathVariable String batchType, @PathVariable String fromDate, @PathVariable String toDate);

	@GetMapping("/docusign/scheduledbatch/batches")
	ResponseEntity<ScheduledBatchLogsInformation> findAllBatches();

	@GetMapping("/docusign/scheduledbatch/latestbatch/batchid/{batchId}")
	ResponseEntity<ScheduledBatchLogResponse> findBatchByBatchId(@PathVariable String batchId);

	@GetMapping("/docusign/scheduledbatch/latestbatch/batchtype/{batchType}")
	ResponseEntity<ScheduledBatchLogResponse> findLatestBatchByBatchType(@PathVariable String batchType);

}