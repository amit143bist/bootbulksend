package com.ds.proserv.notificationdata.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.feign.notificationdata.domain.NotificationDetailDefinition;
import com.ds.proserv.feign.notificationdata.service.NotificationDetailService;
import com.ds.proserv.notificationdata.model.NotificationDetailLog;
import com.ds.proserv.notificationdata.repository.NotificationDetailLogRepository;
import com.ds.proserv.notificationdata.transformer.NotificationDetailTransformer;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
@Transactional
public class NotificationDetailController implements NotificationDetailService {

	@Autowired
	private NotificationDetailLogRepository notificationDetailLogRepository;

	@Autowired
	private NotificationDetailTransformer notificationDetailTransformer;

	@Override
	public ResponseEntity<NotificationDetailDefinition> saveNotification(
			NotificationDetailDefinition notificationDetailDefinition) {

		log.info("saveNotification is called for notificationTopic -> {}",
				notificationDetailDefinition.getNotificationTopic());

		NotificationDetailLog notificationDetailLog = notificationDetailLogRepository
				.save(notificationDetailTransformer.transformToNotificationDetailLog(notificationDetailDefinition));

		return new ResponseEntity<NotificationDetailDefinition>(
				notificationDetailTransformer.transformToNotificationDetailDefinition(notificationDetailLog),
				HttpStatus.CREATED);
	}

}