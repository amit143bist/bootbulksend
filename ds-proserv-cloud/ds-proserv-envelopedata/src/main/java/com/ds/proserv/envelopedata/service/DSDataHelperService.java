package com.ds.proserv.envelopedata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.envelopedata.AsyncConfiguration;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.EnvelopeSPRequest;
import com.ds.proserv.envelopedata.domain.RecipientSPRequest;
import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.model.DSException;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSCustomFieldPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSEnvelopeRepository;
import com.ds.proserv.envelopedata.repository.DSExceptionRepository;
import com.ds.proserv.envelopedata.repository.DSRecipientAuthPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSRecipientRepository;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSDataHelperService extends AbstractDataService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSTabRepository dsTabRepository;

	@Autowired
	private DSEnvelopeRepository dsEnvelopeRepository;

	@Autowired
	private DSRecipientRepository dsRecipientRepository;

	@Autowired
	private DSExceptionRepository dsExceptionRepository;

	@Autowired
	private DSCustomFieldPagingAndSortingRepository dsCustomFieldPagingAndSortingRepository;

	@Autowired
	private DSRecipientAuthPagingAndSortingRepository dsRecipientAuthPagingAndSortingRepository;

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public Iterable<DSException> findAllDSExceptionsForAllEnvelopeIdsById(String processId,
			List<DSExceptionDefinition> dsExceptionDefinitionList) {

		log.info("Finding all exceptions for processId -> {}", processId);
		List<String> dsExceptionIds = dsExceptionDefinitionList.stream().map(DSExceptionDefinition::getId)
				.collect(Collectors.toList());

		Iterable<DSException> dsExceptionIterable = dsExceptionRepository.findAllById(dsExceptionIds);

		return dsExceptionIterable;
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSTab> findAllTabsForAllEnvelopeIds(String processId, List<String> envelopeIdsToSave,
			LocalDateTime leastSentDateTime) {

		log.info("Finding all tabs for processId -> {} and all envelopeIds -> {}", processId, envelopeIdsToSave);
		return dsTabRepository.findAllByEnvelopeIdInAndCreatedDateTimeAfter(envelopeIdsToSave, leastSentDateTime);
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSTab> getAllTabsByEnvelopeIdsAfterSentDateTime(String processId, List<String> envelopeIdsToSave,
			LocalDateTime leastSentDateTime) {

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
		envelopeIdsToSave.forEach(envelopeId -> {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(envelopeId);

			processSPDefinitionList.add(processSPDefinition);
		});

		List<DSTab> savedDSTabList = null;
		try {
			String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

			log.info("Finding all tabs for processId -> {} and all envelopeIds -> {}", processId, envelopeIdsToSave);
			savedDSTabList = dsTabRepository.getAllTabsByEnvelopeIdsAfterSentDateTime(spJSON, leastSentDateTime);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return savedDSTabList;
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSRecipient> findAllRecipientsForAllEnvelopeIdsAfterSentDateTime(String processId,
			List<String> envelopeIdsToSave, LocalDateTime leastSentDateTime) {

		log.info("Finding all recipient for processId -> {} and all envelopeIds -> {}", processId, envelopeIdsToSave);

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
		envelopeIdsToSave.forEach(envelopeId -> {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(envelopeId);

			processSPDefinitionList.add(processSPDefinition);
		});

		List<DSRecipient> savedDSRecipientList = null;
		try {
			String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

			log.info("Finding all dsRecipient for processId -> {} and all envelopeIds -> {}", processId,
					envelopeIdsToSave);
			savedDSRecipientList = dsRecipientRepository.getAllRecipientsByEnvelopeIdsAfterSentDateTime(spJSON,
					leastSentDateTime);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return savedDSRecipientList;
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSRecipient> findAllRecipientsForAllEnvelopeIds(String processId, List<String> envelopeIdsToSave,
			LocalDateTime leastSentDateTime) {

		log.info("Finding all recipient for processId -> {} and all envelopeIds -> {}", processId, envelopeIdsToSave);
		return dsRecipientRepository.findAllByEnvelopeIdInAndCreatedDateTimeAfter(envelopeIdsToSave, leastSentDateTime);
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	@Async(AsyncConfiguration.TASK_EXECUTOR_PROCESSOR)
	public CompletableFuture<List<DSEnvelope>> findAllEnvelopesByEnvelopeIds(String processId,
			List<String> envelopeIdsToSave) {

		log.info("Finding all envelopes for processId -> {} and all envelopeIds -> {}", processId, envelopeIdsToSave);

		return CompletableFuture.supplyAsync((Supplier<List<DSEnvelope>>) () -> {

			Iterable<DSEnvelope> dsEnvelopeIterable = dsEnvelopeRepository.findAllById(envelopeIdsToSave);

			List<DSEnvelope> savedDSEnvelopeList = StreamSupport.stream(dsEnvelopeIterable.spliterator(), false)
					.collect(Collectors.toList());

			return savedDSEnvelopeList;
		});
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSEnvelope> findAllEnvelopesByEnvelopeIdsBySP(String processId, List<String> envelopeIdsToSave) {

		log.info("Finding all envelopes via SP for processId -> {} and all envelopeIds -> {}", processId,
				envelopeIdsToSave);

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
		envelopeIdsToSave.forEach(envelopeId -> {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(envelopeId);

			processSPDefinitionList.add(processSPDefinition);
		});

		try {

			String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

			log.info("Calling SP to find all envelopes for processId -> {} and all envelopeIds -> {}", processId,
					envelopeIdsToSave);

			List<DSEnvelope> envelopeList = dsEnvelopeRepository.getAllEnvelopesByEnvelopeIds(spJSON).join();

			if (null != envelopeList && !envelopeList.isEmpty()) {

				return envelopeList;
			}
			return new ArrayList<DSEnvelope>();

		} catch (JsonProcessingException exp) {
			exp.printStackTrace();
			throw new JSONConversionException(exp.getMessage());
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSCustomField> findAllDSCustomFieldsForAllEnvelopeIdsAfterSentDateTime(String processId,
			List<String> envelopeIdsToSave, LocalDateTime leastSentDateTime) {

		log.info("Finding all dscustomfields for processId -> {} and all envelopeIds -> {}", processId,
				envelopeIdsToSave);

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
		envelopeIdsToSave.forEach(envelopeId -> {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(envelopeId);

			processSPDefinitionList.add(processSPDefinition);
		});

		List<DSCustomField> savedDSCustomFieldList = null;
		try {
			String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

			log.info("Finding all dsCustomFields for processId -> {} and all envelopeIds -> {}", processId,
					envelopeIdsToSave);
			savedDSCustomFieldList = dsCustomFieldPagingAndSortingRepository
					.getAllCustomFieldsByEnvelopeIdsAfterSentDateTime(spJSON, leastSentDateTime);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return savedDSCustomFieldList;
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSCustomField> findAllDSCustomFieldsByEnvelopeIds(String processId, List<String> envelopeIdsToSave,
			LocalDateTime leastSentDateTime) {

		log.info("Finding all customFields for processId -> {} and all envelopeIds -> {}", processId,
				envelopeIdsToSave);
		return dsCustomFieldPagingAndSortingRepository.findAllByEnvelopeIdInAndCreatedDateTimeAfter(envelopeIdsToSave,
				leastSentDateTime);
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public List<DSRecipientAuth> findAllDSRecipientAuthByEnvelopeIds(String processId, List<String> envelopeIdsToSave) {

		log.info("Finding all recipientAuth for processId -> {} and all envelopeIds -> {}", processId,
				envelopeIdsToSave);
		Iterable<DSRecipientAuth> dsRecipientAuthIterable = dsRecipientAuthPagingAndSortingRepository
				.findAllByEnvelopeIdIn(envelopeIdsToSave);

		List<DSRecipientAuth> savedDSRecipientAuthList = StreamSupport
				.stream(dsRecipientAuthIterable.spliterator(), false).collect(Collectors.toList());

		return savedDSRecipientAuthList;
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 5000, multiplier = 2), maxAttempts = 5)
	public void saveDSRecipientForEnvelopeIds(DSEnvelopeData dsEnvelopeData) {

		log.info("saveDSRecipientForEnvelopeIds triggered for processId -> {} and envelopeIds -> {}",
				dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

		RecipientSPRequest recipientSPRequest = new RecipientSPRequest();
		recipientSPRequest.setDsRecipients(dsEnvelopeData.getPrepareToSaveDSRecipientList());

		try {

			String result = dsRecipientRepository.insertUpdate(objectMapper.writeValueAsString(recipientSPRequest));

			if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

				log.error("DSRecipient data not inserted/updated in processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

				throw new ResourceNotSavedException(
						"DSRecipient data not inserted/updated in processId -> " + dsEnvelopeData.getProcessId()
								+ " and envelopeIds -> " + dsEnvelopeData.getEnvelopeIdsToSave());
			}

		} catch (JsonProcessingException e) {

			e.printStackTrace();

			log.error(
					"Exception {} happened in calling sproc_dsrecipient_insert_update in processId -> {} and envelopeIds -> {}",
					e, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

			throw new ResourceNotSavedException("Exception " + e, e);
		}
	}

	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 5000, multiplier = 2), maxAttempts = 5)
	public void saveDSEnvelopeForEnvelopeIds(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSEnvelopeList triggered for processId -> {} and envelopeIds -> {} with size -> {}",
				dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave(),
				dsEnvelopeData.getPrepareToSaveDSEnvelopeList().size());

		EnvelopeSPRequest envelopeSPRequest = new EnvelopeSPRequest();
		envelopeSPRequest.setDsEnvelopes(dsEnvelopeData.getPrepareToSaveDSEnvelopeList());

		try {

			String result = dsEnvelopeRepository.insertUpdate(objectMapper.writeValueAsString(envelopeSPRequest));

			if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

				log.error("DSEnvelope data not inserted/updated in processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

				throw new ResourceNotSavedException(
						"DSEnvelope data not inserted/updated in processId -> " + dsEnvelopeData.getProcessId()
								+ " and envelopeIds -> " + dsEnvelopeData.getEnvelopeIdsToSave());
			}

		} catch (JsonProcessingException e) {

			e.printStackTrace();

			log.error(
					"Exception {} happened in calling sproc_dsenvelope_insert_update in processId -> {} and envelopeIds -> {}",
					e, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

			throw new ResourceNotSavedException("Exception " + e, e);
		}
	}
}