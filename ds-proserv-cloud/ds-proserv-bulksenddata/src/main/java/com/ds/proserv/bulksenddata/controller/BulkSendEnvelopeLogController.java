package com.ds.proserv.bulksenddata.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.bulksenddata.model.BulkSendEnvelopeLog;
import com.ds.proserv.bulksenddata.repository.BulkSendEnvelopeLogPagingAndSortingRepositoryRepository;
import com.ds.proserv.bulksenddata.transformer.BulkSendEnvelopeLogTransformer;
import com.ds.proserv.bulksenddata.transformer.PageableTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogInformation;
import com.ds.proserv.feign.bulksenddata.service.BulkSendEnvelopeLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendEnvelopeLogController implements BulkSendEnvelopeLogService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private BulkSendEnvelopeLogTransformer bulkSendEnvelopeLogTransformer;

	@Autowired
	private BulkSendEnvelopeLogPagingAndSortingRepositoryRepository bulkSendEnvelopeLogPagingAndSortingRepositoryRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendEnvelopeLogDefinition> saveBulkSendEnvelopeLog(
			BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition) {

		log.info("Saving BulkSendEnvelopeLog for bulkBatchId -> {}", bulkSendEnvelopeLogDefinition.getBulkBatchId());
		return Optional
				.ofNullable(bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.save(
						bulkSendEnvelopeLogTransformer.transformToBulkSendEnvelopeLog(bulkSendEnvelopeLogDefinition)))
				.map(savedEnvelopeLogData -> {

					Assert.notNull(savedEnvelopeLogData.getId(),
							"Id cannot be null for bulkBatchId " + bulkSendEnvelopeLogDefinition.getBulkBatchId());

					return new ResponseEntity<BulkSendEnvelopeLogDefinition>(bulkSendEnvelopeLogTransformer
							.transformToBulkSendEnvelopeLogDefinition(savedEnvelopeLogData), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException(
						"BulkSendEnvelopeLog not saved for " + bulkSendEnvelopeLogDefinition.getBulkBatchId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendEnvelopeLogInformation> bulkSaveBulkSendEnvelopeLog(
			BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation) {

		List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList = bulkSendEnvelopeLogInformation
				.getBulkSendEnvelopeLogDefinitions();

		BulkSendEnvelopeLogInformation savedBulkSendEnvelopeLogInformation = new BulkSendEnvelopeLogInformation();
		if (null != bulkSendEnvelopeLogDefinitionList && !bulkSendEnvelopeLogDefinitionList.isEmpty()) {

			try {

				String json = objectMapper.writeValueAsString(bulkSendEnvelopeLogDefinitionList);

				String result = bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.insert(json);

				if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

					throw new ResourceNotSavedException(
							"BulkSendEnvelopeLogData not saved for " + bulkSendEnvelopeLogInformation);
				}
			} catch (JsonProcessingException exp) {
				exp.printStackTrace();
				throw new ResourceNotSavedException(
						"BulkSendEnvelopeLogData not saved for " + bulkSendEnvelopeLogInformation);
			}

			savedBulkSendEnvelopeLogInformation.setBulkSendEnvelopeLogDefinitions(bulkSendEnvelopeLogDefinitionList);
			savedBulkSendEnvelopeLogInformation.setTotalRecords(Long.valueOf(bulkSendEnvelopeLogDefinitionList.size()));

			return new ResponseEntity<BulkSendEnvelopeLogInformation>(savedBulkSendEnvelopeLogInformation,
					HttpStatus.CREATED);
		} else {

			return new ResponseEntity<BulkSendEnvelopeLogInformation>(savedBulkSendEnvelopeLogInformation,
					HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendEnvelopeLogDefinition> updateBulkSendEnvelopeLog(
			BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition, String id) {

		log.info("Updating BulkSendEnvelopeLog for id -> {}", id);
		return bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.findById(id).map(savedBulkEnvelopeLogData -> {

			bulkSendEnvelopeLogDefinition.setId(id);
			return new ResponseEntity<BulkSendEnvelopeLogDefinition>(bulkSendEnvelopeLogTransformer
					.transformToBulkSendEnvelopeLogDefinition(bulkSendEnvelopeLogPagingAndSortingRepositoryRepository
							.save(bulkSendEnvelopeLogTransformer.transformToBulkSendEnvelopeLogUpdate(
									bulkSendEnvelopeLogDefinition, savedBulkEnvelopeLogData))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendEnvelopeLog found for id# " + id));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendEnvelopeLogDefinition> findById(String id) {

		log.info("Finding BulkSendEnvelopeLog for id -> {}", id);
		return bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.findById(id).map(savedEnvelopeLogData -> {

			return new ResponseEntity<BulkSendEnvelopeLogDefinition>(
					bulkSendEnvelopeLogTransformer.transformToBulkSendEnvelopeLogDefinition(savedEnvelopeLogData),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendEnvelopeLog found for id# " + id));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendEnvelopeLogDefinition> findByEnvelopeId(String envelopeId) {

		log.info("Finding BulkSendEnvelopeLog for envelopeId -> {}", envelopeId);
		return bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.findByEnvelopeId(envelopeId)
				.map(savedEnvelopeLogData -> {

					return new ResponseEntity<BulkSendEnvelopeLogDefinition>(bulkSendEnvelopeLogTransformer
							.transformToBulkSendEnvelopeLogDefinition(savedEnvelopeLogData), HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No BulkSendEnvelopeLog found for envelopeId# " + envelopeId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBulkBatchId(String bulkBatchId) {

		log.info("Counting BulkSendEnvelopeLog for bulkBatchId -> {}", bulkBatchId);

		return new ResponseEntity<Long>(
				bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.countByBulkBatchId(bulkBatchId), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBulkBatchIdIn(PageInformation pageInformation) {

		log.info("Counting BulkSendEnvelopeLog for bulkBatchIds -> {}", pageInformation.getPageQueryParams());

		Long count = null;
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> bulkBatchList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BULKBATCHIDS_PARAM_NAME);

			count = bulkSendEnvelopeLogPagingAndSortingRepositoryRepository.countByBulkBatchIdIn(bulkBatchList);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<Long>(count, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendEnvelopeLogInformation> findAllByBulkBatchIdIn(PageInformation pageInformation) {

		log.info("FindAll BulkSendEnvelopeLog for bulkBatchIds -> {}", pageInformation.getPageQueryParams());

		BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation = new BulkSendEnvelopeLogInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> bulkBatchList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BULKBATCHIDS_PARAM_NAME);

			Slice<BulkSendEnvelopeLog> bulkSendEnvelopeLogSlice = bulkSendEnvelopeLogPagingAndSortingRepositoryRepository
					.findAllByBulkBatchIdIn(bulkBatchList, pageable);

			prepareResponseFromSlice(bulkSendEnvelopeLogInformation, bulkSendEnvelopeLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendEnvelopeLogInformation>(bulkSendEnvelopeLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendEnvelopeLogInformation> findAllByEnvelopeIdIn(PageInformation pageInformation) {

		log.info("FindAll BulkSendEnvelopeLog for envelopeIds -> {}", pageInformation.getPageQueryParams());

		BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation = new BulkSendEnvelopeLogInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> envelopeIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.ENVELOPEIDS_PARAM_NAME);

			Slice<BulkSendEnvelopeLog> bulkSendEnvelopeLogSlice = bulkSendEnvelopeLogPagingAndSortingRepositoryRepository
					.findAllByBulkBatchIdIn(envelopeIdList, pageable);

			prepareResponseFromSlice(bulkSendEnvelopeLogInformation, bulkSendEnvelopeLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendEnvelopeLogInformation>(bulkSendEnvelopeLogInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(BulkSendEnvelopeLogInformation bulkSendEnvelopeLogInformation,
			Slice<BulkSendEnvelopeLog> bulkSendEnvelopeLogSlice) {

		log.info("Prepared Response from slice -> {}", bulkSendEnvelopeLogSlice);

		if (null != bulkSendEnvelopeLogSlice && !bulkSendEnvelopeLogSlice.isEmpty()
				&& bulkSendEnvelopeLogSlice.hasContent()) {

			List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitionList = new ArrayList<BulkSendEnvelopeLogDefinition>();
			bulkSendEnvelopeLogSlice.getContent().forEach(bulkSendEnvelopeLog -> {

				bulkSendEnvelopeLogDefinitionList.add(
						bulkSendEnvelopeLogTransformer.transformToBulkSendEnvelopeLogDefinition(bulkSendEnvelopeLog));
			});

			bulkSendEnvelopeLogInformation.setCurrentPage(Long.valueOf(bulkSendEnvelopeLogSlice.getNumber()));
			bulkSendEnvelopeLogInformation.setNextAvailable(bulkSendEnvelopeLogSlice.hasNext());
			bulkSendEnvelopeLogInformation.setContentAvailable(true);
			bulkSendEnvelopeLogInformation.setBulkSendEnvelopeLogDefinitions(bulkSendEnvelopeLogDefinitionList);

		} else {

			bulkSendEnvelopeLogInformation.setContentAvailable(false);
			bulkSendEnvelopeLogInformation.setBulkSendEnvelopeLogDefinitions(null);
		}
	}

}