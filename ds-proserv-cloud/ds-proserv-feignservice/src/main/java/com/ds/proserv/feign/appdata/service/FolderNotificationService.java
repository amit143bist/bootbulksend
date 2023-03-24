package com.ds.proserv.feign.appdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.appdata.domain.FolderNotificationDefinition;
import com.ds.proserv.feign.appdata.domain.FolderNotificationInformation;
import com.ds.proserv.feign.appdata.domain.FolderNotificationRequest;

public interface FolderNotificationService {

	@PostMapping("/docusign/folder/notification/bulksave")
	ResponseEntity<FolderNotificationInformation> bulkUpdateSaveFolderNotifications(
			@RequestBody FolderNotificationInformation folderNotificationInformation);

	@PostMapping("/docusign/folder/notification/save")
	ResponseEntity<FolderNotificationDefinition> saveFolderNotification(
			@RequestBody FolderNotificationDefinition folderNotificationDefinition);

	@PutMapping("/docusign/folder/notification/findby/eventtype/foldernames")
	ResponseEntity<FolderNotificationInformation> findAllByEventTypeAndFolderNames(
			@RequestBody FolderNotificationRequest folderNotificationRequest);

	@PutMapping("/docusign/folder/notification/update/folder")
	ResponseEntity<String> updateFolderNotification(
			@RequestBody FolderNotificationDefinition folderNotificationDefinition);

	@GetMapping("/docusign/folder/notification/findlatest/byeventtype/{eventType}")
	ResponseEntity<FolderNotificationDefinition> findLatestFolderNameByEventType(@PathVariable String eventType);
}