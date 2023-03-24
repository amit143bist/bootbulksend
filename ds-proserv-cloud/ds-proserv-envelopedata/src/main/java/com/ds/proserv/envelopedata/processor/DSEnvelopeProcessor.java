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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.transformer.DSEnvelopeTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSEnvelopeProcessor extends AbstractDSProcessor {

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private DSEnvelopeTransformer dsEnvelopeTransformer;

	@Override
	public long callSequence() {

		return 6;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.ENVELOPE;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.ENVELOPE.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getEnvelopeToBeSavedList() && !dsEnvelopeData.getEnvelopeToBeSavedList().isEmpty()) {

			return true;
		} else {

			log.warn("No envelopes identified in bulkUpdateSaveEnvelopeData for processId -> {}",
					dsEnvelopeData.getProcessId());
			return false;
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public CompletableFuture<String> compareAndPrepareData(DSEnvelopeData dsEnvelopeData) {

		String processId = dsEnvelopeData.getProcessId();

		log.info("Preparing all to be saved/updated envelopeList for processId -> {} and envelopeIds -> {}", processId,
				dsEnvelopeData.getEnvelopeIdsToSave());

		if (isReadEnvelopeByEnvIdsSPEnabled()) {

			return CompletableFuture.supplyAsync((Supplier<String>) () -> {

				try {
					List<DSEnvelope> savedDSEnvelopeList = dsDataHelperService
							.findAllEnvelopesByEnvelopeIdsBySP(processId, dsEnvelopeData.getEnvelopeIdsToSave());

					if (null != savedDSEnvelopeList && !savedDSEnvelopeList.isEmpty()) {

						dsEnvelopeData
								.setSingleEnvelopeSavedTimeGenerated(savedDSEnvelopeList.get(0).getTimeGenerated());
					} else {

						log.info("savedDSEnvelopeList is empty or null for processId -> {} and envelopeIds -> {}",
								processId, dsEnvelopeData.getEnvelopeIdsToSave());
					}

					log.info(
							"processDSEnvelopeData all to be saved/updated envelopeList for processId -> {} and envelopeIds -> {}",
							processId, dsEnvelopeData.getEnvelopeIdsToSave());
					dsEnvelopeData.getEnvelopeToBeSavedList()
							.forEach(throwingConsumerWrapper(toBeSavedEnvelope -> processDSEnvelopeData(dsEnvelopeData,
									toBeSavedEnvelope, savedDSEnvelopeList)));

				} catch (Exception exp) {

					log.error("Exception occurred in fetching envelopedata for processId -> {} and envelopeIds -> {}",
							processId, dsEnvelopeData.getEnvelopeIdsToSave());

					exp.printStackTrace();
					throw exp;
				}
				return AppConstants.SUCCESS_VALUE;

			}, processorAsyncExecutor);

		} else {

			return dsDataHelperService.findAllEnvelopesByEnvelopeIds(processId, dsEnvelopeData.getEnvelopeIdsToSave())
					.thenApplyAsync(savedDSEnvelopeList -> {

						dsEnvelopeData.getEnvelopeToBeSavedList().forEach(
								throwingConsumerWrapper(toBeSavedEnvelope -> processDSEnvelopeData(dsEnvelopeData,
										toBeSavedEnvelope, savedDSEnvelopeList)));

						return AppConstants.SUCCESS_VALUE;

					}, processorAsyncExecutor);
		}

	}

	private boolean isReadEnvelopeByEnvIdsSPEnabled() {

		/*
		 * String enableEnvelopeEnvIdsBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * ENVDATA_SELECTBYENVIDS_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableEnvelopeEnvIdsBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableEnvelopeEnvIdsBySP); }
		 */

		return true;
	}

	private void processDSEnvelopeData(DSEnvelopeData dsEnvelopeData, DSEnvelopeDefinition toBeSavedEnvelope,
			List<DSEnvelope> savedDSEnvelopeList) {

		String processId = dsEnvelopeData.getProcessId();
		DSEnvelope filterSavedDSEnvelope = savedDSEnvelopeList.stream().filter(savedDSEnvelope -> {

			if (null != savedDSEnvelope && null != toBeSavedEnvelope
					&& savedDSEnvelope.getEnvelopeId().equalsIgnoreCase(toBeSavedEnvelope.getEnvelopeId())) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterSavedDSEnvelope && !StringUtils.isEmpty(filterSavedDSEnvelope.getEnvelopeId())) {

			log.debug("EnvelopeId -> {} will be updated for processId -> {}", toBeSavedEnvelope.getEnvelopeId(),
					processId);

			DSEnvelope updatedDSEnvelope = dsEnvelopeTransformer.transformToDSEnvelopeUpdate(toBeSavedEnvelope,
					filterSavedDSEnvelope);
			dsEnvelopeData.getPrepareToSaveDSEnvelopeList().add(updatedDSEnvelope);
		} else {

			log.debug("EnvelopeId -> {} will be saved for processId -> {}", toBeSavedEnvelope.getEnvelopeId(),
					processId);
			DSEnvelope newDsEnvelope = dsEnvelopeTransformer.transformToDSEnvelope(toBeSavedEnvelope);
			dsEnvelopeData.getPrepareToSaveDSEnvelopeList().add(newDsEnvelope);
		}
	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSEnvelopeList()
				&& !dsEnvelopeData.getPrepareToSaveDSEnvelopeList().isEmpty()) {

			return true;
		} else {

			log.error("No envelopes identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());

			return false;
		}
	}

	@Override
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		return CompletableFuture.runAsync(() -> {

			log.info("SaveDSEnvelopeList triggered for processId -> {} and envelopeIds -> {} with size -> {}",
					dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave(),
					dsEnvelopeData.getPrepareToSaveDSEnvelopeList().size());

			dsDataHelperService.saveDSEnvelopeForEnvelopeIds(dsEnvelopeData);
		}, processorAsyncExecutor);
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		log.warn("NOT NEEDED AS OF TODAY");
	}

}