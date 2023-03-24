package com.ds.proserv.envelopedata.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.envelopedata.model.DSException;
import com.ds.proserv.envelopedata.projection.DSExceptionIdProjection;
import com.ds.proserv.envelopedata.repository.DSExceptionPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSExceptionRepository;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.transformer.DSExceptionTransformer;
import com.ds.proserv.envelopedata.transformer.PageableTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionIdResult;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionInformation;
import com.ds.proserv.feign.envelopedata.service.DSExceptionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
@Transactional
public class DSExceptionController implements DSExceptionService {

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private DSExceptionTransformer dsExceptionTransformer;

	@Autowired
	private DSExceptionRepository dsExceptionRepository;

	@Autowired
	private DSExceptionPagingAndSortingRepository dsExceptionPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionDefinition> saveExceptionData(DSExceptionDefinition dsExceptionDefinition) {

		log.info("Saving DSExceptionDefinition for envelopeId -> {}", dsExceptionDefinition.getEnvelopeId());
		return Optional
				.ofNullable(dsExceptionRepository
						.save(dsExceptionTransformer.transformToDSException(dsExceptionDefinition)))
				.map(dsException -> {

					Assert.notNull(dsException.getId(),
							"Exception Id cannot be null for envelopeId " + dsExceptionDefinition.getEnvelopeId());

					return new ResponseEntity<DSExceptionDefinition>(
							dsExceptionTransformer.transformToDSExceptionDefinition(dsException), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException(
						"DSException not saved for " + dsExceptionDefinition.getEnvelopeId()));
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> bulkSaveExceptionData(DSExceptionInformation dsExceptionInformation) {

		log.info("BulkSave Exception called for processId -> {}", dsExceptionInformation.getProcessId());
		List<DSExceptionDefinition> dsExceptionDefinitionList = dsExceptionInformation.getDsExceptionDefinitions();
		List<DSException> dsExceptionList = new ArrayList<DSException>(dsExceptionDefinitionList.size());

		Iterable<DSException> dsExceptionIterable = dsDataHelperService.findAllDSExceptionsForAllEnvelopeIdsById(
				dsExceptionInformation.getProcessId(), dsExceptionDefinitionList);

		dsExceptionIterable.forEach(dsException -> {

			DSExceptionDefinition dsExceptionDefinition = dsExceptionDefinitionList.stream().filter(definition -> {

				if (definition.getId().equalsIgnoreCase(dsException.getId())) {

					return true;
				} else {

					return false;
				}

			}).findFirst().orElse(null);

			if (null != dsExceptionDefinition) {

				dsExceptionList
						.add(dsExceptionTransformer.transformToDSExceptionUpdate(dsExceptionDefinition, dsException));
			}

		});

		if (!dsExceptionList.isEmpty()) {

			dsExceptionRepository.saveAll(dsExceptionList);
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.CREATED);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionDefinition> updateExceptionData(DSExceptionDefinition dsExceptionDefinition,
			String id) {

		log.info("updateExceptionData called for exceptionId -> {}", id);

		return dsExceptionRepository.findById(id).map(dsException -> {

			dsExceptionDefinition.setId(id);
			return new ResponseEntity<DSExceptionDefinition>(
					dsExceptionTransformer.transformToDSExceptionDefinition(dsExceptionRepository.save(
							dsExceptionTransformer.transformToDSExceptionUpdate(dsExceptionDefinition, dsException))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No exception found with exceptionId# " + id));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> updateExceptionRetryStatus(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> exceptionIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.EXCEPTIONIDS_PARAM_NAME);

			List<String> retryStatusList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.RETRYSTATUSES_PARAM_NAME);

			List<String> processIdList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.PROCESSID_PARAM_NAME);
			if (null != processIdList && !processIdList.isEmpty()) {

				String processId = processIdList.get(0);

				log.info("updateExceptionRetryStatus called for processId -> {}", processId);
			}

			if (null != retryStatusList && !retryStatusList.isEmpty()) {

				if (retryStatusList.size() > 1) {

					throw new InvalidInputException("Only one RetryStatus allowed");
				}
			}

			dsExceptionRepository.updateDSExceptionStatus(retryStatusList.get(0), exceptionIdList);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionDefinition> findExceptionById(String id) {

		log.info("findExceptionById called for exceptionId -> {}", id);

		return dsExceptionRepository.findById(id).map(dsException -> {

			return new ResponseEntity<DSExceptionDefinition>(
					dsExceptionTransformer.transformToDSExceptionDefinition(dsException), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No exception found with exceptionId# " + id));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findExceptionByIds(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam exceptionIdsParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.EXCEPTIONIDS_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			if (null == exceptionIdsParam) {

				throw new InvalidInputException("exceptionIdsParam param cannot be null");
			}

			log.info("exceptionIdsParam value is {}", exceptionIdsParam);
			List<String> exceptionIdList = Stream.of(exceptionIdsParam.getParamValue().trim().split(","))
					.collect(Collectors.toList());

			Iterable<DSException> dsExceptionIterable = dsExceptionRepository.findAllByIdIn(exceptionIdList);

			DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();
			List<DSExceptionDefinition> dsExceptionDefinitionList = new ArrayList<DSExceptionDefinition>();
			dsExceptionIterable.forEach(dsException -> {

				dsExceptionDefinitionList.add(dsExceptionTransformer.transformToDSExceptionDefinition(dsException));
			});

			dsExceptionInformation.setDsExceptionDefinitions(dsExceptionDefinitionList);
			dsExceptionInformation.setTotalRecords(Long.valueOf(dsExceptionDefinitionList.size()));

			return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countAllExceptionsByEnvelopeIds(PageInformation pageInformation) {

		log.info("countAllExceptionsByEnvelopeIds -> {}", pageInformation.getPageQueryParams());

		Long exceptionsCount = null;
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam envelopeIdsParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.ENVELOPEIDS_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			if (null == envelopeIdsParam) {

				throw new InvalidInputException("envelopeIdsParam param cannot be null");
			}

			log.info("envelopeIdsParam value is {}", envelopeIdsParam);
			List<String> envelopeIdList = Stream.of(envelopeIdsParam.getParamValue().trim().split(","))
					.collect(Collectors.toList());

			exceptionsCount = dsExceptionPagingAndSortingRepository.countByEnvelopeIdIn(envelopeIdList);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<Long>(exceptionsCount, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findAllExceptionsByEnvelopeIds(PageInformation pageInformation) {

		log.info("findAllExceptionsByEnvelopeIds -> {}", pageInformation.getPageQueryParams());

		DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> envelopeIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.ENVELOPEIDS_PARAM_NAME);

			Slice<DSException> dsExceptionSlice = dsExceptionPagingAndSortingRepository
					.findAllByEnvelopeIdIn(envelopeIdList, pageable);

			prepareResponseFromSlice(dsExceptionInformation, dsExceptionSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findExceptionsByEnvelopeId(String envelopeId) {

		log.info("findExceptionsByEnvelopeId for envelopeId -> {}", envelopeId);

		Iterable<DSException> dsExceptionIterable = dsExceptionRepository.findAllByEnvelopeId(envelopeId);

		if (IterableUtil.isNullOrEmpty(dsExceptionIterable)) {

			throw new ResourceNotFoundException("dsExceptionList is empty or null for envelopeId# " + envelopeId);
		} else {

			List<DSExceptionDefinition> dsExceptionDefinitionList = new ArrayList<DSExceptionDefinition>();
			dsExceptionIterable.forEach(dsException -> {

				dsExceptionDefinitionList.add(dsExceptionTransformer.transformToDSExceptionDefinition(dsException));
			});

			DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();

			dsExceptionInformation.setDsExceptionDefinitions(dsExceptionDefinitionList);
			dsExceptionInformation.setTotalRecords(Long.valueOf(dsExceptionDefinitionList.size()));

			return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findAllExceptionsByDateRange(PageInformation pageInformation) {

		log.info("findAllExceptionsByDateRange -> {}", pageInformation.getPageQueryParams());

		DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();
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

			Slice<DSException> dsExceptionSlice = dsExceptionPagingAndSortingRepository
					.findAllByExceptionDateTimeBetweenAndRetryStatusOrExceptionDateTimeBetweenAndRetryStatusIsNull(
							fromLocalDateTime, toLocalDateTime, RetryStatus.F.toString(), fromLocalDateTime,
							toLocalDateTime, pageable);

			prepareResponseFromSlice(dsExceptionInformation, dsExceptionSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countAllExceptionsByDateRange(PageInformation pageInformation) {

		log.info("countAllExceptionsByDateRange -> {}", pageInformation.getPageQueryParams());

		Long exceptionsCount = null;

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

			exceptionsCount = dsExceptionPagingAndSortingRepository
					.countByExceptionDateTimeBetweenAndRetryStatusOrExceptionDateTimeBetweenAndRetryStatusIsNull(
							fromLocalDateTime, toLocalDateTime, RetryStatus.F.toString(), fromLocalDateTime,
							toLocalDateTime);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<Long>(exceptionsCount, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countByRetryStatusIn(PageInformation pageInformation) {

		log.info("countByRetryStatusIn -> {}", pageInformation.getPageQueryParams());

		Long exceptionsCount = null;

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			exceptionsCount = dsExceptionPagingAndSortingRepository.countByRetryStatusIn(retryStatusesList);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<Long>(exceptionsCount, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findAllExceptionsByRetryStatuses(PageInformation pageInformation) {

		log.info("findAllExceptionsByRetryStatuses -> {}", pageInformation.getPageQueryParams());

		DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			Slice<DSException> dsExceptionSlice = dsExceptionPagingAndSortingRepository
					.findAllByRetryStatusIn(retryStatusesList, pageable);

			prepareResponseFromSlice(dsExceptionInformation, dsExceptionSlice);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionIdResult> findAllExceptionIdsByRetryStatuses(PageInformation pageInformation) {

		log.info("Inside findAllExceptionIdsByRetryStatuses for pageInformation  -> {}", pageInformation);

		DSExceptionIdResult dsExceptionIdResult = new DSExceptionIdResult();
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			Iterable<DSExceptionIdProjection> dsExceptionProjectionIterable = dsExceptionRepository
					.findIdByRetryStatusIn(retryStatusesList);

			prepareResponseFromSlice(dsExceptionIdResult, dsExceptionProjectionIterable);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionIdResult>(dsExceptionIdResult, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<Long> countByRetryStatusOrNullRetryStatus(PageInformation pageInformation) {

		log.info("countByRetryStatusOrNullRetryStatus -> {}", pageInformation.getPageQueryParams());

		Long exceptionsCount = null;

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			exceptionsCount = dsExceptionPagingAndSortingRepository
					.countByRetryStatusInOrRetryStatusIsNull(retryStatusesList);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<Long>(exceptionsCount, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionInformation> findAllExceptionsByRetryStatusesOrNullRetryStatus(
			PageInformation pageInformation) {

		log.info("findAllExceptionsByRetryStatusesOrNullRetryStatus -> {}", pageInformation.getPageQueryParams());

		DSExceptionInformation dsExceptionInformation = new DSExceptionInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			Slice<DSException> dsExceptionSlice = dsExceptionPagingAndSortingRepository
					.findAllByRetryStatusInOrRetryStatusIsNull(retryStatusesList, pageable);

			prepareResponseFromSlice(dsExceptionInformation, dsExceptionSlice);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionInformation>(dsExceptionInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSExceptionIdResult> findAllExceptionIdsByRetryStatusesOrNullRetryStatus(
			PageInformation pageInformation) {

		log.info("Inside findAllExceptionIdsByRetryStatusesOrNullRetryStatus for pageInformation  -> {}",
				pageInformation);

		DSExceptionIdResult dsExceptionIdResult = new DSExceptionIdResult();
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam retryStatusesParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.RETRYSTATUSES_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> retryStatusesList = null;

			if (null == retryStatusesParam || StringUtils.isEmpty(retryStatusesParam.getParamValue())) {

				log.info("Since No RetryStatus sent so setting F as Retry Status for {}",
						pageInformation.getPageQueryParams());
				retryStatusesList = new ArrayList<String>();
				retryStatusesList.add(RetryStatus.F.toString());
			} else {

				retryStatusesList = Stream.of(retryStatusesParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.RETRYSTATUSES_PARAM_NAME, retryStatusesParam);

			Iterable<DSExceptionIdProjection> dsExceptionProjectionIterable = dsExceptionRepository
					.findIdByRetryStatusInOrRetryStatusIsNull(retryStatusesList);

			prepareResponseFromSlice(dsExceptionIdResult, dsExceptionProjectionIterable);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSExceptionIdResult>(dsExceptionIdResult, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(DSExceptionIdResult dsExceptionIdResult,
			Iterable<DSExceptionIdProjection> dsExceptionProjectionIterable) {

		if (null != dsExceptionProjectionIterable && null != dsExceptionProjectionIterable.iterator()
				&& dsExceptionProjectionIterable.iterator().hasNext()) {

			List<String> exceptionIds = StreamSupport.stream(dsExceptionProjectionIterable.spliterator(), false)
					.map(DSExceptionIdProjection::getId).collect(Collectors.toList());

			dsExceptionIdResult.setTotalRecords(Long.valueOf(exceptionIds.size()));
			dsExceptionIdResult.setDsEnvelopeIds(exceptionIds);
		}
	}

	private void prepareResponseFromSlice(DSExceptionInformation dsExceptionInformation,
			Slice<DSException> dsExceptionSlice) {

		log.info("prepareResponseFromSlice called for {}", dsExceptionSlice);

		if (null != dsExceptionSlice && !dsExceptionSlice.isEmpty() && dsExceptionSlice.hasContent()) {

			List<DSExceptionDefinition> dsExceptionDefinitionList = new ArrayList<DSExceptionDefinition>();
			dsExceptionSlice.getContent().forEach(dsException -> {

				dsExceptionDefinitionList.add(dsExceptionTransformer.transformToDSExceptionDefinition(dsException));
			});

			dsExceptionInformation.setCurrentPage(Long.valueOf(dsExceptionSlice.getNumber()));
			dsExceptionInformation.setNextAvailable(dsExceptionSlice.hasNext());
			dsExceptionInformation.setContentAvailable(true);
			dsExceptionInformation.setDsExceptionDefinitions(dsExceptionDefinitionList);

		} else {

			dsExceptionInformation.setContentAvailable(false);
			dsExceptionInformation.setDsExceptionDefinitions(null);
		}
	}

}