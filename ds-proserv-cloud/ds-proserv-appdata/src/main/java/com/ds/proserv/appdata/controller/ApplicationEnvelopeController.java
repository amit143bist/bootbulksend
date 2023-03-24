package com.ds.proserv.appdata.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.appdata.domain.ApplicationEnvelopeSPRequest;
import com.ds.proserv.appdata.model.ApplicationEnvelopeData;
import com.ds.proserv.appdata.repository.ApplicationEnvelopeDataPagingAndSortingRepository;
import com.ds.proserv.appdata.transformer.ApplicationEnvelopeDataTransformer;
import com.ds.proserv.appdata.transformer.PageableTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeInformation;
import com.ds.proserv.feign.appdata.service.ApplicationEnvelopeService;
import com.ds.proserv.feign.domain.DateRange;
import com.ds.proserv.feign.util.DateRangeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ApplicationEnvelopeController implements ApplicationEnvelopeService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private ApplicationEnvelopeDataTransformer applicationEnvelopeDataTransformer;

	@Autowired
	private ApplicationEnvelopeDataPagingAndSortingRepository applicationEnvelopeDataPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<ApplicationEnvelopeDefinition> save(
			ApplicationEnvelopeDefinition applicationEnvelopeDefinition) {

		log.info("Saving the data for applicationId -> {}", applicationEnvelopeDefinition.getApplicationId());
		ApplicationEnvelopeData applicationEnvelopeData = applicationEnvelopeDataTransformer
				.transformToApplicationEnvelopeData(applicationEnvelopeDefinition);
		ApplicationEnvelopeData savedApplicationEnvelopeData = applicationEnvelopeDataPagingAndSortingRepository
				.save(applicationEnvelopeData);

		return new ResponseEntity<ApplicationEnvelopeDefinition>(applicationEnvelopeDataTransformer
				.transformToApplicationEnvelopeDefinition(savedApplicationEnvelopeData), HttpStatus.CREATED);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<ApplicationEnvelopeInformation> bulkSave(
			ApplicationEnvelopeInformation applicationEnvelopeInformation) {

		log.info("Saving the data for processId -> {}", applicationEnvelopeInformation.getProcessId());
		List<ApplicationEnvelopeDefinition> applicationEnvelopeDefinitions = applicationEnvelopeInformation
				.getApplicationEnvelopeDefinitions();

		List<ApplicationEnvelopeSPRequest> applicationEnvelopeSPRequestList = new ArrayList<ApplicationEnvelopeSPRequest>();

		if (null != applicationEnvelopeDefinitions && !applicationEnvelopeDefinitions.isEmpty()) {

			applicationEnvelopeDefinitions.forEach(applicationEnvelopeDefinition -> {

				applicationEnvelopeSPRequestList.add(applicationEnvelopeDataTransformer
						.transformToApplicationEnvelopeSPRequest(applicationEnvelopeDefinition));

			});
		}

		if (null != applicationEnvelopeSPRequestList && !applicationEnvelopeSPRequestList.isEmpty()) {

			try {

				String json = objectMapper.writeValueAsString(applicationEnvelopeSPRequestList);

				String result = applicationEnvelopeDataPagingAndSortingRepository.insert(json);

				if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

					throw new ResourceNotSavedException(
							"ApplicationEnvelopeData not saved for " + applicationEnvelopeInformation);
				}
			} catch (JsonProcessingException exp) {
				exp.printStackTrace();
				throw new ResourceNotSavedException(
						"ApplicationEnvelopeData not saved for " + applicationEnvelopeInformation);
			}

			ApplicationEnvelopeInformation savedApplicationEnvelopeInformation = new ApplicationEnvelopeInformation();
			savedApplicationEnvelopeInformation.setApplicationEnvelopeDefinitions(applicationEnvelopeDefinitions);
			savedApplicationEnvelopeInformation.setTotalRecords(Long.valueOf(applicationEnvelopeDefinitions.size()));

			return new ResponseEntity<ApplicationEnvelopeInformation>(savedApplicationEnvelopeInformation,
					HttpStatus.CREATED);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.NO_CONTENT);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByApplicationId(String applicationId) {

		log.info("Count By applicationId -> {}", applicationId);
		return new ResponseEntity<Long>(
				applicationEnvelopeDataPagingAndSortingRepository.countByApplicationId(applicationId), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByApplicationType(String applicationType) {

		log.info("Count By applicationType -> {}", applicationType);
		return new ResponseEntity<Long>(
				applicationEnvelopeDataPagingAndSortingRepository.countByApplicationType(applicationType),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByApplicationTypeAndEnvelopeIdIsNull(String applicationType) {

		log.info("Count By failed applicationType -> {}", applicationType);
		return new ResponseEntity<Long>(applicationEnvelopeDataPagingAndSortingRepository
				.countByApplicationTypeAndEnvelopeIdIsNull(applicationType), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findAllByApplicationTypeAndEnvelopeIdIsNull(
			String applicationType, PageInformation pageInformation) {

		log.info("FindAll By failed applicationType -> {}", applicationType);
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		Slice<ApplicationEnvelopeData> applicationEnvelopeDataSlice = applicationEnvelopeDataPagingAndSortingRepository
				.findAllByApplicationTypeAndEnvelopeIdIsNull(applicationType, pageable);

		return new ResponseEntity<ApplicationEnvelopeInformation>(
				prepareResponseFromSlice(applicationEnvelopeDataSlice), HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findByApplicationIdIn(PageInformation pageInformation) {

		log.info("FindAll By applicationIds for pageInformation -> {}", pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> applicationIds = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONIDS_PARAM_NAME);

			List<ApplicationEnvelopeData> applicationEnvelopeDataList = applicationEnvelopeDataPagingAndSortingRepository
					.findAllByApplicationIdIn(applicationIds);

			if (null == applicationEnvelopeDataList || applicationEnvelopeDataList.isEmpty()) {

				return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.NO_CONTENT);
			}

			return prepareResponseData(applicationEnvelopeDataList);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findByEnvelopeIdIn(PageInformation pageInformation) {

		log.info("FindAll By envelopeIds for pageInformation -> {}", pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> envelopeIds = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.ENVELOPEIDS_PARAM_NAME);

			List<ApplicationEnvelopeData> applicationEnvelopeDataList = applicationEnvelopeDataPagingAndSortingRepository
					.findAllByEnvelopeIdIn(envelopeIds);

			if (null == applicationEnvelopeDataList || applicationEnvelopeDataList.isEmpty()) {

				return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.NO_CONTENT);
			}

			return prepareResponseData(applicationEnvelopeDataList);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findByRecipientEmailsContainingIgnoreCase(
			String recipientEmail) {

		log.info("FindAll By recipientEmails -> {}", recipientEmail);
		List<ApplicationEnvelopeData> applicationEnvelopeDataList = applicationEnvelopeDataPagingAndSortingRepository
				.findAllByRecipientEmailsContainingIgnoreCase(recipientEmail);

		ApplicationEnvelopeInformation applicationEnvelopeInformation = new ApplicationEnvelopeInformation();
		if (null != applicationEnvelopeDataList && !applicationEnvelopeDataList.isEmpty()) {

			List<ApplicationEnvelopeDefinition> savedApplicationEnvelopeDefinitions = new ArrayList<ApplicationEnvelopeDefinition>(
					applicationEnvelopeDataList.size());

			applicationEnvelopeDataList.forEach(applicationEnvelopeData -> {

				savedApplicationEnvelopeDefinitions.add(applicationEnvelopeDataTransformer
						.transformToApplicationEnvelopeDefinition(applicationEnvelopeData));
			});

			applicationEnvelopeInformation.setApplicationEnvelopeDefinitions(savedApplicationEnvelopeDefinitions);
			applicationEnvelopeInformation.setTotalRecords(Long.valueOf(savedApplicationEnvelopeDefinitions.size()));

			return new ResponseEntity<ApplicationEnvelopeInformation>(applicationEnvelopeInformation, HttpStatus.OK);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByEnvelopeSentTimestampBetweenAndApplicationType(String fromDate, String toDate,
			String applicationType) {

		log.info("Count By dates {}, {} and applicationType -> {}", fromDate, toDate, applicationType);
		DateRange dateRange = DateRangeUtil.createDateRange(fromDate, toDate);

		return new ResponseEntity<Long>(
				applicationEnvelopeDataPagingAndSortingRepository.countByEnvelopeSentTimestampBetweenAndApplicationType(
						dateRange.getStartDateTime(), dateRange.getEndDateTime(), applicationType),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findAllByEnvelopeSentTimestampBetweenAndApplicationType(
			PageInformation pageInformation) {

		log.info("FindAll successful applications with pageInformation -> {}", pageInformation);

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			String applicationType = DSUtil.extractPageQueryParamValue(pageQueryParams,
					AppConstants.APPLICATIONTYPE_PARAM_NAME);

			String fromDateTime = DSUtil.extractPageQueryParamValue(pageQueryParams,
					AppConstants.FROMDATETIME_PARAM_NAME);

			String toDateTime = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.TODATETIME_PARAM_NAME);

			DateRange dateRange = DateRangeUtil.createDateRange(fromDateTime, toDateTime);

			Slice<ApplicationEnvelopeData> applicationEnvelopeDataSlice = applicationEnvelopeDataPagingAndSortingRepository
					.findAllByEnvelopeSentTimestampBetweenAndApplicationType(dateRange.getStartDateTime(),
							dateRange.getEndDateTime(), applicationType, pageable);

			return new ResponseEntity<ApplicationEnvelopeInformation>(
					prepareResponseFromSlice(applicationEnvelopeDataSlice), HttpStatus.OK);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countByFailureTimestampBetweenAndApplicationType(String fromDate, String toDate,
			String applicationType) {

		log.info("Count Failures By dates {}, {} and applicationType -> {}", fromDate, toDate, applicationType);

		DateRange dateRange = DateRangeUtil.createDateRange(fromDate, toDate);

		return new ResponseEntity<Long>(
				applicationEnvelopeDataPagingAndSortingRepository.countByFailureTimestampBetweenAndApplicationType(
						dateRange.getStartDateTime(), dateRange.getEndDateTime(), applicationType),
				HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<ApplicationEnvelopeInformation> findAllByFailureTimestampBetweenAndApplicationType(
			PageInformation pageInformation) {

		log.info("FindAll failed applications with pageInformation -> {}", pageInformation);

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			String applicationType = DSUtil.extractPageQueryParamValue(pageQueryParams,
					AppConstants.APPLICATIONTYPE_PARAM_NAME);

			String fromDateTime = DSUtil.extractPageQueryParamValue(pageQueryParams,
					AppConstants.FROMDATETIME_PARAM_NAME);

			String toDateTime = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.TODATETIME_PARAM_NAME);

			DateRange dateRange = DateRangeUtil.createDateRange(fromDateTime, toDateTime);

			Slice<ApplicationEnvelopeData> applicationEnvelopeDataSlice = applicationEnvelopeDataPagingAndSortingRepository
					.findAllByFailureTimestampBetweenAndApplicationType(dateRange.getStartDateTime(),
							dateRange.getEndDateTime(), applicationType, pageable);

			return new ResponseEntity<ApplicationEnvelopeInformation>(
					prepareResponseFromSlice(applicationEnvelopeDataSlice), HttpStatus.OK);
		} else {

			return new ResponseEntity<ApplicationEnvelopeInformation>(HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<ApplicationEnvelopeInformation> prepareResponseData(
			List<ApplicationEnvelopeData> applicationEnvelopeDataList) {
		List<ApplicationEnvelopeDefinition> savedApplicationEnvelopeDefinitions = new ArrayList<ApplicationEnvelopeDefinition>();
		applicationEnvelopeDataList.forEach(applicationEnvelopeData -> {

			savedApplicationEnvelopeDefinitions.add(applicationEnvelopeDataTransformer
					.transformToApplicationEnvelopeDefinition(applicationEnvelopeData));
		});

		ApplicationEnvelopeInformation savedApplicationEnvelopeInformation = new ApplicationEnvelopeInformation();
		savedApplicationEnvelopeInformation.setApplicationEnvelopeDefinitions(savedApplicationEnvelopeDefinitions);
		savedApplicationEnvelopeInformation.setTotalRecords(Long.valueOf(savedApplicationEnvelopeDefinitions.size()));

		return new ResponseEntity<ApplicationEnvelopeInformation>(savedApplicationEnvelopeInformation, HttpStatus.OK);
	}

	private ApplicationEnvelopeInformation prepareResponseFromSlice(
			Slice<ApplicationEnvelopeData> applicationEnvelopeDataSlice) {

		log.info("Prepared Respose from Slice -> {}", applicationEnvelopeDataSlice);

		ApplicationEnvelopeInformation applicationEnvelopeInformation = new ApplicationEnvelopeInformation();
		if (null != applicationEnvelopeDataSlice && !applicationEnvelopeDataSlice.isEmpty()
				&& applicationEnvelopeDataSlice.hasContent()) {

			List<ApplicationEnvelopeDefinition> applicationEnvelopeDefinitionList = new ArrayList<ApplicationEnvelopeDefinition>();
			applicationEnvelopeDataSlice.getContent().forEach(applicationEnvelopeData -> {

				applicationEnvelopeDefinitionList.add(applicationEnvelopeDataTransformer
						.transformToApplicationEnvelopeDefinition(applicationEnvelopeData));
			});

			applicationEnvelopeInformation.setCurrentPage(Long.valueOf(applicationEnvelopeDataSlice.getNumber()));
			applicationEnvelopeInformation.setNextAvailable(applicationEnvelopeDataSlice.hasNext());
			applicationEnvelopeInformation.setContentAvailable(true);
			applicationEnvelopeInformation.setApplicationEnvelopeDefinitions(applicationEnvelopeDefinitionList);
			applicationEnvelopeInformation.setTotalRecords(Long.valueOf(applicationEnvelopeDefinitionList.size()));

		} else {

			applicationEnvelopeInformation.setContentAvailable(false);
			applicationEnvelopeInformation.setApplicationEnvelopeDefinitions(null);
			applicationEnvelopeInformation.setTotalRecords(0L);
		}

		return applicationEnvelopeInformation;
	}

}