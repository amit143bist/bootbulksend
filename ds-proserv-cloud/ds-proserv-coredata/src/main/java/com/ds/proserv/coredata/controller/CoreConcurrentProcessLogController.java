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

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.coredata.model.CoreConcurrentProcessLog;
import com.ds.proserv.coredata.repository.CoreConcurrentProcessLogRepository;
import com.ds.proserv.coredata.transformer.ConcurrentProcessLogTransformer;
import com.ds.proserv.coredata.validator.ProcessLogValidator;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogsInformation;
import com.ds.proserv.feign.coredata.service.CoreConcurrentProcessLogService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Transactional
@Slf4j
public class CoreConcurrentProcessLogController implements CoreConcurrentProcessLogService {

	@Lazy
	@Autowired
	private CoreConcurrentProcessLogRepository concurrentProcessLogRepository;

	@Autowired
	private ConcurrentProcessLogTransformer concurrentProcessLogTransformer;

	@Autowired
	private ProcessLogValidator processLogValidator;

	@Autowired
	private HazelcastInstance hazelcast;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogDefinition> saveConcurrentProcess(
			ConcurrentProcessLogDefinition concurrentProcessLogRequest) {

		processLogValidator.validateSaveData(concurrentProcessLogRequest);
		CoreConcurrentProcessLog coreConcurrentProcessLog = concurrentProcessLogTransformer
				.tranformToCoreConcurrentProcessLog(concurrentProcessLogRequest);

		log.debug("concurrentProcessLogRequest transformed to coreConcurrentProcessLog for batchId -> {}",
				concurrentProcessLogRequest.getBatchId());
		return Optional.ofNullable(concurrentProcessLogRepository.save(coreConcurrentProcessLog))
				.map(savedCoreConcurrentProcessLog -> {

					Assert.notNull(savedCoreConcurrentProcessLog.getProcessId(),
							"ProcessId cannot be null for batchId# " + concurrentProcessLogRequest.getBatchId());

					log.debug(
							"savedCoreConcurrentProcessLog successfully saved in CoreConcurrentProcessLogController.saveConcurrentProcess()");
					return new ResponseEntity<ConcurrentProcessLogDefinition>(concurrentProcessLogTransformer
							.tranformToConcurrentProcessLogResponse(savedCoreConcurrentProcessLog), HttpStatus.CREATED);
				})
				.orElseThrow(() -> new ResourceNotSavedException("Requested Concurrent Process not saved for batchId# "
						+ concurrentProcessLogRequest.getBatchId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogDefinition> updateConcurrentProcess(
			ConcurrentProcessLogDefinition concurrentProcessLogRequest, String processId) {

		log.debug("updateConcurrentProcess called for processId -> {}", processId);

		return concurrentProcessLogRepository.findById(processId).map(coreConcurrentProcess -> {

			if (null != coreConcurrentProcess.getProcessEndDateTime()) {

				coreConcurrentProcess.setProcessStatus("RE_" + concurrentProcessLogRequest.getProcessStatus());
			} else {

				coreConcurrentProcess.setProcessStatus(concurrentProcessLogRequest.getProcessStatus());
			}

			coreConcurrentProcess.setProcessEndDateTime(LocalDateTime.now());

			if (null != concurrentProcessLogRequest.getTotalRecordsInProcess()) {

				coreConcurrentProcess.setTotalRecordsInProcess(concurrentProcessLogRequest.getTotalRecordsInProcess());
			}

			CoreConcurrentProcessLog savedCoreConcurrentProcessLog = null;

			IMap<String, String> map = hazelcast.getMap("PROCESS_LOCKS");
			map.lock(AppConstants.SAVE_LOCK_NAME);

			try {

				savedCoreConcurrentProcessLog = concurrentProcessLogRepository.save(coreConcurrentProcess);
			} catch (Exception exp) {

				unlockKey(map, AppConstants.SAVE_LOCK_NAME);
				log.error(
						"Lock error occurred {}, message is {}, requested Concurrent Process not saved for batchId# {}",
						exp, exp.getMessage(), concurrentProcessLogRequest.getBatchId());
				throw new ResourceNotSavedException(
						"Lock error occurred, requested Concurrent Process not saved for batchId# "
								+ concurrentProcessLogRequest.getBatchId());
			} finally {

				unlockKey(map, AppConstants.SAVE_LOCK_NAME);
			}

			return new ResponseEntity<ConcurrentProcessLogDefinition>(concurrentProcessLogTransformer
					.tranformToConcurrentProcessLogResponse(savedCoreConcurrentProcessLog), HttpStatus.OK);

		}).orElseThrow(() -> new ResourceNotFoundException("No Process found with processId# " + processId));
	}

	private void unlockKey(IMap<String, String> map, String key) {
		try {

			map.unlock("SAVE_LOCK");
		} catch (Exception exp) {

			log.error("Exception -> {} occurred in unlockKey for key -> {}", exp + "_" + exp.getMessage(), key);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countPendingConcurrentProcessInBatch(String batchId) {

		log.debug("countPendingConcurrentProcessInBatch called for batchId -> {}", batchId);

		return Optional.ofNullable(concurrentProcessLogRepository.countByBatchIdAndProcessEndDateTimeIsNull(batchId))
				.map(processCount -> {

					Assert.state(processCount > -1,
							"ProcessCount should be greater than -1, check the batchId# " + batchId);

					return new ResponseEntity<Long>(processCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned for batchId# " + batchId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countPendingConcurrentProcessInGroup(String groupId) {

		log.debug("countPendingConcurrentProcessInGroup called for groupId -> {}", groupId);

		return Optional.ofNullable(concurrentProcessLogRepository.countByGroupIdAndProcessEndDateTimeIsNull(groupId))
				.map(processCount -> {

					Assert.state(processCount > -1,
							"ProcessCount should be greater than -1, check the groupId# " + groupId);

					return new ResponseEntity<Long>(processCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException("No result returned for groupId# " + groupId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllProcessesForBatchId(String batchId) {

		log.debug("findAllBatchesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchId(batchId), batchId);

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllInCompleteProcessesForBatchId(String batchId) {

		log.debug("findAllIncompleteBatchesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndProcessEndDateTimeIsNull(batchId),
				batchId);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllCompleteProcessesForBatchId(String batchId) {

		log.debug("findAllCompleteProcessesForBatchId called for batchId -> {}", batchId);

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndProcessEndDateTimeIsNotNull(batchId),
				batchId);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogDefinition> findProcessByProcessId(String processId) {

		log.debug("findProcessByProcessId called for processId -> {}", processId);

		return concurrentProcessLogRepository.findById(processId).map(processLog -> {

			return new ResponseEntity<ConcurrentProcessLogDefinition>(
					concurrentProcessLogTransformer.tranformToConcurrentProcessLogResponse(processLog), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"Requested Concurrent Process not found for processId# " + processId));
	}

	private ResponseEntity<ConcurrentProcessLogsInformation> prepareResponse(
			Iterable<CoreConcurrentProcessLog> coreConcurrentProcessLogIterable, String batchId) {

		log.debug("prepareResponse called for batchId -> {}", batchId);

		if (IterableUtil.isNullOrEmpty(coreConcurrentProcessLogIterable)) {

			if (null != batchId) {

				log.error("CoreConcurrentProcessLogIterable is null, No process exists for batchId# {}", batchId);
				throw new ResourceNotFoundException("No process exists for batchId# " + batchId);
			}
		}

		List<ConcurrentProcessLogDefinition> concurrentProcessLogDefinitionList = new ArrayList<ConcurrentProcessLogDefinition>();
		coreConcurrentProcessLogIterable.forEach(coreConcurrentProcessLog -> {

			concurrentProcessLogDefinitionList.add(
					concurrentProcessLogTransformer.tranformToConcurrentProcessLogResponse(coreConcurrentProcessLog));
		});

		ConcurrentProcessLogsInformation concurrentProcessLogsInformation = new ConcurrentProcessLogsInformation();
		concurrentProcessLogsInformation.setConcurrentProcessLogDefinitions(concurrentProcessLogDefinitionList);

		if (null != concurrentProcessLogDefinitionList && !concurrentProcessLogDefinitionList.isEmpty()) {

			concurrentProcessLogsInformation
					.setTotalProcessesCount(Long.valueOf(concurrentProcessLogDefinitionList.size()));
		}

		return new ResponseEntity<ConcurrentProcessLogsInformation>(concurrentProcessLogsInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countTotalRecordsInGroup(String groupId) {

		return Optional.ofNullable(concurrentProcessLogRepository.getTotalRecordsInGroup(groupId))
				.map(totalRecordsCount -> {

					Assert.state(totalRecordsCount > -1,
							"TotalRecordsInBatch should be greater than -1, check the groupId# " + groupId);

					return new ResponseEntity<Long>(totalRecordsCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No result returned in countTotalRecordsInBatch for groupId# " + groupId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllParentGroups(String batchId) {

		return prepareResponse(concurrentProcessLogRepository.findAllByBatchIdAndAccountIdIsNull(batchId), batchId);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<ConcurrentProcessLogsInformation> findAllSuccessParentGroups(String batchId) {

		return prepareResponse(concurrentProcessLogRepository
				.findAllByBatchIdAndAccountIdIsNullAndProcessEndDateTimeIsNotNull(batchId), batchId);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countTotalRecordsInBatch(String batchId) {

		return Optional.ofNullable(concurrentProcessLogRepository.getTotalRecordsInBatch(batchId))
				.map(totalRecordsCount -> {

					Assert.state(totalRecordsCount > -1,
							"TotalRecordsInBatch should be greater than -1, check the batchId# " + batchId);

					return new ResponseEntity<Long>(totalRecordsCount, HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No result returned in countTotalRecordsInBatch for batchId# " + batchId));
	}

}