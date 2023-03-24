package com.ds.proserv.appdata.controller;

import java.time.LocalDateTime;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.appdata.model.FolderNotificationLog;
import com.ds.proserv.appdata.repository.FolderNotificationLogRepository;
import com.ds.proserv.appdata.transformer.FolderNotificationLogTransformer;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.appdata.domain.FolderNotificationDefinition;
import com.ds.proserv.feign.appdata.domain.FolderNotificationInformation;
import com.ds.proserv.feign.appdata.domain.FolderNotificationRequest;
import com.ds.proserv.feign.appdata.service.FolderNotificationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
public class FolderNotificationController implements FolderNotificationService {

	@Autowired
	private FolderNotificationLogTransformer folderNotificationLogTransformer;

	@Autowired
	private FolderNotificationLogRepository folderNotificationLogRepository;

	@Override
	public ResponseEntity<FolderNotificationInformation> bulkUpdateSaveFolderNotifications(
			FolderNotificationInformation folderNotificationInformation) {

		log.info("Inside bulkUpdateSaveFolderNotifications");
		List<FolderNotificationDefinition> folderNotificationDefinitions = folderNotificationInformation
				.getFolderNotificationDefinitions();

		List<FolderNotificationLog> folderNotificationLogs = new ArrayList<FolderNotificationLog>(
				folderNotificationDefinitions.size());
		folderNotificationDefinitions.forEach(folderNotificationDefinition -> {

			folderNotificationLogs.add(
					folderNotificationLogTransformer.transformToFolderNotificationLog(folderNotificationDefinition));
		});

		if (null != folderNotificationLogs && !folderNotificationLogs.isEmpty()) {

			Iterable<FolderNotificationLog> savedFolderLogs = folderNotificationLogRepository
					.saveAll(folderNotificationLogs);

			List<FolderNotificationDefinition> savedFolderNotificationDefinitions = new ArrayList<FolderNotificationDefinition>();
			savedFolderLogs.forEach(folderNotificationLog -> {

				savedFolderNotificationDefinitions.add(folderNotificationLogTransformer
						.transformToFolderNotificationDefinition(folderNotificationLog));
			});

			FolderNotificationInformation savedFolderNotificationInformation = new FolderNotificationInformation();
			savedFolderNotificationInformation.setFolderNotificationDefinitions(savedFolderNotificationDefinitions);
			savedFolderNotificationInformation.setTotalRecords(Long.valueOf(savedFolderNotificationDefinitions.size()));

			return new ResponseEntity<FolderNotificationInformation>(savedFolderNotificationInformation,
					HttpStatus.CREATED);
		}

		return new ResponseEntity<FolderNotificationInformation>(HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<FolderNotificationDefinition> saveFolderNotification(
			FolderNotificationDefinition folderNotificationDefinition) {

		FolderNotificationLog folderNotificationLog = folderNotificationLogTransformer
				.transformToFolderNotificationLog(folderNotificationDefinition);

		FolderNotificationDefinition savedFolderNotificationDefinition = folderNotificationLogTransformer
				.transformToFolderNotificationDefinition(folderNotificationLogRepository.save(folderNotificationLog));
		return new ResponseEntity<FolderNotificationDefinition>(savedFolderNotificationDefinition, HttpStatus.CREATED);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<FolderNotificationInformation> findAllByEventTypeAndFolderNames(
			FolderNotificationRequest folderNotificationRequest) {

		log.info("Inside findAllByEventTypeAndFolderNames for eventType -> {}",
				folderNotificationRequest.getEventType());

		List<FolderNotificationLog> folderNotificationLogs = folderNotificationLogRepository
				.findAllByEventTypeAndFolderNameIn(folderNotificationRequest.getEventType(),
						folderNotificationRequest.getFolderNames());

		if (null != folderNotificationLogs && !folderNotificationLogs.isEmpty()) {

			List<FolderNotificationDefinition> savedFolderNotificationDefinitions = new ArrayList<FolderNotificationDefinition>();
			folderNotificationLogs.forEach(folderNotificationLog -> {

				savedFolderNotificationDefinitions.add(folderNotificationLogTransformer
						.transformToFolderNotificationDefinition(folderNotificationLog));
			});

			FolderNotificationInformation savedFolderNotificationInformation = new FolderNotificationInformation();
			savedFolderNotificationInformation.setFolderNotificationDefinitions(savedFolderNotificationDefinitions);
			savedFolderNotificationInformation.setTotalRecords(Long.valueOf(savedFolderNotificationDefinitions.size()));

			return new ResponseEntity<FolderNotificationInformation>(savedFolderNotificationInformation, HttpStatus.OK);
		} else {

			FolderNotificationInformation savedFolderNotificationInformation = new FolderNotificationInformation();
			savedFolderNotificationInformation.setTotalRecords(0L);

			return new ResponseEntity<FolderNotificationInformation>(savedFolderNotificationInformation,
					HttpStatus.NO_CONTENT);
		}
	}

	@Override
	public ResponseEntity<String> updateFolderNotification(FolderNotificationDefinition folderNotificationDefinition) {

		if (null != folderNotificationDefinition.getFileCount()) {

			folderNotificationLogRepository.updateFolderNotificationLog(LocalDateTime.now(),
					folderNotificationDefinition.getFolderName(), folderNotificationDefinition.getFileCount(),
					folderNotificationDefinition.getId());
		} else {

			folderNotificationLogRepository.updateFolderNotificationLog(LocalDateTime.now(),
					folderNotificationDefinition.getFolderName(), folderNotificationDefinition.getId());
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<FolderNotificationDefinition> findLatestFolderNameByEventType(String eventType) {

		return new ResponseEntity<FolderNotificationDefinition>(
				folderNotificationLogTransformer.transformToFolderNotificationDefinition(folderNotificationLogRepository
						.findTopByEventTypeOrderByEventTimestampDesc(eventType).map(folderNotification -> {

							return folderNotification;
						}).orElseThrow(
								() -> new ResourceNotFoundException("No Batch running with batch type " + eventType))),
				HttpStatus.OK);

	}

}