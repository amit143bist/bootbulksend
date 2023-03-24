package com.ds.proserv.bulksenddata.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

import com.ds.proserv.bulksenddata.model.BulkSendProcessLog;
import com.ds.proserv.bulksenddata.projection.BulkSendProcessLogIdProjection;
import com.ds.proserv.bulksenddata.repository.BulkSendProcessLogPagingAndSortingRepository;
import com.ds.proserv.bulksenddata.transformer.BulkSendProcessLogTransformer;
import com.ds.proserv.bulksenddata.transformer.PageableTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogIdResult;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogInformation;
import com.ds.proserv.feign.bulksenddata.service.BulkSendProcessLogService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendProcessLogController implements BulkSendProcessLogService {

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private BulkSendProcessLogTransformer bulkSendProcessLogTransformer;

	@Autowired
	private BulkSendProcessLogPagingAndSortingRepository bulkSendProcessLogPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendProcessLogDefinition> saveBulkSendProcessLog(
			BulkSendProcessLogDefinition bulkSendProcessLogDefinition) {

		log.info("Saving BulkSendProcessLog for batchId -> {}", bulkSendProcessLogDefinition.getBatchId());
		return Optional
				.ofNullable(bulkSendProcessLogPagingAndSortingRepository.save(
						bulkSendProcessLogTransformer.transformToBulkSendProcessLog(bulkSendProcessLogDefinition)))
				.map(savedBulkSendLogData -> {

					Assert.notNull(savedBulkSendLogData.getCreatedBy(),
							"CreatedBy cannot be null for batchId " + savedBulkSendLogData.getBatchId());

					return new ResponseEntity<BulkSendProcessLogDefinition>(
							bulkSendProcessLogTransformer.transformToBulkSendProcessLogDefinition(savedBulkSendLogData),
							HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException(
						"BulkSendProcessLog not saved for batchId -> " + bulkSendProcessLogDefinition.getBatchId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendProcessLogDefinition> updateBulkSendProcessLog(
			BulkSendProcessLogDefinition bulkSendProcessLogDefinition, String batchId) {

		log.info("Updating BulkSendProcessLog for id -> {}", batchId);
		return bulkSendProcessLogPagingAndSortingRepository.findById(batchId).map(savedBulkSendProcessLogData -> {

			bulkSendProcessLogDefinition.setBatchId(batchId);
			return new ResponseEntity<BulkSendProcessLogDefinition>(
					bulkSendProcessLogTransformer
							.transformToBulkSendProcessLogDefinition(bulkSendProcessLogPagingAndSortingRepository
									.save(bulkSendProcessLogTransformer.transformToBulkSendProcessLogUpdate(
											bulkSendProcessLogDefinition, savedBulkSendProcessLogData))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendProcessLog found for id# " + batchId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendProcessLogInformation> bulkSaveBulkSendProcessLog(
			BulkSendProcessLogInformation bulkSendProcessLogInformation) {

		List<BulkSendProcessLogDefinition> bulkSendProcessLogDefinitionList = bulkSendProcessLogInformation
				.getBulkSendProcessLogDefinitions();

		BulkSendProcessLogInformation bulkSendProcessLogInformationSaved = new BulkSendProcessLogInformation();
		if (null != bulkSendProcessLogDefinitionList && !bulkSendProcessLogDefinitionList.isEmpty()) {

			List<BulkSendProcessLog> customEnvelopeDataList = new ArrayList<BulkSendProcessLog>(
					bulkSendProcessLogDefinitionList.size());

			List<String> batchIds = bulkSendProcessLogDefinitionList.stream()
					.map(BulkSendProcessLogDefinition::getBatchId).collect(Collectors.toList());

			Iterable<BulkSendProcessLog> bulkSendProcessLogSavedDataIterable = bulkSendProcessLogPagingAndSortingRepository
					.findAllById(batchIds);

			bulkSendProcessLogSavedDataIterable.forEach(bulkSendProcessLogSavedData -> {

				BulkSendProcessLogDefinition toProcessBulkSendProcessLogDefinition = bulkSendProcessLogDefinitionList
						.stream().filter(bulkSendProcessLogDefinition -> bulkSendProcessLogDefinition.getBatchId()
								.equalsIgnoreCase(bulkSendProcessLogSavedData.getBatchId()))
						.findFirst().orElse(null);

				BulkSendProcessLog toSaveBulkSendProcessLog = bulkSendProcessLogTransformer
						.transformToBulkSendProcessLogUpdate(toProcessBulkSendProcessLogDefinition,
								bulkSendProcessLogSavedData);

				customEnvelopeDataList.add(toSaveBulkSendProcessLog);
			});

			Iterable<BulkSendProcessLog> savedBulkSendProcessLogIterable = bulkSendProcessLogPagingAndSortingRepository
					.saveAll(customEnvelopeDataList);
			List<BulkSendProcessLogDefinition> savedBulkSendProcessLogDefinitionList = bulkSendProcessLogInformation
					.getBulkSendProcessLogDefinitions();

			savedBulkSendProcessLogIterable.forEach(savedBulkSendProcessLog -> {

				savedBulkSendProcessLogDefinitionList.add(
						bulkSendProcessLogTransformer.transformToBulkSendProcessLogDefinition(savedBulkSendProcessLog));
			});

			bulkSendProcessLogInformationSaved.setBulkSendProcessLogDefinitions(savedBulkSendProcessLogDefinitionList);
			bulkSendProcessLogInformationSaved
					.setTotalRecords(Long.valueOf(savedBulkSendProcessLogDefinitionList.size()));

			return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformationSaved, HttpStatus.OK);

		} else {

			return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformationSaved,
					HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogDefinition> findById(String batchId) {

		log.info("Finding BulkSendProcessLog for id -> {}", batchId);
		return bulkSendProcessLogPagingAndSortingRepository.findById(batchId).map(savedBulkSendProcessLogData -> {

			return new ResponseEntity<BulkSendProcessLogDefinition>(
					bulkSendProcessLogTransformer.transformToBulkSendProcessLogDefinition(savedBulkSendProcessLogData),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No BulkSendProcessLog found for id# " + batchId));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBatchStatus(String batchStatus) {

		log.info("Counting BulkSendProcessLog for batchStatus -> {}", batchStatus);

		return new ResponseEntity<Long>(bulkSendProcessLogPagingAndSortingRepository.countByBatchStatus(batchStatus),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogInformation> findAllByBatchStatuses(PageInformation pageInformation) {

		log.info("FindAll BulkSendProcessLog for batchStatuses -> {}", pageInformation.getPageQueryParams());

		BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> bulkBatchStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BATCHSTATUSES_PARAM_NAME);

			Slice<BulkSendProcessLog> bulkSendLogSlice = bulkSendProcessLogPagingAndSortingRepository
					.findAllByBatchStatusIn(bulkBatchStatusList, pageable);

			prepareResponseFromSlice(bulkSendProcessLogInformation, bulkSendLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBatchName(String batchName) {

		log.info("Counting BulkSendProcessLog for batchName -> {}", batchName);

		return new ResponseEntity<Long>(bulkSendProcessLogPagingAndSortingRepository.countByBatchName(batchName),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogInformation> findAllByBatchNames(PageInformation pageInformation) {

		log.info("FindAll BulkSendProcessLog for batchStatuses -> {}", pageInformation.getPageQueryParams());

		BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> bulkBatchNameList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BATCHSTATUSES_PARAM_NAME);

			Slice<BulkSendProcessLog> bulkSendLogSlice = bulkSendProcessLogPagingAndSortingRepository
					.findAllByBatchNameIn(bulkBatchNameList, pageable);

			prepareResponseFromSlice(bulkSendProcessLogInformation, bulkSendLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBatchSubmittedDateTimeBetween(String startDateTime, String endDateTime) {

		log.info("Counting BulkSendProcessLog for startDateTime -> {} and endDateTime -> {}", startDateTime,
				endDateTime);

		LocalDateTime startLocalDateTime = LocalDateTime.parse(startDateTime);
		LocalDateTime endLocalDateTime = LocalDateTime.parse(endDateTime);

		return new ResponseEntity<Long>(bulkSendProcessLogPagingAndSortingRepository
				.countByBatchSubmittedDateTimeBetween(startLocalDateTime, endLocalDateTime), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogInformation> findAllByBatchSubmittedDateTimeBetween(
			PageInformation pageInformation) {

		log.info("FindAll BulkSendProcessLog in findAllByBatchSubmittedDateTimeBetween -> {}",
				pageInformation.getPageQueryParams());

		BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
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

			Slice<BulkSendProcessLog> bulkSendEnvelopeLogSlice = bulkSendProcessLogPagingAndSortingRepository
					.findAllByBatchSubmittedDateTimeBetween(fromLocalDateTime, toLocalDateTime, pageable);

			prepareResponseFromSlice(bulkSendProcessLogInformation, bulkSendEnvelopeLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByBatchSubmittedDateTimeBetweenAndBatchStatus(String startDateTime,
			String endDateTime, String batchStatus) {

		log.info("Counting BulkSendProcessLog for startDateTime -> {}, endDateTime -> {} and batchStatus -> {}",
				startDateTime, endDateTime, batchStatus);

		LocalDateTime startLocalDateTime = LocalDateTime.parse(startDateTime);
		LocalDateTime endLocalDateTime = LocalDateTime.parse(endDateTime);

		return new ResponseEntity<Long>(bulkSendProcessLogPagingAndSortingRepository
				.countByBatchSubmittedDateTimeBetweenAndBatchStatusOrBatchSubmittedDateTimeBetweenAndBatchStatusIsNull(
						startLocalDateTime, endLocalDateTime, batchStatus, startLocalDateTime, endLocalDateTime),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogInformation> findAllByBatchSubmittedDateTimeBetweenAndBatchStatus(
			PageInformation pageInformation) {

		log.info("FindAll BulkSendProcessLog in findAllByBatchSubmittedDateTimeBetweenAndBatchStatus -> {}",
				pageInformation.getPageQueryParams());

		BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
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

			List<String> bulkBatchStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BATCHSTATUSES_PARAM_NAME);

			if (null != bulkBatchStatusList && bulkBatchStatusList.size() > 1) {

				throw new InvalidInputException(
						"Only one status possible, cannot send more than status in the pageInformation");
			}

			LocalDateTime fromLocalDateTime = LocalDateTime.parse(fromDateTimeParam.getParamValue());

			LocalDateTime toLocalDateTime = LocalDateTime.parse(toDateTimeParam.getParamValue());

			Slice<BulkSendProcessLog> bulkSendProcessLogSlice = bulkSendProcessLogPagingAndSortingRepository
					.findAllByBatchSubmittedDateTimeBetweenAndBatchStatusOrBatchSubmittedDateTimeBetweenAndBatchStatusIsNull(
							fromLocalDateTime, toLocalDateTime, bulkBatchStatusList.get(0), fromLocalDateTime,
							toLocalDateTime, pageable);

			prepareResponseFromSlice(bulkSendProcessLogInformation, bulkSendProcessLogSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(BulkSendProcessLogInformation bulkSendProcessLogInformation,
			Slice<BulkSendProcessLog> bulkSendProcessLogSlice) {

		log.debug("prepareResponseFromSlice called for {}", bulkSendProcessLogSlice);

		if (null != bulkSendProcessLogSlice && !bulkSendProcessLogSlice.isEmpty()
				&& bulkSendProcessLogSlice.hasContent()) {

			List<BulkSendProcessLogDefinition> bulkSendProcessLogDefinitionList = new ArrayList<BulkSendProcessLogDefinition>();
			bulkSendProcessLogSlice.getContent().forEach(bulkSendProcessLog -> {

				bulkSendProcessLogDefinitionList
						.add(bulkSendProcessLogTransformer.transformToBulkSendProcessLogDefinition(bulkSendProcessLog));
			});

			bulkSendProcessLogInformation.setCurrentPage(Long.valueOf(bulkSendProcessLogSlice.getNumber()));
			bulkSendProcessLogInformation.setNextAvailable(bulkSendProcessLogSlice.hasNext());
			bulkSendProcessLogInformation.setContentAvailable(true);
			bulkSendProcessLogInformation.setBulkSendProcessLogDefinitions(bulkSendProcessLogDefinitionList);

		} else {

			bulkSendProcessLogInformation.setContentAvailable(false);
			bulkSendProcessLogInformation.setBulkSendProcessLogDefinitions(null);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogIdResult> findAllBatchIdsByBatchStatuses(PageInformation pageInformation) {

		BulkSendProcessLogIdResult bulkSendProcessLogIdResult = new BulkSendProcessLogIdResult();
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> batchStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BATCHSTATUSES_PARAM_NAME);

			Iterable<BulkSendProcessLogIdProjection> batchIdIterable = bulkSendProcessLogPagingAndSortingRepository
					.findBatchIdByBatchStatusIn(batchStatusList);

			prepareResponseFromSlice(bulkSendProcessLogIdResult, batchIdIterable);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<BulkSendProcessLogIdResult>(bulkSendProcessLogIdResult, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(BulkSendProcessLogIdResult bulkSendProcessLogIdResult,
			Iterable<BulkSendProcessLogIdProjection> batchIdIterable) {

		if (null != batchIdIterable && null != batchIdIterable.iterator() && batchIdIterable.iterator().hasNext()) {

			List<String> batchIds = StreamSupport.stream(batchIdIterable.spliterator(), false)
					.map(BulkSendProcessLogIdProjection::getBatchId).collect(Collectors.toList());

			bulkSendProcessLogIdResult.setTotalRecords(Long.valueOf(batchIds.size()));
			bulkSendProcessLogIdResult.setBatchIds(batchIds);
		} else {

			bulkSendProcessLogIdResult.setTotalRecords(0L);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendProcessLogInformation> findAllByBatchIds(PageInformation pageInformation) {

		BulkSendProcessLogInformation bulkSendProcessLogInformation = new BulkSendProcessLogInformation();
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> batchIds = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.BULKBATCHIDS_PARAM_NAME);

			Iterable<BulkSendProcessLog> bulkSendProcessLogIterable = bulkSendProcessLogPagingAndSortingRepository
					.findAllById(batchIds);

			if (null != bulkSendProcessLogIterable && null != bulkSendProcessLogIterable.iterator()
					&& bulkSendProcessLogIterable.iterator().hasNext()) {

				List<BulkSendProcessLogDefinition> bulkSendProcessLogDefinitionList = new ArrayList<BulkSendProcessLogDefinition>();
				bulkSendProcessLogIterable.forEach(bulkSendProcessLog -> {

					bulkSendProcessLogDefinitionList.add(
							bulkSendProcessLogTransformer.transformToBulkSendProcessLogDefinition(bulkSendProcessLog));
				});

				if (null != bulkSendProcessLogDefinitionList && !bulkSendProcessLogDefinitionList.isEmpty()) {

					bulkSendProcessLogInformation.setBulkSendProcessLogDefinitions(bulkSendProcessLogDefinitionList);
					bulkSendProcessLogInformation
							.setTotalRecords(Long.valueOf(bulkSendProcessLogDefinitionList.size()));

					return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation,
							HttpStatus.OK);
				}

			}

			return new ResponseEntity<BulkSendProcessLogInformation>(bulkSendProcessLogInformation,
					HttpStatus.NO_CONTENT);

		}

		return new ResponseEntity<BulkSendProcessLogInformation>(HttpStatus.BAD_REQUEST);
	}

}