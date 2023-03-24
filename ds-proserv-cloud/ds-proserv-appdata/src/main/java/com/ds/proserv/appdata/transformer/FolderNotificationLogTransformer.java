package com.ds.proserv.appdata.transformer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ds.proserv.appdata.model.FolderNotificationLog;
import com.ds.proserv.feign.appdata.domain.FolderNotificationDefinition;

@Component
public class FolderNotificationLogTransformer {

	public FolderNotificationLog transformToFolderNotificationLog(
			FolderNotificationDefinition folderNotificationDefinition) {

		FolderNotificationLog folderNotificationLog = new FolderNotificationLog();

		folderNotificationLog.setEventTimestamp(LocalDateTime.now());
		folderNotificationLog.setEventType(folderNotificationDefinition.getEventType());
		folderNotificationLog.setFileCount(folderNotificationDefinition.getFileCount());
		folderNotificationLog.setFolderName(folderNotificationDefinition.getFolderName());

		return folderNotificationLog;
	}

	public FolderNotificationDefinition transformToFolderNotificationDefinition(
			FolderNotificationLog folderNotificationLog) {

		FolderNotificationDefinition folderNotificationDefinition = new FolderNotificationDefinition();

		if (null != folderNotificationLog.getEventTimestamp()) {

			folderNotificationDefinition.setEventTimestamp(folderNotificationLog.getEventTimestamp().toString());
		}

		folderNotificationDefinition.setEventType(folderNotificationLog.getEventType());
		folderNotificationDefinition.setFileCount(folderNotificationLog.getFileCount());
		folderNotificationDefinition.setFolderName(folderNotificationLog.getFolderName());
		folderNotificationDefinition.setId(folderNotificationLog.getId());

		return folderNotificationDefinition;
	}
}