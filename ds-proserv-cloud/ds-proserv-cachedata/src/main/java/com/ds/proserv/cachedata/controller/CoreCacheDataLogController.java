package com.ds.proserv.cachedata.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

import com.ds.proserv.cachedata.model.CacheDataLog;
import com.ds.proserv.cachedata.processor.CacheDataProcessor;
import com.ds.proserv.cachedata.repository.CacheDataLogPagingAndSortingRepository;
import com.ds.proserv.cachedata.transformer.CacheLogTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.domain.CacheLogInformation;
import com.ds.proserv.feign.cachedata.service.CoreCacheDataLogService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
public class CoreCacheDataLogController implements CoreCacheDataLogService {

	@Lazy
	@Autowired
	private CacheDataProcessor cacheDataProcessor;

	@Autowired
	private CacheLogTransformer cacheLogTransformer;

	@Lazy
	@Autowired
	private CacheDataLogPagingAndSortingRepository cacheDataLogPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> saveCache(CacheLogDefinition cacheLogDefinition) {

		log.info("CacheKey {} and cacheValue {} need to be saved", cacheLogDefinition.getCacheKey(),
				cacheLogDefinition.getCacheValue());

		CacheDataLog cacheDataLog = cacheLogTransformer.transformToCacheDataLog(cacheLogDefinition);

		return Optional.ofNullable(cacheDataLogPagingAndSortingRepository.save(cacheDataLog))
				.map(savedcoreCacheDataLog -> {

					Assert.notNull(savedcoreCacheDataLog.getCacheId(),
							"CacheId cannot be null for CacheKey " + cacheLogDefinition.getCacheKey());

					return new ResponseEntity<CacheLogDefinition>(
							cacheLogTransformer.transformToCacheLogDefinition(savedcoreCacheDataLog),
							HttpStatus.CREATED);
				}).orElseThrow(
						() -> new ResourceNotSavedException("Cache not saved for " + cacheLogDefinition.getCacheKey()));

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> updateCache(String cacheId, CacheLogDefinition cacheLogDefinition) {

		log.info("updateCache called for cacheId -> {}", cacheId);

		return cacheDataLogPagingAndSortingRepository.findById(cacheId).map(cacheDataLog -> {

			CacheDataLog cacheDataLogToBeUpdated = cacheLogTransformer.transformToCacheLogAsUpdate(cacheLogDefinition,
					cacheDataLog);

			return new ResponseEntity<CacheLogDefinition>(cacheLogTransformer.transformToCacheLogDefinition(
					cacheDataLogPagingAndSortingRepository.save(cacheDataLogToBeUpdated)), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheId# " + cacheId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> saveAllCache(CacheLogInformation cacheLogInformation) {

		List<CacheLogDefinition> cacheLogDefinitionList = cacheLogInformation.getCacheLogDefinitions();
		log.info("Size of cacheList is {}", cacheLogDefinitionList.size());

		List<CacheDataLog> coreCacheDataLogList = cacheLogTransformer
				.transformToCacheDataLogList(cacheLogDefinitionList);

		return Optional.ofNullable(cacheDataLogPagingAndSortingRepository.saveAll(coreCacheDataLogList))
				.map(savedcoreCacheDataLog -> {

					log.info("Saved CacheData successfully");
					return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException("Cache not saved"));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogInformation> updateAllCache(CacheLogInformation cacheLogInformation) {

		String processId = cacheLogInformation.getProcessId();
		List<CacheLogDefinition> cacheLogDefinitionList = cacheLogInformation.getCacheLogDefinitions();
		log.info("Size of cacheList is {}", cacheLogDefinitionList.size());

		if (cacheDataProcessor.isDataAvailableForProcessing(processId, cacheLogDefinitionList)) {

			List<CacheDataLog> prepareToSaveCacheDataLogList = cacheDataProcessor.compareAndPrepareData(processId,
					cacheLogDefinitionList);

			if (cacheDataProcessor.isDataAvailableForSave(processId, prepareToSaveCacheDataLogList)) {

				CacheLogInformation cacheLogInformationAfterUpdate = new CacheLogInformation();

				List<CacheDataLog> updatedCacheDataLogList = cacheDataProcessor.callRepositorySaveOperations(processId,
						prepareToSaveCacheDataLogList);
				cacheLogInformationAfterUpdate.setCacheLogDefinitions(
						cacheLogTransformer.transformToCacheLogDefinitionList(updatedCacheDataLogList));
				cacheLogInformationAfterUpdate.setTotalRecords(Long.valueOf(updatedCacheDataLogList.size()));

				return new ResponseEntity<CacheLogInformation>(cacheLogInformationAfterUpdate, HttpStatus.ACCEPTED);
			} else {

				log.error("-------------------- No Cache data to save --------------------");
				throw new ResourceNotSavedException(AppConstants.NO_DATA_AVAILABLE_TO_SAVE);
			}
		} else {

			log.error("-------------------- No Cache data to process --------------------");
			throw new ResourceNotSavedException(AppConstants.NO_DATA_AVAILABLE_TO_PROCESS);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> deleteByCacheId(String cacheId) {

		log.info("DeleteByCacheId called for cacheId -> {}", cacheId);

		return cacheDataLogPagingAndSortingRepository.findById(cacheId).map(cacheData -> {

			cacheDataLogPagingAndSortingRepository.delete(cacheData);
			return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.ACCEPTED);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheId " + cacheId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> deleteByCacheKey(String cacheKey) {

		log.info("DeleteByCacheKey called for cacheKey -> {}", cacheKey);

		return cacheDataLogPagingAndSortingRepository.findByCacheKey(cacheKey).map(cacheData -> {

			cacheDataLogPagingAndSortingRepository.delete(cacheData);
			return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.ACCEPTED);
		}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey));

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> deleteByCacheKeyAndCacheReference(String cacheKey, String cacheReference) {

		log.info("DeleteByCacheKey called for cacheKey -> {} and cacheReference -> {}", cacheKey, cacheReference);

		return cacheDataLogPagingAndSortingRepository.findByCacheKeyAndCacheReference(cacheKey, cacheReference)
				.map(cacheData -> {

					cacheDataLogPagingAndSortingRepository.delete(cacheData);
					return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.ACCEPTED);
				}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheId(String cacheId) {

		log.info("FindBycacheId called for cacheId -> {}", cacheId);

		return new ResponseEntity<CacheLogDefinition>(cacheLogTransformer.transformToCacheLogDefinition(
				cacheDataLogPagingAndSortingRepository.findById(cacheId).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheId " + cacheId))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheKey(String cacheKey) {

		log.info("FindByCacheKey called for cacheKey -> {}", cacheKey);

		return new ResponseEntity<CacheLogDefinition>(cacheLogTransformer.transformToCacheLogDefinition(
				cacheDataLogPagingAndSortingRepository.findByCacheKey(cacheKey).map(cacheData -> {

					return cacheData;
				}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey " + cacheKey))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheKeyAndCacheValue(String cacheKey, String cacheValue) {

		log.info("FindByCacheKeyAndCacheValue called for cacheKey -> {} and cacheValue -> {}", cacheKey, cacheValue);

		return new ResponseEntity<CacheLogDefinition>(
				cacheLogTransformer.transformToCacheLogDefinition(cacheDataLogPagingAndSortingRepository
						.findByCacheKeyAndCacheValue(cacheKey, cacheValue).map(cacheData -> {

							return cacheData;
						})
						.orElseThrow(() -> new ResourceNotFoundException(
								"No Cache found with cacheKey " + cacheKey + " cacheValue -> " + cacheValue))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheKeyAndCacheReference(String cacheKey, String cacheReference) {

		log.info("FindByCacheKeyAndCacheReference called for cacheKey -> {} and cacheReference -> {}", cacheKey,
				cacheReference);

		return new ResponseEntity<CacheLogDefinition>(
				cacheLogTransformer.transformToCacheLogDefinition(cacheDataLogPagingAndSortingRepository
						.findByCacheKeyAndCacheReference(cacheKey, cacheReference).map(cacheData -> {

							return cacheData;
						})
						.orElseThrow(() -> new ResourceNotFoundException(
								"No Cache found with cacheKey " + cacheKey + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheKeyValueAndCacheReference(String cacheKey, String cacheValue,
			String cacheReference) {

		log.info(
				"FindByCacheKeyValueAndCacheReference called for cacheKey -> {}, cacheValue -> {} and cacheReference -> {}",
				cacheKey, cacheValue, cacheReference);

		return new ResponseEntity<CacheLogDefinition>(
				cacheLogTransformer.transformToCacheLogDefinition(cacheDataLogPagingAndSortingRepository
						.findByCacheKeyAndCacheValueAndCacheReference(cacheKey, cacheValue, cacheReference)
						.map(cacheData -> {

							return cacheData;
						})
						.orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheKey -> " + cacheKey
								+ " cacheValue -> " + cacheValue + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogInformation> findAllCacheDataLog() {

		log.info("FindAllCacheDataLog called");

		Iterable<CacheDataLog> cacheDataLogIterable = cacheDataLogPagingAndSortingRepository.findAll();

		CacheLogInformation cacheLogInformation = new CacheLogInformation();
		if (IterableUtil.sizeOf(cacheDataLogIterable) > 0) {

			List<CacheDataLog> cacheDataLogList = StreamSupport.stream(cacheDataLogIterable.spliterator(), false)
					.collect(Collectors.toList());

			List<CacheLogDefinition> cacheLogDefinitionList = cacheLogTransformer
					.transformToCacheLogDefinitionList(cacheDataLogList);

			cacheLogInformation.setCacheLogDefinitions(cacheLogDefinitionList);
			cacheLogInformation.setTotalRecords(Long.valueOf(cacheLogDefinitionList.size()));

			return new ResponseEntity<CacheLogInformation>(cacheLogInformation, HttpStatus.OK);
		} else {
			return new ResponseEntity<CacheLogInformation>(cacheLogInformation, HttpStatus.NO_CONTENT);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogInformation> findAllByCacheReference(String cacheReference) {

		log.info("FindAllByCacheReference called for cacheReference -> {}", cacheReference);

		Iterable<CacheDataLog> cacheDataLogIterable = cacheDataLogPagingAndSortingRepository
				.findAllByCacheReference(cacheReference);

		if (IterableUtil.sizeOf(cacheDataLogIterable) > 0) {

			List<CacheDataLog> cacheDataLogList = StreamSupport.stream(cacheDataLogIterable.spliterator(), false)
					.collect(Collectors.toList());

			List<CacheLogDefinition> cacheLogDefinitionList = cacheLogTransformer
					.transformToCacheLogDefinitionList(cacheDataLogList);

			CacheLogInformation cacheLogInformation = new CacheLogInformation();
			cacheLogInformation.setCacheLogDefinitions(cacheLogDefinitionList);
			cacheLogInformation.setTotalRecords(Long.valueOf(cacheLogDefinitionList.size()));

			return new ResponseEntity<CacheLogInformation>(cacheLogInformation, HttpStatus.OK);
		} else {

			log.info("No Cache present for cacheReference -> {}", cacheReference);
			CacheLogInformation cacheLogInformation = new CacheLogInformation();
			return new ResponseEntity<CacheLogInformation>(cacheLogInformation, HttpStatus.NO_CONTENT);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogDefinition> findByCacheValueAndCacheReference(String cacheValue,
			String cacheReference) {

		log.debug("FindByCacheValueAndCacheReference called for cacheValue -> {} and cacheReference -> {}", cacheValue,
				cacheReference);

		return new ResponseEntity<CacheLogDefinition>(
				cacheLogTransformer.transformToCacheLogDefinition(cacheDataLogPagingAndSortingRepository
						.findByCacheValueAndCacheReference(cacheValue, cacheReference).map(cacheData -> {

							return cacheData;
						}).orElseThrow(() -> new ResourceNotFoundException("No Cache found with cacheValue "
								+ cacheValue + " cacheReference -> " + cacheReference))),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CacheLogInformation> findByCacheValue(String cacheValue) {

		log.debug("FindByCacheValue called for cacheValue -> {}", cacheValue);

		Iterable<CacheDataLog> cacheDataLogIterable = cacheDataLogPagingAndSortingRepository
				.findAllByCacheValue(cacheValue);

		if (IterableUtil.sizeOf(cacheDataLogIterable) > 0) {

			List<CacheDataLog> cacheDataLogList = StreamSupport.stream(cacheDataLogIterable.spliterator(), false)
					.collect(Collectors.toList());

			List<CacheLogDefinition> cacheLogDefinitionList = cacheLogTransformer
					.transformToCacheLogDefinitionList(cacheDataLogList);

			CacheLogInformation cacheLogInformation = new CacheLogInformation();
			cacheLogInformation.setCacheLogDefinitions(cacheLogDefinitionList);
			cacheLogInformation.setTotalRecords(Long.valueOf(cacheLogDefinitionList.size()));

			return new ResponseEntity<CacheLogInformation>(cacheLogInformation, HttpStatus.OK);
		} else {

			throw new ResourceNotFoundException("No Cache found with cacheValue -> " + cacheValue);
		}
	}

}