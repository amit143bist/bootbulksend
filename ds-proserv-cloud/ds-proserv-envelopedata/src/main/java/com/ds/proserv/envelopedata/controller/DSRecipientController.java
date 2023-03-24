package com.ds.proserv.envelopedata.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.IterableUtil;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSRecipientPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSRecipientRepository;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.envelopedata.transformer.DSRecipientTransformer;
import com.ds.proserv.envelopedata.transformer.DSTabTransformer;
import com.ds.proserv.envelopedata.transformer.PageableTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientInformation;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.ds.proserv.feign.envelopedata.service.DSRecipientService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
@Transactional
public class DSRecipientController implements DSRecipientService {

	@Autowired
	PageableTransformer pageableTransformer;

	@Autowired
	private DSTabTransformer dsTabTransformer = null;

	@Autowired
	private DSRecipientTransformer dsRecipientTransformer = null;

	@Autowired
	private DSTabRepository dsTabRepository = null;

	@Autowired
	private DSRecipientRepository dsRecipientRepository = null;

	@Autowired
	DSRecipientPagingAndSortingRepository dsRecipientPagingAndSortingRepository = null;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientDefinition> saveRecipientData(DSRecipientDefinition dsRecipientDefinition) {

		log.info("Saving DSRecipientDefinition for recipientId -> {} and for envelopeId -> {}",
				dsRecipientDefinition.getRecipientId(), dsRecipientDefinition.getEnvelopeId());
		return Optional
				.ofNullable(dsRecipientRepository
						.save(dsRecipientTransformer.transformToDSRecipient(dsRecipientDefinition)))
				.map(dsRecipient -> {

					Assert.notNull(dsRecipient.getCreatedBy(),
							"CreatedBy cannot be null for recipientId " + dsRecipientDefinition.getRecipientId()
									+ " for envelopeId " + dsRecipientDefinition.getEnvelopeId());

					return new ResponseEntity<DSRecipientDefinition>(
							dsRecipientTransformer.transformToDSRecipientDefinition(dsRecipient), HttpStatus.CREATED);
				})
				.orElseThrow(() -> new ResourceNotSavedException(
						"DSRecipient not saved for recipientId " + dsRecipientDefinition.getRecipientId()
								+ " and for envelopeId " + dsRecipientDefinition.getEnvelopeId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientDefinition> updateRecipientData(DSRecipientDefinition dsRecipientDefinition,
			String recipientId) {

		log.info("updateRecipientData called for recipientId -> {} and for envelopeId -> {}", recipientId,
				dsRecipientDefinition.getEnvelopeId());

		return dsRecipientRepository.findById(recipientId).map(dsException -> {

			dsRecipientDefinition.setRecipientId(recipientId);
			return new ResponseEntity<DSRecipientDefinition>(
					dsRecipientTransformer.transformToDSRecipientDefinition(dsRecipientRepository
							.save(dsRecipientTransformer.transformToDSRecipient(dsRecipientDefinition))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No recipient found with recipientId# " + recipientId
				+ " for envelopeId# " + dsRecipientDefinition.getEnvelopeId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientDefinition> findRecipientByRecipientId(String recipientId) {

		return dsRecipientRepository.findById(recipientId).map(recipientData -> {

			return new ResponseEntity<DSRecipientDefinition>(
					dsRecipientTransformer.transformToDSRecipientDefinition(recipientData), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No Recipient found in findRecipientByRecipientId with recipientId# " + recipientId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientInformation> findRecipientsByEnvelopeId(String envelopeId) {

		Iterable<DSRecipient> dsRecipientIterable = dsRecipientRepository.findAllByEnvelopeId(envelopeId);

		if (IterableUtil.isNullOrEmpty(dsRecipientIterable)) {

			throw new ResourceNotFoundException("dsRecipientList is empty or null for envelopeId# " + envelopeId);
		} else {

			List<DSRecipientDefinition> dsRecipientDefinitionList = new ArrayList<DSRecipientDefinition>();
			dsRecipientIterable.forEach(dsRecipient -> {

				dsRecipientDefinitionList.add(dsRecipientTransformer.transformToDSRecipientDefinition(dsRecipient));
			});

			DSRecipientInformation dsRecipientInformation = new DSRecipientInformation();

			dsRecipientInformation.setDsRecipientDefinitions(dsRecipientDefinitionList);
			dsRecipientInformation.setTotalRecords(Long.valueOf(dsRecipientDefinitionList.size()));

			return new ResponseEntity<DSRecipientInformation>(dsRecipientInformation, HttpStatus.OK);

		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientDefinition> findRecipientTreeByRecipientId(String recipientId) {

		return fetchRecipientTree(recipientId);
	}

	private ResponseEntity<DSRecipientDefinition> fetchRecipientTree(String recipientId) {

		Optional<DSRecipient> dsRecipientOptional = dsRecipientRepository.findById(recipientId);
		if (null != dsRecipientOptional && dsRecipientOptional.isPresent()) {

			Iterable<DSTab> dsTabIterable = dsTabRepository.findAllByRecipientId(recipientId);

			if (IterableUtil.isNullOrEmpty(dsTabIterable)) {

				throw new ResourceNotFoundException("tabList is empty or null for recipientId# " + recipientId);
			} else {

				List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();
				dsTabIterable.forEach(dsTab -> {

					dsTabDefinitionList.add(dsTabTransformer.transformToDSTabDefinition(dsTab));
				});

				DSRecipientDefinition dsRecipientDefinition = dsRecipientTransformer
						.transformToDSRecipientDefinition(dsRecipientOptional.get());
				dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);

				return new ResponseEntity<DSRecipientDefinition>(dsRecipientDefinition, HttpStatus.OK);
			}
		} else {
			throw new ResourceNotFoundException(
					"No Recipient found in findRecipientTreeByRecipientId with recipientId# " + recipientId);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientInformation> findRecipientsTreeByEnvelopeId(String envelopeId) {

		Iterable<DSRecipient> dsRecipientIterable = dsRecipientRepository.findAllByEnvelopeId(envelopeId);

		if (IterableUtil.isNullOrEmpty(dsRecipientIterable)) {

			throw new ResourceNotFoundException("dsRecipientList is empty or null for envelopeId# " + envelopeId);
		} else {

			List<DSRecipientDefinition> dsRecipientDefinitionList = new ArrayList<DSRecipientDefinition>();
			dsRecipientIterable.forEach(dsRecipient -> {

				DSRecipientDefinition dsRecipientDefinition = fetchRecipientTree(dsRecipient.getRecipientId())
						.getBody();

				dsRecipientDefinitionList.add(dsRecipientDefinition);

			});

			DSRecipientInformation dsRecipientInformation = new DSRecipientInformation();
			dsRecipientInformation.setDsRecipientDefinitions(dsRecipientDefinitionList);
			dsRecipientInformation.setTotalRecords(Long.valueOf(dsRecipientDefinitionList.size()));

			return new ResponseEntity<DSRecipientInformation>(dsRecipientInformation, HttpStatus.OK);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSRecipientInformation> findRecipientsByEnvelopeIds(PageInformation pageInformation) {

		log.info("findRecipientsTreeByEnvelopeIds -> {}", pageInformation.getPageQueryParams());

		DSRecipientInformation dsRecipientInformation = new DSRecipientInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam envelopeIdsParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.ENVELOPEIDS_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> envelopeIdList = null;

			if (null == envelopeIdsParam || StringUtils.isEmpty(envelopeIdsParam.getParamValue())) {

				throw new InvalidInputException("envelopeIdsParam cannot be empty or null");
			} else {

				envelopeIdList = Stream.of(envelopeIdsParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.ENVELOPEIDS_PARAM_NAME, envelopeIdsParam);

			Slice<DSRecipient> dsRecipientSlice = dsRecipientPagingAndSortingRepository
					.findAllByEnvelopeIdIn(envelopeIdList, pageable);

			prepareResponseFromSlice(dsRecipientInformation, dsRecipientSlice);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSRecipientInformation>(dsRecipientInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(DSRecipientInformation dsRecipientInformation,
			Slice<DSRecipient> dsRecipientSlice) {

		log.info("prepareResponseFromSlice called for {}", dsRecipientSlice);

		if (null != dsRecipientSlice && !dsRecipientSlice.isEmpty() && dsRecipientSlice.hasContent()) {

			List<DSRecipientDefinition> dsRecipientDefinitionList = new ArrayList<DSRecipientDefinition>();
			dsRecipientSlice.getContent().forEach(dsRecipient -> {

				dsRecipientDefinitionList.add(dsRecipientTransformer.transformToDSRecipientDefinition(dsRecipient));
			});

			dsRecipientInformation.setCurrentPage(Long.valueOf(dsRecipientSlice.getNumber()));
			dsRecipientInformation.setNextAvailable(dsRecipientSlice.hasNext());
			dsRecipientInformation.setContentAvailable(true);
			dsRecipientInformation.setDsRecipientDefinitions(dsRecipientDefinitionList);

		} else {

			dsRecipientInformation.setContentAvailable(false);
			dsRecipientInformation.setDsRecipientDefinitions(null);
		}
	}

}