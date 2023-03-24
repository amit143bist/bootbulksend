package com.ds.proserv.bulksenddata.controller;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.bulksenddata.model.BulkSendRecordLog;
import com.ds.proserv.bulksenddata.model.BulkSendRecordLogId;
import com.ds.proserv.bulksenddata.repository.BulkSendRecordLogPagingAndSortingRepository;
import com.ds.proserv.bulksenddata.transformer.BulkSendRecordLogTransformer;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogDefinition;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogInformation;
import com.ds.proserv.feign.bulksenddata.service.BulkSendRecordLogService;
import com.ds.proserv.feign.domain.DateRange;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.ds.proserv.feign.util.DateRangeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendRecordLogController implements BulkSendRecordLogService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private BulkSendRecordLogTransformer bulkSendRecordLogTransformer;

	@Autowired
	private BulkSendRecordLogPagingAndSortingRepository bulkSendRecordLogPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public ResponseEntity<BulkSendRecordLogInformation> bulkSaveBulkSendRecordLogs(
			BulkSendRecordLogInformation bulkSendRecordLogInformation) {

		log.info("BulkSave initiated for processId -> {} and batchId -> {}",
				bulkSendRecordLogInformation.getProcessId(), bulkSendRecordLogInformation.getBatchId());
		List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions = bulkSendRecordLogInformation
				.getBulkSendRecordLogDefinitions();

		List<BulkSendRecordLog> bulkSendRecordLogList = new ArrayList<BulkSendRecordLog>(
				bulkSendRecordLogDefinitions.size());
		bulkSendRecordLogDefinitions.forEach(bulkSendRecordLogDefinition -> {

			bulkSendRecordLogList
					.add(bulkSendRecordLogTransformer.transformToBulkSendRecordLog(bulkSendRecordLogDefinition));
		});

		Iterable<BulkSendRecordLog> savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
				.saveAll(bulkSendRecordLogList);

		List<BulkSendRecordLogDefinition> savedBulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>(
				bulkSendRecordLogDefinitions.size());

		BulkSendRecordLogInformation savedBulkSendRecordLogInformation = prepareResponse(
				savedBulkSendRecordLogDefinitions, savedBulkSendRecordLogs);

		return new ResponseEntity<BulkSendRecordLogInformation>(savedBulkSendRecordLogInformation, HttpStatus.CREATED);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogs(
			BulkSendRecordLogInformation bulkSendRecordLogInformation) {

		List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions = bulkSendRecordLogInformation
				.getBulkSendRecordLogDefinitions();

		List<BulkSendRecordLogId> bulkSendRecordLogIdList = new ArrayList<BulkSendRecordLogId>();
		bulkSendRecordLogDefinitions.forEach(bulkSendRecordLogDefinition -> {

			BulkSendRecordLogId bulkSendRecordLogId = new BulkSendRecordLogId(bulkSendRecordLogDefinition.getRecordId(),
					bulkSendRecordLogDefinition.getRecordType());
			bulkSendRecordLogIdList.add(bulkSendRecordLogId);
		});

		Iterable<BulkSendRecordLog> savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
				.findAllById(bulkSendRecordLogIdList);

		BulkSendRecordLogInformation savedBulkSendRecordLogInformation = prepareResponse(bulkSendRecordLogDefinitions,
				savedBulkSendRecordLogs);

		return new ResponseEntity<BulkSendRecordLogInformation>(savedBulkSendRecordLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogsByDateRange(String startDateTime,
			String endDateTime) {

		DateRange dateRange = DateRangeUtil.createDateRange(startDateTime, endDateTime);

		Iterable<BulkSendRecordLog> savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
				.findAllByStartDateTimeAndEndDateTime(dateRange.getStartDateTime(), dateRange.getEndDateTime());

		List<BulkSendRecordLogDefinition> savedBulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>();

		BulkSendRecordLogInformation savedBulkSendRecordLogInformation = prepareResponse(
				savedBulkSendRecordLogDefinitions, savedBulkSendRecordLogs);

		return new ResponseEntity<BulkSendRecordLogInformation>(savedBulkSendRecordLogInformation, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<BulkSendRecordLogInformation> bulkFindAllBulkSendRecordLogs(String recordType,
			PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		List<String> recordIds = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
				AppConstants.RECORDIDS_PARAM_NAME);
		// dsCacheManager
		Iterable<BulkSendRecordLog> savedBulkSendRecordLogs = null;

		if (isBulkRecordFetchBySPEnabled()) {

			if (log.isDebugEnabled()) {

				log.debug("Calling SP for recordType -> {}", recordType);
			}

			List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

			for (String recordId : recordIds) {

				ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
				processSPDefinition.setRecordId(recordId);

				processSPDefinitionList.add(processSPDefinition);
			}

			try {

				String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);
				savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
						.getAllRecordByRecordTypeAndRecordIds(recordType, spJSON);

				if (log.isDebugEnabled()) {

					log.debug("Calling SP for recordType -> {} with spJSON -> {} and result is {}", recordType, spJSON,
							savedBulkSendRecordLogs);
				}
			} catch (JsonProcessingException e) {

				e.printStackTrace();
			}

		} else {

			savedBulkSendRecordLogs = bulkSendRecordLogPagingAndSortingRepository
					.findAllByBulkSendRecordLogIdRecordTypeAndBulkSendRecordLogIdRecordIdIn(recordType, recordIds);
		}

		List<BulkSendRecordLogDefinition> savedBulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>();

		BulkSendRecordLogInformation savedBulkSendRecordLogInformation = prepareResponse(
				savedBulkSendRecordLogDefinitions, savedBulkSendRecordLogs);

		return new ResponseEntity<BulkSendRecordLogInformation>(savedBulkSendRecordLogInformation, HttpStatus.OK);
	}

	private boolean isBulkRecordFetchBySPEnabled() {

		String enableBulkRecordFetchBySP = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.BULKSENDRECORD_SELECTBYRECORDIDS_STOREDPROC);

		if (!StringUtils.isEmpty(enableBulkRecordFetchBySP)) {

			return Boolean.parseBoolean(enableBulkRecordFetchBySP);
		}

		return true;
	}

	private BulkSendRecordLogInformation prepareResponse(List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions,
			Iterable<BulkSendRecordLog> savedBulkSendRecordLogs) {

		if (null == savedBulkSendRecordLogs) {

			BulkSendRecordLogInformation savedBulkSendRecordLogInformation = new BulkSendRecordLogInformation();
			savedBulkSendRecordLogInformation.setTotalRecords(Long.valueOf(0L));
			return savedBulkSendRecordLogInformation;
		}

		List<BulkSendRecordLogDefinition> savedBulkSendRecordLogDefinitions = new ArrayList<BulkSendRecordLogDefinition>(
				bulkSendRecordLogDefinitions.size());

		savedBulkSendRecordLogs.forEach(savedBulkSendRecordLog -> {

			savedBulkSendRecordLogDefinitions
					.add(bulkSendRecordLogTransformer.transformToBulkSendRecordLogDefinition(savedBulkSendRecordLog));
		});

		BulkSendRecordLogInformation savedBulkSendRecordLogInformation = new BulkSendRecordLogInformation();
		savedBulkSendRecordLogInformation.setTotalRecords(Long.valueOf(savedBulkSendRecordLogDefinitions.size()));
		savedBulkSendRecordLogInformation.setBulkSendRecordLogDefinitions(savedBulkSendRecordLogDefinitions);
		return savedBulkSendRecordLogInformation;
	}

}