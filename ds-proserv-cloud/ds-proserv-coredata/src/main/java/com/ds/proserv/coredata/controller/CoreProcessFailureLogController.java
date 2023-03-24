package com.ds.proserv.coredata.controller;

import java.math.BigInteger;
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

import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.coredata.model.CoreProcessFailureLog;
import com.ds.proserv.coredata.repository.CoreProcessFailureLogRepository;
import com.ds.proserv.coredata.transformer.CoreProcessFailureLogTransformer;
import com.ds.proserv.coredata.validator.FailureLogValidator;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogsInformation;
import com.ds.proserv.feign.coredata.service.CoreProcessFailureLogService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Transactional
@Slf4j
public class CoreProcessFailureLogController implements CoreProcessFailureLogService {

	@Lazy
	@Autowired
	private CoreProcessFailureLogRepository coreProcessFailureLogRepository;

	@Autowired
	private CoreProcessFailureLogTransformer coreProcessFailureLogTransformer;

	@Autowired
	private FailureLogValidator failureLogValidator;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogDefinition> saveFailureLog(
			ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest) {

		failureLogValidator.validateSaveData(concurrentProcessFailureLogRequest);
		CoreProcessFailureLog coreProcessFailureLog = coreProcessFailureLogTransformer
				.transformToCoreProcessFailureLog(concurrentProcessFailureLogRequest);

		log.debug("concurrentProcessFailureLogRequest transformed to coreProcessFailureLog for failureRecordId -> {}",
				concurrentProcessFailureLogRequest.getFailureRecordId());
		return Optional.ofNullable(coreProcessFailureLogRepository.save(coreProcessFailureLog)).map(failureLog -> {

			Assert.notNull(failureLog.getProcessFailureId(), "ProcessFailureId cannot be null for failureRecordId# "
					+ concurrentProcessFailureLogRequest.getFailureRecordId());

			return new ResponseEntity<ConcurrentProcessFailureLogDefinition>(
					coreProcessFailureLogTransformer.transformToConcurrentProcessFailureLogResponse(failureLog),
					HttpStatus.CREATED);
		}).orElseThrow(() -> new ResourceNotSavedException(
				"Failure Log not saved for " + concurrentProcessFailureLogRequest.getFailureRecordId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogDefinition> updateFailureLog(
			ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest, String processFailureId) {

		log.debug("updateFailureLog called for processFailureId -> {}", processFailureId);

		return coreProcessFailureLogRepository.findById(processFailureId).map(failureLog -> {

			coreProcessFailureLogTransformer.updateFailureLogData(concurrentProcessFailureLogRequest, failureLog);

			CoreProcessFailureLog savedCoreProcessFailureLog = coreProcessFailureLogRepository.save(failureLog);
			return new ResponseEntity<ConcurrentProcessFailureLogDefinition>(coreProcessFailureLogTransformer
					.transformToConcurrentProcessFailureLogResponse(savedCoreProcessFailureLog), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No ProcessFailure found with processFailureId# " + processFailureId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLog() {

		log.debug("listAllProcessFailureLog called");

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByRetryStatusOrRetryStatusIsNull(RetryStatus.F.toString());

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException("CoreProcessFailureLogList is empty or null");
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForConcurrentProcessId(
			String processId) {

		log.debug("listAllProcessFailureLogForConcurrentProcessId called for processId -> {}", processId);

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByProcessIdAndRetryStatusOrProcessIdAndRetryStatusIsNull(processId, RetryStatus.F.toString(),
						processId);

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException(
					"CoreProcessFailureLogList is empty or null for processId# " + processId);
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForFailureRecordId(
			String failureRecordId) {

		log.debug("listAllProcessFailureLogForFailureRecordId called for failureRecordId -> {}", failureRecordId);

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByFailureRecordIdAndRetryStatusOrFailureRecordIdAndRetryStatusIsNull(failureRecordId,
						RetryStatus.F.toString(), failureRecordId);

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException(
					"CoreProcessFailureLogList is empty or null for failurerecordId# " + failureRecordId);
		}

		return prepareResponse(coreProcessFailureLogList);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailuresByProcessIds(
			List<String> processIds) {

		return prepareResponse(
				coreProcessFailureLogRepository.findAllByProcessIdInAndRetryStatusOrProcessIdInAndRetryStatusIsNull(
						processIds, RetryStatus.F.toString(), processIds));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countProcessFailuresByProcessIds(List<String> processIds) {

		log.debug("countProcessFailuresByProcessIds called");

		return Optional.ofNullable(
				coreProcessFailureLogRepository.countByProcessIdInAndRetryStatusOrProcessIdInAndRetryStatusIsNull(
						processIds, RetryStatus.F.toString(), processIds))
				.map(failureCount -> {

					Assert.state(failureCount > -1, "ProcessCount should be greater than -1, check the processIds ");

					return new ResponseEntity<Long>(failureCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned"));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countProcessFailures() {

		log.debug("countProcessFailures called");

		return Optional
				.ofNullable(
						coreProcessFailureLogRepository.countByRetryStatusOrRetryStatusIsNull(RetryStatus.F.toString()))
				.map(failureCount -> {

					Assert.state(failureCount > -1, "ProcessCount should be greater than -1 ");

					return new ResponseEntity<Long>(failureCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned"));
	}

	private ResponseEntity<ConcurrentProcessFailureLogsInformation> prepareResponse(
			Iterable<CoreProcessFailureLog> coreProcessFailureLogList) {

		log.debug("prepareResponse called");

		List<ConcurrentProcessFailureLogDefinition> concurrentProcessFailureLogResponseList = new ArrayList<ConcurrentProcessFailureLogDefinition>();

		coreProcessFailureLogList.forEach(failureLog -> {

			concurrentProcessFailureLogResponseList
					.add(coreProcessFailureLogTransformer.transformToConcurrentProcessFailureLogResponse(failureLog));
		});

		ConcurrentProcessFailureLogsInformation concurrentProcessFailureLogsInformation = new ConcurrentProcessFailureLogsInformation();
		concurrentProcessFailureLogsInformation
				.setConcurrentProcessFailureLogDefinitions(concurrentProcessFailureLogResponseList);

		if (null != concurrentProcessFailureLogResponseList && !concurrentProcessFailureLogResponseList.isEmpty()) {

			concurrentProcessFailureLogsInformation
					.setTotalFailureCount(BigInteger.valueOf(concurrentProcessFailureLogResponseList.size()));
		}

		return new ResponseEntity<ConcurrentProcessFailureLogsInformation>(concurrentProcessFailureLogsInformation,
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countProcessFailuresByBatchIds(List<String> batchIds) {

		log.debug("countProcessFailuresByBatchIds called for batchIds -> {}", batchIds);

		return Optional.ofNullable(
				coreProcessFailureLogRepository.countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(batchIds,
						RetryStatus.F.toString(), batchIds))
				.map(failureCount -> {

					Assert.state(failureCount > -1, "ProcessCount should be greater than -1, check the processIds ");

					return new ResponseEntity<Long>(failureCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned"));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessFailureLogsInformation> listAllProcessFailureLogForBatchId(String batchId) {

		log.debug("listAllProcessFailureLogForBatchId called for batchId -> {}", batchId);

		Iterable<CoreProcessFailureLog> coreProcessFailureLogList = coreProcessFailureLogRepository
				.findAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull(batchId, RetryStatus.F.toString(),
						batchId);

		if (IterableUtil.isNullOrEmpty(coreProcessFailureLogList)) {

			throw new ResourceNotFoundException("CoreProcessFailureLogList is empty or null for batchId# " + batchId);
		}

		return prepareResponse(coreProcessFailureLogList);
	}

}