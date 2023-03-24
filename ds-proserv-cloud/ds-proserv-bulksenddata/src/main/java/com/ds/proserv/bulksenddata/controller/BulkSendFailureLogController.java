package com.ds.proserv.bulksenddata.controller;

import java.time.LocalDateTime;
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

import com.ds.proserv.bulksenddata.model.BulkSendFailureLog;
import com.ds.proserv.bulksenddata.repository.BulkSendFailureLogPagingAndSortingRepository;
import com.ds.proserv.bulksenddata.transformer.BulkSendFailureLogTransformer;
import com.ds.proserv.bulksenddata.transformer.PageableTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogInformation;
import com.ds.proserv.feign.bulksenddata.service.BulkSendFailureLogService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendFailureLogController implements BulkSendFailureLogService {

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private BulkSendFailureLogTransformer bulkSendFailureLogTransformer;

	@Autowired
	private BulkSendFailureLogPagingAndSortingRepository bulkSendFailureLogPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendFailureLogDefinition> saveBulkSendFailure(
			BulkSendFailureLogDefinition bulkSendFailureLogDefinition) {

		log.info("Saving BulkSendFailureLog for applicationIds -> {}",
				bulkSendFailureLogDefinition.getApplicationIds());
		return Optional
				.ofNullable(bulkSendFailureLogPagingAndSortingRepository.save(
						bulkSendFailureLogTransformer.transformToBulkSendFailureLog(bulkSendFailureLogDefinition)))
				.map(savedBulkSendFailureLogData -> {

					Assert.notNull(savedBulkSendFailureLogData.getId(),
							"Id cannot be null for applicationIds " + savedBulkSendFailureLogData.getApplicationIds());

					return new ResponseEntity<BulkSendFailureLogDefinition>(bulkSendFailureLogTransformer
							.transformToBulkSendFailureLogDefinition(savedBulkSendFailureLogData), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException("BulkSendFailureLog not saved for applicationIds -> "
						+ bulkSendFailureLogDefinition.getApplicationIds()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendFailureLogDefinition> updateBulkSendFailure(
			BulkSendFailureLogDefinition bulkSendFailureLogDefinition, String failureId) {

		log.info("Updating BulkSendFailureLog for id -> {}", failureId);
		return bulkSendFailureLogPagingAndSortingRepository.findById(failureId).map(savedBulkSendFailureLogData -> {

			bulkSendFailureLogDefinition.setId(failureId);
			return new ResponseEntity<BulkSendFailureLogDefinition>(
					bulkSendFailureLogTransformer
							.transformToBulkSendFailureLogDefinition(bulkSendFailureLogPagingAndSortingRepository
									.save(bulkSendFailureLogTransformer.transformToBulkSendFailureLogUpdate(
											bulkSendFailureLogDefinition, savedBulkSendFailureLogData))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendFailureLog found for id# " + failureId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendFailureLogDefinition> findByBulkSendFailureId(String failureId) {

		log.info("Finding BulkSendFailureLog for id -> {}", failureId);
		return bulkSendFailureLogPagingAndSortingRepository.findById(failureId).map(savedBulkSendFailureLogData -> {

			return new ResponseEntity<BulkSendFailureLogDefinition>(
					bulkSendFailureLogTransformer.transformToBulkSendFailureLogDefinition(savedBulkSendFailureLogData),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendFailureLog found for id# " + failureId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBatchFailureDateTimeBetween(String startDateTime, String endDateTime) {

		log.info(
				"Counting BulkSendFailureLog in countByBatchFailureDatetimeBetween for startDateTime -> {} and endDateTime -> {}",
				startDateTime, endDateTime);

		return new ResponseEntity<Long>(bulkSendFailureLogPagingAndSortingRepository.countByBatchFailureDateTimeBetween(
				LocalDateTime.parse(startDateTime), LocalDateTime.parse(endDateTime)), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendFailureLogInformation> findAllByBatchFailureDateTimeBetween(
			PageInformation pageInformation) {

		log.info("FindAll BulkSendFailureLog in findAllByBatchFailureDatetimeBetween -> {}",
				pageInformation.getPageQueryParams());

		BulkSendFailureLogInformation bulkSendFailureLogInformation = new BulkSendFailureLogInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam fromDateTimeParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.FROMDATETIME_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			if (null == fromDateTimeParam) {

				throw new InvalidInputException("fromDateTimeParam param cannot be null");
			}

			PageQueryParam toDateTimeParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.TODATETIME_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			if (null == toDateTimeParam) {

				throw new InvalidInputException("toDateTimeParam param cannot be null");
			}

			LocalDateTime fromLocalDateTime = LocalDateTime.parse(fromDateTimeParam.getParamValue());

			LocalDateTime toLocalDateTime = LocalDateTime.parse(toDateTimeParam.getParamValue());

			Slice<BulkSendFailureLog> bulkSendEnvelopeLogSlice = bulkSendFailureLogPagingAndSortingRepository
					.findAllByBatchFailureDateTimeBetween(fromLocalDateTime, toLocalDateTime, pageable);

			prepareResponseFromSlice(bulkSendFailureLogInformation, bulkSendEnvelopeLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendFailureLogInformation>(bulkSendFailureLogInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(BulkSendFailureLogInformation bulkSendFailureLogInformation,
			Slice<BulkSendFailureLog> bulkSendFailureLogSlice) {

		log.info("prepareResponseFromSlice called for {}", bulkSendFailureLogSlice);

		if (null != bulkSendFailureLogSlice && !bulkSendFailureLogSlice.isEmpty()
				&& bulkSendFailureLogSlice.hasContent()) {

			List<BulkSendFailureLogDefinition> bulkSendFailureLogDefinitionList = new ArrayList<BulkSendFailureLogDefinition>();
			bulkSendFailureLogSlice.getContent().forEach(bulkSendFailureLogDefinition -> {

				bulkSendFailureLogDefinitionList.add(bulkSendFailureLogTransformer
						.transformToBulkSendFailureLogDefinition(bulkSendFailureLogDefinition));
			});

			bulkSendFailureLogInformation.setCurrentPage(Long.valueOf(bulkSendFailureLogSlice.getNumber()));
			bulkSendFailureLogInformation.setNextAvailable(bulkSendFailureLogSlice.hasNext());
			bulkSendFailureLogInformation.setContentAvailable(true);
			bulkSendFailureLogInformation.setBulkSendFailureLogDefinitions(bulkSendFailureLogDefinitionList);

		} else {

			bulkSendFailureLogInformation.setContentAvailable(false);
			bulkSendFailureLogInformation.setBulkSendFailureLogDefinitions(null);
		}
	}

}