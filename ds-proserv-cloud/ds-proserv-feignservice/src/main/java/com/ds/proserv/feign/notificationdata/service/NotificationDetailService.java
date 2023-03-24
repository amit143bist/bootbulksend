package com.ds.proserv.feign.notificationdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.notificationdata.domain.NotificationDetailDefinition;

public interface NotificationDetailService {

	@PostMapping("/docusign/notification/save")
	ResponseEntity<NotificationDetailDefinition> saveNotification(@RequestBody NotificationDetailDefinition notificationDetailDefinition);
}