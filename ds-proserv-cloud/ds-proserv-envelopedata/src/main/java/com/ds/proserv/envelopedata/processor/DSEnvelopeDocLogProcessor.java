package com.ds.proserv.envelopedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.repository.DSEnvelopeDocLogPagingAndSortingRepository;
import com.ds.proserv.envelopedata.transformer.DSEnvelopeDocLogTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDocLogDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSEnvelopeDocLogProcessor extends AbstractDSProcessor {

	@Autowired
	private DSEnvelopeDocLogTransformer dsEnvelopeDocLogTransformer;

	@Autowired
	private DSEnvelopeDocLogPagingAndSortingRepository dsEnvelopeDocLogPagingAndSortingRepository;

	@Override
	public long callSequence() {

		return 3;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.ENVELOPEDOCLOG;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.ENVELOPEDOCLOG.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getEnvelopeDocLogToBeSavedList()
				&& !dsEnvelopeData.getEnvelopeDocLogToBeSavedList().isEmpty()) {

			return true;
		} else {

			log.warn("No envelopeDocLog identified in bulkUpdateSaveEnvelopeData for processId -> {}",
					dsEnvelopeData.getProcessId());
			return false;
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public CompletableFuture<String> compareAndPrepareData(DSEnvelopeData dsEnvelopeData) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;
			String processId = dsEnvelopeData.getProcessId();

			log.info("Preparing all to be saved/updated envelopeDocLog for processId -> {} and envelopeIds -> {}",
					processId, dsEnvelopeData.getEnvelopeIdsToSave());

			dsEnvelopeData.getEnvelopeDocLogToBeSavedList().forEach(throwingConsumerWrapper(
					toBeSavedEnvelopeDocLog -> processDSEnvelopeDocLogData(dsEnvelopeData, toBeSavedEnvelopeDocLog)));

			log.info("Total envelopeDocLog to be saved/updated for processId -> {} and envelopeIds -> {} is {}",
					processId, dsEnvelopeData.getEnvelopeIdsToSave(),
					dsEnvelopeData.getPrepareToSaveDSEnvelopeDocLogList().size());

			return asyncStatus;
		}, processorAsyncExecutor);
	}

	private void processDSEnvelopeDocLogData(DSEnvelopeData dsEnvelopeData,
			DSEnvelopeDocLogDefinition toBeSavedEnvelopeDocLog) {

		log.debug("DSEnvelopeDocLog with timegenerated -> {} and EnvelopeId -> {} will be saved for processId -> {}",
				toBeSavedEnvelopeDocLog.getTimeGenerated(), toBeSavedEnvelopeDocLog.getEnvelopeId(),
				dsEnvelopeData.getProcessId());

		dsEnvelopeData.getPrepareToSaveDSEnvelopeDocLogList()
				.add(dsEnvelopeDocLogTransformer.transformToDSEnvelopeDocLog(toBeSavedEnvelopeDocLog));
	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSEnvelopeDocLogList()
				&& !dsEnvelopeData.getPrepareToSaveDSEnvelopeDocLogList().isEmpty()) {

			return true;
		} else {

			log.error("No envelopeDocLog identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());

			return false;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSEnvelopeDocLogList triggered for processId -> {}", dsEnvelopeData.getProcessId());

		return CompletableFuture.runAsync(() -> {

			dsEnvelopeDocLogPagingAndSortingRepository.saveAll(dsEnvelopeData.getPrepareToSaveDSEnvelopeDocLogList());
		}, processorAsyncExecutor);
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		log.warn("NOT NEEDED AS OF TODAY");
	}

}