package com.ds.proserv.envelopedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.transformer.DSRecipientTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSRecipientProcessor extends AbstractDSProcessor {

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private DSRecipientTransformer dsRecipientTransformer;

	@Override
	public long callSequence() {

		return 5;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.RECIPIENT;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.RECIPIENT.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getRecipientToBeSavedList()
				&& !dsEnvelopeData.getRecipientToBeSavedList().isEmpty()) {

			return true;
		} else {

			log.warn("No recipients identified in bulkUpdateSaveEnvelopeData for processId -> {}",
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

			try {
				List<DSRecipient> savedDSRecipientList = null;
				if (isReadRecipientByEnvIdsSPEnabled()) {

					savedDSRecipientList = dsDataHelperService.findAllRecipientsForAllEnvelopeIdsAfterSentDateTime(
							processId, dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());
				} else {

					savedDSRecipientList = dsDataHelperService.findAllRecipientsForAllEnvelopeIds(processId,
							dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());
				}

				AtomicReference<List<DSRecipient>> savedDSRecipientListAtomicReference = new AtomicReference<List<DSRecipient>>();
				savedDSRecipientListAtomicReference.set(savedDSRecipientList);

				dsEnvelopeData.getRecipientToBeSavedList()
						.forEach(throwingConsumerWrapper(toBeSavedRecipient -> processDSRecipientData(dsEnvelopeData,
								toBeSavedRecipient, savedDSRecipientListAtomicReference.get())));

				log.info("Total Recipients to be saved/updated for processId -> {} and envelopes -> {} is {}",
						processId, dsEnvelopeData.getPrepareToSaveDSRecipientList().size());
			} catch (Exception exp) {

				log.error("Some exception {} occurred in preparing dsrecipient for processId -> {} and envelopes -> {}",
						exp, processId, dsEnvelopeData.getEnvelopeIdsToSave());
				exp.printStackTrace();
				throw exp;
			}
			return asyncStatus;
		}, processorAsyncExecutor);
	}

	private boolean isReadRecipientByEnvIdsSPEnabled() {

		/*
		 * String enableRecipientEnvIdsBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * ENVDATA_RECIPIENTS_SELECTBYENVIDS_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableRecipientEnvIdsBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableRecipientEnvIdsBySP); }
		 */

		return true;
	}

	private void processDSRecipientData(DSEnvelopeData dsEnvelopeData, DSRecipientDefinition toBeSavedRecipient,
			List<DSRecipient> savedDSRecipientList) {

		String processId = dsEnvelopeData.getProcessId();
		DSRecipient filterSavedDSRecipient = savedDSRecipientList.stream().filter(savedDSRecipient -> {

			if (null != savedDSRecipient && null != toBeSavedRecipient
					&& savedDSRecipient.getRecipientId().equalsIgnoreCase(toBeSavedRecipient.getRecipientId())) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterSavedDSRecipient && !StringUtils.isEmpty(filterSavedDSRecipient.getRecipientId())) {

			log.debug("RecipientId -> {} will be updated for processId -> {} and envelopeId -> {}",
					toBeSavedRecipient.getRecipientId(), processId, toBeSavedRecipient.getEnvelopeId());

			DSRecipient exitingDsRecipient = dsRecipientTransformer.transformToDSRecipientUpdate(toBeSavedRecipient,
					filterSavedDSRecipient);
			dsEnvelopeData.getPrepareToSaveDSRecipientList().add(exitingDsRecipient);
		} else {

			log.debug("RecipientId -> {} will be saved for processId -> {} and envelopeId -> {}",
					toBeSavedRecipient.getRecipientId(), processId, toBeSavedRecipient.getEnvelopeId());

			DSRecipient newDsRecipient = dsRecipientTransformer.transformToDSRecipient(toBeSavedRecipient);
			dsEnvelopeData.getPrepareToSaveDSRecipientList().add(newDsRecipient);
		}
	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSRecipientList()
				&& !dsEnvelopeData.getPrepareToSaveDSRecipientList().isEmpty()) {

			return true;
		} else {

			log.error("No recipients identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());
			return false;
		}
	}

	@Override
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSRecipientList triggered for processId -> {} and envelopeIds -> {}",
				dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

		return CompletableFuture.runAsync(() -> {

			dsDataHelperService.saveDSRecipientForEnvelopeIds(dsEnvelopeData);
		}, processorAsyncExecutor);
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSRecipient> dsRecipients = dsEnvelopeSavedData.getDsRecipients();
		if (null != dsRecipients && !dsRecipients.isEmpty()) {

			List<DSRecipient> uniqueDSRecipients = dsRecipients.stream().filter(value -> value != null)
					.collect(Collectors.collectingAndThen(
							Collectors.toCollection(
									() -> new TreeSet<>(Comparator.comparing(DSRecipient::getRecipientId))),
							ArrayList::new));

			dsEnvelopeSavedData.setUniqueDSRecipients(uniqueDSRecipients);
		}

	}

}