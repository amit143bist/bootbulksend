package com.ds.proserv.coredata.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.assertj.core.util.IterableUtil;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.coredata.model.CoreScheduledBatchLog;
import com.ds.proserv.coredata.repository.CoreScheduledBatchLogRepository;
import com.ds.proserv.coredata.transformer.CoreScheduledBatchLogTransformer;
import com.ds.proserv.coredata.validator.BatchLogValidator;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogsInformation;
import com.ds.proserv.feign.coredata.service.CoreScheduledBatchLogService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Transactional
@Slf4j
public class CoreScheduledBatchLogController implements CoreScheduledBatchLogService {

	@Lazy
	@Autowired
	private CoreScheduledBatchLogRepository batchLogRepository;

	@Autowired
	private CoreScheduledBatchLogTransformer coreScheduledBatchLogTransformer;

	@Autowired
	private BatchLogValidator batchLogValidator;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> saveBatch(ScheduledBatchLogRequest scheduledBatchLogRequest) {

		batchLogValidator.validateSaveData(scheduledBatchLogRequest);
		CoreScheduledBatchLog coreScheduledBatchLog = coreScheduledBatchLogTransformer
				.transformToCoreScheduledBatchLog(scheduledBatchLogRequest);

		log.debug("scheduledBatchLogRequest transformed to coreScheduledBatchLog for batchType -> {}",
				scheduledBatchLogRequest.getBatchType());
		return Optional.ofNullable(batchLogRepository.save(coreScheduledBatchLog)).map(savedCoreScheduledBatchLog -> {

			Assert.notNull(savedCoreScheduledBatchLog.getBatchId(),
					"BatchId cannot be null for batchType " + scheduledBatchLogRequest.getBatchType());

			return new ResponseEntity<ScheduledBatchLogResponse>(
					coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(savedCoreScheduledBatchLog),
					HttpStatus.CREATED);
		}).orElseThrow(
				() -> new ResourceNotSavedException("Batch not saved for " + scheduledBatchLogRequest.getBatchType()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> updateBatch(String batchId) {

		log.debug("updateBatch called for batchId -> {}", batchId);

		return batchLogRepository.findById(batchId).map(scheduledBatch -> {

			if (null != scheduledBatch.getBatchEndDateTime()) {

				log.warn("BatchEndDateTime should be null for batchId#-> {}", batchId);

				return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
						.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)),
						HttpStatus.ALREADY_REPORTED);

			}

			scheduledBatch.setBatchEndDateTime(LocalDateTime.now());

			return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
					.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Batch found with batchId# " + batchId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> updateBatch(String batchId, Long totalRecordsInBatch) {

		log.info("updateBatch called for batchId -> {} and totalRecordsInBatch -> {}", batchId, totalRecordsInBatch);

		return batchLogRepository.findById(batchId).map(scheduledBatch -> {

			if (null != scheduledBatch.getBatchEndDateTime()) {

				log.warn("BatchEndDateTime should be null for batchId#-> {}", batchId);

				/*
				 * return new
				 * ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
				 * .transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)
				 * ), HttpStatus.ALREADY_REPORTED);
				 */

			}

			if (null != totalRecordsInBatch) {

				scheduledBatch.setTotalRecords(totalRecordsInBatch);
			}
			scheduledBatch.setBatchEndDateTime(LocalDateTime.now());

			return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
					.transformToScheduledBatchLogResponse(batchLogRepository.save(scheduledBatch)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Batch found with batchId# " + batchId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> findInCompleteBatch(String batchType) {

		log.debug("findInCompleteBatch called for batchType -> {}", batchType);

		List<CoreScheduledBatchLog> scheduledBatchLogList = batchLogRepository
				.findAllByBatchTypeAndBatchEndDateTimeIsNull(batchType);

		if (null != scheduledBatchLogList && !scheduledBatchLogList.isEmpty()) {

			if (scheduledBatchLogList.size() > 1) {

				throw new RunningBatchException("More than one batch is already running for batchType " + batchType);
			}

			return new ResponseEntity<ScheduledBatchLogResponse>(
					coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(scheduledBatchLogList.get(0)),
					HttpStatus.OK);

		} else {

			ScheduledBatchLogResponse scheduledBatchLogResponse = new ScheduledBatchLogResponse();
			return new ResponseEntity<ScheduledBatchLogResponse>(scheduledBatchLogResponse, HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> findBatchByBatchId(String batchId) {

		log.debug("findBatchByBatchId called for batchId -> {}", batchId);

		return new ResponseEntity<ScheduledBatchLogResponse>(coreScheduledBatchLogTransformer
				.transformToScheduledBatchLogResponse(batchLogRepository.findById(batchId).map(scheduledBatch -> {

					return scheduledBatch;
				}).orElseThrow(() -> new ResourceNotFoundException("No Batch available for batchId# " + batchId))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogResponse> findLatestBatchByBatchType(String batchType) {

		log.debug("findLatestBatchByBatchType called for batchType -> {}", batchType);

		return new ResponseEntity<ScheduledBatchLogResponse>(
				coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(batchLogRepository
						.findTopByBatchTypeOrderByBatchStartDateTimeDesc(batchType).map(scheduledBatch -> {

							return scheduledBatch;
						}).orElseThrow(
								() -> new ResourceNotFoundException("No Batch running with batch type " + batchType))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogsInformation> findAllInCompleteBatches(String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchTypeAndBatchEndDateTimeIsNull(batchType), batchType);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogsInformation> findAllBatchesByBatchType(String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchType(batchType), batchType);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogsInformation> findAllByBatchTypeAndBatchStartDateTimeBetween(
			String batchType, String fromDate, String toDate) {

		return prepareResponse(
				batchLogRepository.findAllByBatchTypeAndBatchStartDateTimeBetween(batchType,
						DateTimeUtil.convertToLocalDateTime(fromDate), DateTimeUtil.convertToLocalDateTime(toDate)),
				batchType);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogsInformation> findAllBatches() {

		return prepareResponse(batchLogRepository.findAll(), null);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ScheduledBatchLogsInformation> findAllCompleteBatches(String batchType) {

		return prepareResponse(batchLogRepository.findAllByBatchTypeAndBatchEndDateTimeIsNotNull(batchType), batchType);
	}

	private ResponseEntity<ScheduledBatchLogsInformation> prepareResponse(
			Iterable<CoreScheduledBatchLog> coreScheduledBatchLogIterable, String batchType) {

		log.debug("prepareResponse is called for batchType -> {}", batchType);
		if (IterableUtil.isNullOrEmpty(coreScheduledBatchLogIterable)) {

			if (null != batchType) {
				throw new ResourceNotFoundException("No Batch exists for batchType " + batchType);
			} else {
				throw new ResourceNotFoundException("No Batch exists");
			}
		}

		List<ScheduledBatchLogResponse> scheduledBatchLogResponseList = new ArrayList<ScheduledBatchLogResponse>();
		coreScheduledBatchLogIterable.forEach(coreScheduledBatchLog -> {

			scheduledBatchLogResponseList
					.add(coreScheduledBatchLogTransformer.transformToScheduledBatchLogResponse(coreScheduledBatchLog));
		});

		ScheduledBatchLogsInformation scheduledBatchLogsInformation = new ScheduledBatchLogsInformation();
		scheduledBatchLogsInformation.setScheduledBatchLogResponses(scheduledBatchLogResponseList);

		if (null != scheduledBatchLogResponseList && !scheduledBatchLogResponseList.isEmpty()) {

			scheduledBatchLogsInformation.setTotalBatchesCount(Long.valueOf(scheduledBatchLogResponseList.size()));
		}

		return new ResponseEntity<ScheduledBatchLogsInformation>(scheduledBatchLogsInformation, HttpStatus.OK);
	}

}