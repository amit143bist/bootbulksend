package com.ds.proserv.shell.service;

import com.ds.proserv.common.constant.BatchType;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.shell.client.CoreConcurrentProcessLogClient;
import com.ds.proserv.shell.client.CoreProcessFailureLogClient;
import com.ds.proserv.shell.client.CoreScheduledBatchLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CoreBatchDataService {

	@Autowired
	CoreScheduledBatchLogClient coreScheduledBatchLogClient;

	@Autowired
	CoreProcessFailureLogClient coreProcessFailureLogClient;

	@Autowired
	CoreConcurrentProcessLogClient coreConcurrentProcessLogClient;

	public String checkOrCreateBatch() {

		try {
			ResponseEntity<ScheduledBatchLogResponse> scheduledBatchLogResponseEntity = coreScheduledBatchLogClient
					.findLatestBatchByBatchType(BatchType.BULKSENDENVELOPES.toString());

			ScheduledBatchLogResponse scheduledBatchLogResponse = scheduledBatchLogResponseEntity.getBody();

			if (null != scheduledBatchLogResponse.getBatchEndDateTime()) {

				log.info("Successfully found last completed batch job of batchType -> {}, last completed batchId is {}",
						BatchType.BULKSENDENVELOPES.toString(), scheduledBatchLogResponse.getBatchId());

				return createNewBatch(BatchType.BULKSENDENVELOPES.toString());

			} else {

				log.error("Another Batch running of batchType -> {} since {}", BatchType.BULKSENDENVELOPES.toString(),
						scheduledBatchLogResponse.getBatchStartDateTime());

				throw new RunningBatchException(
						"Another Batch already running for batch type " + BatchType.BULKSENDENVELOPES.toString()
								+ " since " + scheduledBatchLogResponse.getBatchStartDateTime());

			}
		} catch (ResponseStatusException exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("No Batch running of batchType -> {}", BatchType.BULKSENDENVELOPES.toString());
			if (exp.getStatus() == HttpStatus.NOT_FOUND) {

				return createNewBatch(BatchType.BULKSENDENVELOPES.toString());

			}
		}

		return null;
	}

	private String createNewBatch(String batchType) {

		ScheduledBatchLogRequest scheduledBatchLogRequest = new ScheduledBatchLogRequest();
		scheduledBatchLogRequest.setBatchType(batchType);
		scheduledBatchLogRequest.setBatchStartParameters(batchType);
		scheduledBatchLogRequest.setTotalRecords(0L);

		return coreScheduledBatchLogClient.saveBatch(scheduledBatchLogRequest).getBody().getBatchId();
	}

	public void finishNewBatch(String batchId, Long totalRecordsInBatch) {

		coreScheduledBatchLogClient.updateBatch(batchId, totalRecordsInBatch);
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId) {

		log.info("Creating New ConcurrentProcess with batchSize {} for batchId -> {}", batchSize, batchId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);

		return coreConcurrentProcessLogClient.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();
	}

	public void finishConcurrentProcess(String processId, String processStatus) {

		log.info("Finishing ConcurrentProcess for processId -> {}", processId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setProcessStatus(processStatus);

		coreConcurrentProcessLogClient.updateConcurrentProcess(concurrentProcessLogDefinition, processId).getBody();

	}

	public void createFailureProcess(String failureRecordId, String failureCode, String failureReason,
			String failureStep, String processId) {

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setFailureRecordId(failureRecordId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode);
		concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep);
		concurrentProcessFailureLogDefinition.setProcessId(processId);
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());
		coreProcessFailureLogClient.saveFailureLog(concurrentProcessFailureLogDefinition);
	}
}