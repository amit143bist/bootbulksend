package com.ds.proserv.envelopedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.repository.DSRecipientAuthPagingAndSortingRepository;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.transformer.DSRecipientAuthTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientAuthDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSRecipientAuthProcessor extends AbstractDSProcessor {

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private DSRecipientAuthTransformer dsRecipientAuthTransformer;

	@Autowired
	private DSRecipientAuthPagingAndSortingRepository dsRecipientAuthPagingAndSortingRepository;

	@Override
	public long callSequence() {

		return 2;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.RECIPIENTAUTH;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.RECIPIENTAUTH.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getRecipientAuthToBeSavedList()
				&& !dsEnvelopeData.getRecipientAuthToBeSavedList().isEmpty()) {

			return true;

		} else {

			log.warn("No recipientAuthdata identified in bulkUpdateSaveEnvelopeData for processId -> {}",
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

			log.info("Preparing all to be saved/updated recipientAuth for processId -> {} and envelopeIds -> {}",
					processId, dsEnvelopeData.getEnvelopeIdsToSave());

			List<DSRecipientAuth> savedDSRecipientAuthList = dsDataHelperService
					.findAllDSRecipientAuthByEnvelopeIds(processId, dsEnvelopeData.getEnvelopeIdsToSave());

			dsEnvelopeData.getRecipientAuthToBeSavedList().forEach(
					throwingConsumerWrapper(toBeSavedRecipientAuth -> processDSRecipientAuthData(dsEnvelopeData,
							toBeSavedRecipientAuth, savedDSRecipientAuthList)));

			log.info("Total DSRecipientAuth to be saved/updated for processId -> {} and envelopeIds -> {} is {}",
					processId, dsEnvelopeData.getEnvelopeIdsToSave(),
					dsEnvelopeData.getPrepareToSaveDSRecipientAuthList().size());

			return asyncStatus;
		}, processorAsyncExecutor);
	}

	private void processDSRecipientAuthData(DSEnvelopeData dsEnvelopeData,
			DSRecipientAuthDefinition toBeSavedRecipientAuth, List<DSRecipientAuth> savedDSRecipientAuthList) {

		String processId = dsEnvelopeData.getProcessId();
		DSRecipientAuth filterSavedDSRecipientAuth = savedDSRecipientAuthList.stream().filter(savedDSRecipientAuth -> {

			if (null != savedDSRecipientAuth && null != toBeSavedRecipientAuth
					&& savedDSRecipientAuth.getEnvelopeId().equalsIgnoreCase(toBeSavedRecipientAuth.getEnvelopeId())
					&& savedDSRecipientAuth.getRecipientId().equalsIgnoreCase(toBeSavedRecipientAuth.getRecipientId())
					&& savedDSRecipientAuth.getType().equalsIgnoreCase(toBeSavedRecipientAuth.getType())
					&& savedDSRecipientAuth.getEventDateTime()
							.isEqual(LocalDateTime.parse(toBeSavedRecipientAuth.getEventDateTime()))) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null == filterSavedDSRecipientAuth || StringUtils.isEmpty(filterSavedDSRecipientAuth.getId())) {

			log.debug("DSRecipientAuth with eventDateTime -> {} for EnvelopeId -> {} will be saved for processId -> {}",
					toBeSavedRecipientAuth.getEventDateTime(), toBeSavedRecipientAuth.getEnvelopeId(), processId);
			dsEnvelopeData.getPrepareToSaveDSRecipientAuthList()
					.add(dsRecipientAuthTransformer.transformToDSRecipientAuth(toBeSavedRecipientAuth));
		}
	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSRecipientAuthList()
				&& !dsEnvelopeData.getPrepareToSaveDSRecipientAuthList().isEmpty()) {

			return true;
		} else {

			log.error("No recipientAuthdata identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());

			return false;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSRecipientAuthList triggered for processId -> {}", dsEnvelopeData.getProcessId());

		return CompletableFuture.runAsync(() -> {

			dsRecipientAuthPagingAndSortingRepository.saveAll(dsEnvelopeData.getPrepareToSaveDSRecipientAuthList());
		}, processorAsyncExecutor);
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSRecipientAuth> dsRecipientAuths = dsEnvelopeSavedData.getDsRecipientAuths();
		if (null != dsRecipientAuths && !dsRecipientAuths.isEmpty()) {
			List<DSRecipientAuth> uniqueDSRecipientAuths = dsRecipientAuths.stream().filter(value -> value != null)
					.collect(Collectors.collectingAndThen(
							Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DSRecipientAuth::getId))),
							ArrayList::new));
			dsEnvelopeSavedData.setUniqueDSRecipientAuths(uniqueDSRecipientAuths);
		}
	}

}