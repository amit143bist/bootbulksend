package com.ds.proserv.appdata.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
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

import com.ds.proserv.appdata.model.CustomEnvelopeData;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataBucketNameProjection;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataCountDateProjection;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataIdProjection;
import com.ds.proserv.appdata.repository.CustomEnvelopeDataPagingAndSortingRepository;
import com.ds.proserv.appdata.service.EnvelopeDataHelperService;
import com.ds.proserv.appdata.transformer.CustomEnvelopeDataTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountBucketNameInformation;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountBucketNameResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountDateInformation;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountDateResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataDefinition;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdRequest;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataInformation;
import com.ds.proserv.feign.appdata.service.CustomEnvelopeDataService;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CustomEnvelopeDataController implements CustomEnvelopeDataService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EnvelopeDataHelperService envelopeDataHelperService;

	@Autowired
	private CustomEnvelopeDataTransformer customEnvelopeDataTransformer;

	@Autowired
	private CustomEnvelopeDataPagingAndSortingRepository customEnvelopeDataPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataIdResponse> findPendingDocDownloadEnvelopesByEnvelopeIds(
			CustomEnvelopeDataIdRequest customEnvelopeDataIdRequest) {

		Iterable<CustomEnvelopeDataIdProjection> envelopeIdIterable = customEnvelopeDataPagingAndSortingRepository
				.findAllByEnvelopeIdInAndDocDownloadStatusFlagIsNull(customEnvelopeDataIdRequest.getEnvelopeIds());

		CustomEnvelopeDataIdResponse customEnvelopeDataIdResponse = new CustomEnvelopeDataIdResponse();
		if (null != envelopeIdIterable && null != envelopeIdIterable.iterator()
				&& envelopeIdIterable.iterator().hasNext()) {

			List<String> envelopeIds = StreamSupport.stream(envelopeIdIterable.spliterator(), false)
					.map(CustomEnvelopeDataIdProjection::getEnvelopeId).map(String::trim).map(String::toLowerCase)
					.collect(Collectors.toList());

			customEnvelopeDataIdResponse.setTotalRecords(Long.valueOf(envelopeIds.size()));
			customEnvelopeDataIdResponse.setEnvelopeIds(envelopeIds);

			return new ResponseEntity<CustomEnvelopeDataIdResponse>(customEnvelopeDataIdResponse, HttpStatus.OK);
		} else {

			customEnvelopeDataIdResponse.setTotalRecords(0L);
			return new ResponseEntity<CustomEnvelopeDataIdResponse>(customEnvelopeDataIdResponse,
					HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<CustomEnvelopeDataDefinition> saveEnvelopeData(
			CustomEnvelopeDataDefinition customEnvelopeDataDefinition) {

		log.info("Saving CustomEnvelopeData for envelopeId -> {}", customEnvelopeDataDefinition.getEnvelopeId());

		CustomEnvelopeData checkSavedCustomEnvelopeData = null;
		if (envelopeDataHelperService.isCustomEnvDataFetchByIdSPEnabled()) {

			checkSavedCustomEnvelopeData = customEnvelopeDataPagingAndSortingRepository
					.getCustomEnvelopeDataById(customEnvelopeDataDefinition.getEnvelopeId());
		} else {

			checkSavedCustomEnvelopeData = customEnvelopeDataPagingAndSortingRepository
					.findById(customEnvelopeDataDefinition.getEnvelopeId()).orElse(null);
		}

		if (null != checkSavedCustomEnvelopeData && !StringUtils.isEmpty(checkSavedCustomEnvelopeData.getCreatedBy())) {

			return new ResponseEntity<CustomEnvelopeDataDefinition>(
					customEnvelopeDataTransformer.transformToCustomEnvelopeDataDefinition(checkSavedCustomEnvelopeData),
					HttpStatus.ALREADY_REPORTED);
		}

		return Optional
				.ofNullable(customEnvelopeDataPagingAndSortingRepository.save(
						customEnvelopeDataTransformer.transformToCustomEnvelopeData(customEnvelopeDataDefinition)))
				.map(savedCustomEnvelopeData -> {

					Assert.notNull(savedCustomEnvelopeData.getCreatedDateTime(),
							"CreatedDateTime cannot be null for envelopeId "
									+ customEnvelopeDataDefinition.getEnvelopeId());

					return new ResponseEntity<CustomEnvelopeDataDefinition>(customEnvelopeDataTransformer
							.transformToCustomEnvelopeDataDefinition(savedCustomEnvelopeData), HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException(
						"CustomEnvelopeData not saved for " + customEnvelopeDataDefinition.getEnvelopeId()));

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<CustomEnvelopeDataInformation> bulkSaveEnvelopeData(
			CustomEnvelopeDataInformation customEnvelopeDataInformation) {

		log.info("Saving bulkCustomEnvelopeData for processId -> {}", customEnvelopeDataInformation.getProcessId());
		List<CustomEnvelopeDataDefinition> customEnvelopeDataDefinitionList = customEnvelopeDataInformation
				.getCustomEnvelopeDataDefinitions();

		CustomEnvelopeDataInformation customEnvelopeDataInformationSaved = new CustomEnvelopeDataInformation();
		if (null != customEnvelopeDataDefinitionList && !customEnvelopeDataDefinitionList.isEmpty()) {

			List<String> envelopeIdList = customEnvelopeDataDefinitionList.stream()
					.map(CustomEnvelopeDataDefinition::getEnvelopeId).collect(Collectors.toList());

			Iterable<CustomEnvelopeData> savedCustomEnvelopeDataIterable = customEnvelopeDataPagingAndSortingRepository
					.findAllById(envelopeIdList);

			List<String> savedEnvelopeIdList = null;
			if (null != savedCustomEnvelopeDataIterable) {

				List<CustomEnvelopeData> savedCustomEnvelopeDataList = StreamSupport
						.stream(savedCustomEnvelopeDataIterable.spliterator(), false).collect(Collectors.toList());

				savedEnvelopeIdList = savedCustomEnvelopeDataList.stream().map(CustomEnvelopeData::getEnvelopeId)
						.collect(Collectors.toList());
			}

			List<CustomEnvelopeData> customEnvelopeDataList = new ArrayList<CustomEnvelopeData>(
					customEnvelopeDataDefinitionList.size());
			for (CustomEnvelopeDataDefinition customEnvelopeDataDefinition : customEnvelopeDataDefinitionList) {

				if (null == savedEnvelopeIdList || savedEnvelopeIdList.isEmpty()
						|| !savedEnvelopeIdList.contains(customEnvelopeDataDefinition.getEnvelopeId())) {

					customEnvelopeDataList.add(
							customEnvelopeDataTransformer.transformToCustomEnvelopeData(customEnvelopeDataDefinition));
				}
			}

			if (null != customEnvelopeDataList && !customEnvelopeDataList.isEmpty()) {

				Iterable<CustomEnvelopeData> customEnvelopeDataIterable = customEnvelopeDataPagingAndSortingRepository
						.saveAll(customEnvelopeDataList);

				List<CustomEnvelopeDataDefinition> customEnvelopeDataDefinitionListSaved = new ArrayList<CustomEnvelopeDataDefinition>();
				customEnvelopeDataIterable.forEach(customEnvelopeData -> {

					customEnvelopeDataDefinitionListSaved.add(
							customEnvelopeDataTransformer.transformToCustomEnvelopeDataDefinition(customEnvelopeData));
				});

				customEnvelopeDataInformationSaved
						.setTotalRecords(Long.valueOf(customEnvelopeDataDefinitionListSaved.size()));
				customEnvelopeDataInformationSaved
						.setCustomEnvelopeDataDefinitions(customEnvelopeDataDefinitionListSaved);

				return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformationSaved,
						HttpStatus.CREATED);
			} else {

				log.info("All envelopeIds already saved earlier for processId -> {}",
						customEnvelopeDataInformation.getProcessId());
				return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformationSaved,
						HttpStatus.ALREADY_REPORTED);
			}

		} else {

			return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformationSaved,
					HttpStatus.NO_CONTENT);
		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<CustomEnvelopeDataDefinition> updateEnvelopeData(
			CustomEnvelopeDataDefinition customEnvelopeDataDefinition) {

		log.info("Updating CustomEnvelopeData for envelopeId -> {}", customEnvelopeDataDefinition.getEnvelopeId());
		return customEnvelopeDataPagingAndSortingRepository.findById(customEnvelopeDataDefinition.getEnvelopeId())
				.map(savedCustomEnvelopeData -> {

					return new ResponseEntity<CustomEnvelopeDataDefinition>(
							customEnvelopeDataTransformer.transformToCustomEnvelopeDataDefinition(
									customEnvelopeDataPagingAndSortingRepository
											.save(customEnvelopeDataTransformer.transformToCustomEnvelopeDataAsUpdate(
													customEnvelopeDataDefinition, savedCustomEnvelopeData))),
							HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No CustomEnvelopeData found for envelopeId# " + customEnvelopeDataDefinition.getEnvelopeId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<String> updateCustomEnvelopeDataProcessStatusStartTime(PageInformation pageInformation) {

		List<String> customEnvelopeDataList = DSUtil
				.extractFirstPageQueryParamValueAsList(pageInformation.getPageQueryParams());

		if (envelopeDataHelperService.isUpdateProcessStatusStartDateTimeBySPEnabled()) {

			customEnvelopeDataPagingAndSortingRepository
					.updateCustomEnvelopeDataProcessStatusStartTimeBySP(createRecordIdJSON(customEnvelopeDataList));
		} else {

			LocalDateTime envProcessStartDateTime = LocalDateTime.now();
			customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataProcessStatusStartTime(
					ProcessStatus.INPROGRESS.toString(), envProcessStartDateTime, customEnvelopeDataList);
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<String> updateCustomEnvelopeDataProcessStatusEndTime(PageInformation pageInformation) {

		List<String> customEnvelopeDataList = DSUtil
				.extractFirstPageQueryParamValueAsList(pageInformation.getPageQueryParams());

		if (envelopeDataHelperService.isUpdateProcessStatusEndDateTimeBySPEnabled()) {

			customEnvelopeDataPagingAndSortingRepository
					.updateCustomEnvelopeDataProcessStatusEndTimeBySP(createRecordIdJSON(customEnvelopeDataList));
		} else {

			LocalDateTime envProcessEndDateTime = LocalDateTime.now();
			customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataProcessStatusEndTime(
					ProcessStatus.COMPLETED.toString(), envProcessEndDateTime, customEnvelopeDataList);
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<String> updateCustomEnvelopeDataDocDownloadStatusEndTime(PageInformation pageInformation) {

		List<String> customEnvelopeDataList = DSUtil
				.extractFirstPageQueryParamValueAsList(pageInformation.getPageQueryParams());

		if (envelopeDataHelperService.isUpdateDocDownloadEndDateTimeBySPEnabled()) {

			customEnvelopeDataPagingAndSortingRepository
					.updateCustomEnvelopeDataDocDownloadStatusEndTimeBySP(createRecordIdJSON(customEnvelopeDataList));
		} else {

			LocalDateTime docDownloadTimeStamp = LocalDateTime.now();
			customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataDocDownloadStatusEndTime(
					ProcessStatus.COMPLETED.toString(), docDownloadTimeStamp, customEnvelopeDataList);
		}
		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataInformation> findEnvelopesByDateRange(String fromDate, String toDate,
			String status, Integer count, Integer pageNumber) {

		CustomEnvelopeDataInformation customEnvelopeDataInformation = processCustomDataByParams(fromDate, toDate,
				status, count, pageNumber, null, false);

		return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataInformation> findEnvelopesBySenderIdentifierAndDateRange(
			String senderIdentifier, String fromDate, String toDate, String status, Integer count, Integer pageNumber) {

		CustomEnvelopeDataInformation customEnvelopeDataInformation = processCustomDataByParams(fromDate, toDate,
				status, count, pageNumber, senderIdentifier, false);

		return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CustomEnvelopeDataInformation> findAndUpdateEnvelopesBySenderIdentifierAndDateRange(
			String senderIdentifier, String fromDate, String toDate, String status, Integer count, Integer pageNumber) {

		return fetchAndUpdateData(fromDate, toDate, status, count, pageNumber, senderIdentifier, true);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<CustomEnvelopeDataInformation> findAndUpdateEnvelopesByDateRange(String fromDate,
			String toDate, String status, Integer count, Integer pageNumber) {

		return fetchAndUpdateData(fromDate, toDate, status, count, pageNumber, null, true);
	}

	private ResponseEntity<CustomEnvelopeDataInformation> fetchAndUpdateData(String fromDate, String toDate,
			String status, Integer count, Integer pageNumber, String senderIdentifier, boolean putFlow) {

		CustomEnvelopeDataInformation customEnvelopeDataInformation = processCustomDataByParams(fromDate, toDate,
				status, count, pageNumber, senderIdentifier, putFlow);

		if (customEnvelopeDataInformation.getContentAvailable()
				&& null != customEnvelopeDataInformation.getCustomEnvelopeDataDefinitions()
				&& !customEnvelopeDataInformation.getCustomEnvelopeDataDefinitions().isEmpty()) {

			List<String> customEnvelopeDataList = customEnvelopeDataInformation.getCustomEnvelopeDataDefinitions()
					.stream()
					.collect(Collectors.mapping(CustomEnvelopeDataDefinition::getEnvelopeId, Collectors.toList()));

			if (envelopeDataHelperService.isUpdateProcessStatusStartDateTimeBySPEnabled()) {

				customEnvelopeDataPagingAndSortingRepository
						.updateCustomEnvelopeDataProcessStatusStartTimeBySP(createRecordIdJSON(customEnvelopeDataList));
			} else {

				customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataProcessStatusStartTime(
						ProcessStatus.INPROGRESS.toString(), LocalDateTime.now(), customEnvelopeDataList);
			}

		}

		return new ResponseEntity<CustomEnvelopeDataInformation>(customEnvelopeDataInformation, HttpStatus.OK);
	}

	private CustomEnvelopeDataInformation processCustomDataByParams(String fromDate, String toDate, String status,
			Integer count, Integer pageNumber, String senderIdentifier, boolean putFlow) {

		ProcessStatus processStatusEnum = EnumUtils.getEnum(ProcessStatus.class, status.toUpperCase());

		Pageable pageable = PageRequest.of(pageNumber, count);

		LocalDateTime startDateTime = null;
		LocalDateTime OrStartDateTime = null;
		if (DateTimeUtil.isValidDateTimeByPatternNano(fromDate)) {

			startDateTime = LocalDateTime.parse(fromDate,
					DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
			OrStartDateTime = LocalDateTime.parse(fromDate,
					DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
		} else {

			startDateTime = LocalDateTime.parse(fromDate);
			OrStartDateTime = LocalDateTime.parse(fromDate);
		}

		LocalDateTime endDateTime = null;
		LocalDateTime OrEndDateTime = null;
		if (DateTimeUtil.isValidDateTimeByPatternNano(toDate)) {

			endDateTime = LocalDateTime.parse(toDate, DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
			OrEndDateTime = LocalDateTime.parse(toDate,
					DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
		} else {

			endDateTime = LocalDateTime.parse(toDate);
			OrEndDateTime = LocalDateTime.parse(toDate);
		}

		Slice<CustomEnvelopeData> customEnvelopeDataSlice = null;
		CustomEnvelopeDataInformation customEnvelopeDataInformation = new CustomEnvelopeDataInformation();
		switch (processStatusEnum) {

		case ALL:

			List<String> envProcessStatusFlags = new ArrayList<String>();
			envProcessStatusFlags.add(ProcessStatus.COMPLETED.toString());
			envProcessStatusFlags.add(ProcessStatus.INPROGRESS.toString());

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagInOrEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(
								startDateTime, endDateTime, senderIdentifier, envProcessStatusFlags, OrStartDateTime,
								OrEndDateTime, senderIdentifier, pageable);
			} else {
				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagInOrEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(
								startDateTime, endDateTime, envProcessStatusFlags, OrStartDateTime, OrEndDateTime,
								pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		case EMPTY:

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(OrStartDateTime,
								OrEndDateTime, senderIdentifier, pageable);
			} else {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(OrStartDateTime, OrEndDateTime,
								pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		case NOTEMPTY:

			List<String> envProcessStatusNotEmptyFlags = new ArrayList<String>();
			envProcessStatusNotEmptyFlags.add(ProcessStatus.COMPLETED.toString());
			envProcessStatusNotEmptyFlags.add(ProcessStatus.INPROGRESS.toString());

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIn(OrStartDateTime,
								OrEndDateTime, senderIdentifier, envProcessStatusNotEmptyFlags, pageable);
			} else {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagIn(OrStartDateTime, OrEndDateTime,
								envProcessStatusNotEmptyFlags, pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		case COMPLETED:

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlag(OrStartDateTime,
								OrEndDateTime, senderIdentifier, status.toUpperCase(), pageable);
			} else {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlag(OrStartDateTime, OrEndDateTime,
								status.toUpperCase(), pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		case INPROGRESS:

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlag(OrStartDateTime,
								OrEndDateTime, senderIdentifier, status.toUpperCase(), pageable);

			} else {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlag(OrStartDateTime, OrEndDateTime,
								status.toUpperCase(), pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		case NOTCOMPLETED:

			if (!StringUtils.isEmpty(senderIdentifier)) {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagOrEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(
								startDateTime, endDateTime, senderIdentifier, ProcessStatus.INPROGRESS.toString(),
								OrStartDateTime, OrEndDateTime, senderIdentifier, pageable);
			} else {

				customEnvelopeDataSlice = customEnvelopeDataPagingAndSortingRepository
						.findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagOrEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(
								startDateTime, endDateTime, ProcessStatus.INPROGRESS.toString(), OrStartDateTime,
								OrEndDateTime, pageable);
			}

			prepareResponseFromSlice(customEnvelopeDataInformation, customEnvelopeDataSlice, fromDate, toDate, status,
					count, pageNumber, senderIdentifier, putFlow);
			break;

		default:

			log.error(
					":::::::::::::::::::: Invalid ProcessStatus in findEnvelopeByDateRange() -> {} ::::::::::::::::::::",
					status);
		}
		return customEnvelopeDataInformation;
	}

	private void prepareResponseFromSlice(CustomEnvelopeDataInformation customEnvelopeDataInformation,
			Slice<CustomEnvelopeData> customEnvelopeDataSlice, String fromDate, String toDate, String status,
			Integer count, Integer lastPageNumber, String senderIdentifier, boolean putFlow) {

		log.info("Prepared Response from Slice -> {}", customEnvelopeDataSlice);

		if (null != customEnvelopeDataSlice && !customEnvelopeDataSlice.isEmpty()
				&& customEnvelopeDataSlice.hasContent()) {

			List<CustomEnvelopeDataDefinition> customEnvelopeDataDefinitionList = new ArrayList<CustomEnvelopeDataDefinition>();
			customEnvelopeDataSlice.getContent().forEach(customEnvelopeData -> {

				customEnvelopeDataDefinitionList
						.add(customEnvelopeDataTransformer.transformToCustomEnvelopeDataDefinition(customEnvelopeData));
			});

			if (customEnvelopeDataSlice.hasNext()) {

				String startNextUri = "/docusign/customdata";

				if (!StringUtils.isEmpty(senderIdentifier)) {

					startNextUri = startNextUri + "/senderidentifier/" + senderIdentifier;
				}

				String fullNextUri = startNextUri + "/fromdate/" + fromDate + "/todate/" + toDate + "/status/" + status
						+ "/count/" + count + "/pagenumber/" + (putFlow ? lastPageNumber : (lastPageNumber + 1));
				customEnvelopeDataInformation.setNextUri(fullNextUri);
			} else {

				customEnvelopeDataInformation.setNextUri(null);
			}

			customEnvelopeDataInformation.setTotalRecords(Long.valueOf(customEnvelopeDataDefinitionList.size()));
			customEnvelopeDataInformation.setCurrentPage(Long.valueOf(customEnvelopeDataSlice.getNumber()));
			customEnvelopeDataInformation.setNextAvailable(customEnvelopeDataSlice.hasNext());
			customEnvelopeDataInformation.setContentAvailable(true);
			customEnvelopeDataInformation.setCustomEnvelopeDataDefinitions(customEnvelopeDataDefinitionList);

		} else {

			customEnvelopeDataInformation.setNextUri(null);
			customEnvelopeDataInformation.setContentAvailable(false);
			customEnvelopeDataInformation.setCustomEnvelopeDataDefinitions(null);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataCountDateInformation> findAllDownloadedEnvelopesCountByDate() {

		log.info("Inside findAllDownloadedEnvelopesCountByDate");
		List<CustomEnvelopeDataCountDateProjection> countByDateList = customEnvelopeDataPagingAndSortingRepository
				.findAllDownloadedEnvelopesCountByDate();

		if (null != countByDateList && !countByDateList.isEmpty()) {

			List<CustomEnvelopeDataCountDateResponse> customEnvelopeDataCountDateList = new ArrayList<CustomEnvelopeDataCountDateResponse>(
					countByDateList.size());
			countByDateList.forEach(countByDate -> {

				customEnvelopeDataCountDateList
						.add(customEnvelopeDataTransformer.transformToCustomEnvelopeDataCountDateResponse(countByDate));
			});

			CustomEnvelopeDataCountDateInformation customEnvelopeDataCountDateInformation = new CustomEnvelopeDataCountDateInformation();
			customEnvelopeDataCountDateInformation
					.setTotalRecords(Long.valueOf(customEnvelopeDataCountDateList.size()));
			customEnvelopeDataCountDateInformation
					.setCustomEnvelopeDataCountDateResponses(customEnvelopeDataCountDateList);

			return new ResponseEntity<CustomEnvelopeDataCountDateInformation>(customEnvelopeDataCountDateInformation,
					HttpStatus.OK);
		}
		return new ResponseEntity<CustomEnvelopeDataCountDateInformation>(HttpStatus.NO_CONTENT);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<String> updateCustomEnvelopeDataDocDownloadStatusEndTimeWithBucketName(
			PageInformation pageInformation) {

		List<String> customEnvelopeDataList = DSUtil.extractPageQueryParamValueAsList(
				pageInformation.getPageQueryParams(), AppConstants.ENVELOPEIDS_PARAM_NAME);

		String bucketName = DSUtil.extractPageQueryParamValue(pageInformation.getPageQueryParams(),
				AppConstants.BUCKET_PARAM_NAME);

		if (envelopeDataHelperService.isUpdateDocDownloadEndDateTimeAndBucketNameBySPEnabled()) {

			customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataDocDownloadStatusEndTimeBucketNameBySP(
					createRecordIdJSON(customEnvelopeDataList), bucketName);

		} else {

			LocalDateTime docDownloadTimeStamp = LocalDateTime.now();
			customEnvelopeDataPagingAndSortingRepository.updateCustomEnvelopeDataDocDownloadStatusEndTimeByBucketName(
					ProcessStatus.COMPLETED.toString(), docDownloadTimeStamp, bucketName, customEnvelopeDataList);
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	private String createRecordIdJSON(List<String> customEnvelopeDataList) {

		List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

		for (String recordId : customEnvelopeDataList) {

			ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
			processSPDefinition.setRecordId(recordId);

			processSPDefinitionList.add(processSPDefinition);
		}

		String recordIdsAsJSON;
		try {

			recordIdsAsJSON = objectMapper.writeValueAsString(processSPDefinitionList);

			if (log.isDebugEnabled()) {

				log.debug("calling SP sucessfully with recordIdsAsJSON -> {}", recordIdsAsJSON);
			}
		} catch (JsonProcessingException e) {

			e.printStackTrace();
			throw new ResourceNotSavedException(e.getMessage());
		}
		return recordIdsAsJSON;
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataCountBucketNameInformation> findAllDownloadedEnvelopesCountByBucketName(
			String bucketName) {

		log.info("Inside findAllDownloadedEnvelopesCountByBucketName");
		List<CustomEnvelopeDataBucketNameProjection> countByBucketNameList = null;

		if (envelopeDataHelperService.isCustomEnvDataFetchBySPEnabled()) {

			if (log.isDebugEnabled()) {

				log.debug("Fetching CustomEnvData via SP for bucketName -> {}", bucketName);
			}
			countByBucketNameList = customEnvelopeDataPagingAndSortingRepository
					.getAllDownloadedEnvelopesCountByBucketName(bucketName);
		} else {

			countByBucketNameList = customEnvelopeDataPagingAndSortingRepository
					.findAllDownloadedEnvelopesCountByBucketName(bucketName);
		}

		if (null != countByBucketNameList && !countByBucketNameList.isEmpty()) {

			List<CustomEnvelopeDataCountBucketNameResponse> customEnvelopeDataCountBucketNameList = new ArrayList<CustomEnvelopeDataCountBucketNameResponse>(
					countByBucketNameList.size());
			countByBucketNameList.forEach(countByBucketName -> {

				customEnvelopeDataCountBucketNameList.add(customEnvelopeDataTransformer
						.transformToCustomEnvelopeDataCountBucketNameResponse(countByBucketName));
			});

			CustomEnvelopeDataCountBucketNameInformation customEnvelopeDataCountBucketNameInformation = new CustomEnvelopeDataCountBucketNameInformation();
			customEnvelopeDataCountBucketNameInformation
					.setTotalRecords(Long.valueOf(customEnvelopeDataCountBucketNameList.size()));
			customEnvelopeDataCountBucketNameInformation
					.setCustomEnvelopeDataCountBucketNameResponses(customEnvelopeDataCountBucketNameList);

			return new ResponseEntity<CustomEnvelopeDataCountBucketNameInformation>(
					customEnvelopeDataCountBucketNameInformation, HttpStatus.OK);
		}

		return new ResponseEntity<CustomEnvelopeDataCountBucketNameInformation>(HttpStatus.NO_CONTENT);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<CustomEnvelopeDataIdResponse> findAllByDownloadBucketName(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		String bucketName = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.BUCKET_PARAM_NAME);
		int pageNumber = Integer
				.parseInt(DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.PAGENUMBER_PARAM_NAME));
		int paginationLimit = Integer
				.parseInt(DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.PAGINATIONLIMIT_PARAM_NAME));

		Pageable pageable = PageRequest.of(pageNumber, paginationLimit);

		Slice<CustomEnvelopeDataIdProjection> customEnvelopeDataIdProjectionSlice = customEnvelopeDataPagingAndSortingRepository
				.findAllByDownloadBucketName(bucketName, pageable);

		CustomEnvelopeDataIdResponse customEnvelopeDataIdResponse = new CustomEnvelopeDataIdResponse();
		if (null != customEnvelopeDataIdProjectionSlice && customEnvelopeDataIdProjectionSlice.hasContent()) {

			List<String> envelopeIds = StreamSupport.stream(customEnvelopeDataIdProjectionSlice.spliterator(), false)
					.map(CustomEnvelopeDataIdProjection::getEnvelopeId).map(String::trim).map(String::toLowerCase)
					.collect(Collectors.toList());

			customEnvelopeDataIdResponse.setCurrentPage(Long.valueOf(customEnvelopeDataIdProjectionSlice.getNumber()));
			customEnvelopeDataIdResponse.setTotalRecords(Long.valueOf(envelopeIds.size()));
			customEnvelopeDataIdResponse.setEnvelopeIds(envelopeIds);
			customEnvelopeDataIdResponse.setNextAvailable(customEnvelopeDataIdProjectionSlice.hasNext());

			return new ResponseEntity<CustomEnvelopeDataIdResponse>(customEnvelopeDataIdResponse, HttpStatus.OK);
		} else {

			log.info("No content available for bucketName -> {}", bucketName);
			customEnvelopeDataIdResponse.setTotalRecords(0L);
			return new ResponseEntity<CustomEnvelopeDataIdResponse>(customEnvelopeDataIdResponse,
					HttpStatus.NO_CONTENT);
		}
	}

}