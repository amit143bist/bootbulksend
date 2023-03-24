package com.ds.proserv.notificationdata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.feign.notificationdata.domain.NotificationDetailDefinition;
import com.ds.proserv.notificationdata.model.NotificationDetailLog;

@Component
public class NotificationDetailTransformer {

	public NotificationDetailLog transformToNotificationDetailLog(
			NotificationDetailDefinition notificationDetailDefinition) {

		NotificationDetailLog notificationDetailLog = new NotificationDetailLog();

		notificationDetailLog.setNotificationId(notificationDetailDefinition.getNotificationId());
		notificationDetailLog.setClientCredentialId(notificationDetailDefinition.getClientCredentialId());
		notificationDetailLog.setCommunicationMode(notificationDetailDefinition.getCommunicationMode());

		if (!StringUtils.isEmpty(notificationDetailDefinition.getNotificationSentTimestamp())) {

			notificationDetailLog.setNotificationSentTimestamp(
					LocalDateTime.parse(notificationDetailDefinition.getNotificationSentTimestamp()));
		}

		notificationDetailLog.setNotificationStatus(notificationDetailDefinition.getNotificationStatus());
		notificationDetailLog.setNotificationTopic(notificationDetailDefinition.getNotificationTopic());
		notificationDetailLog.setNotificationType(notificationDetailDefinition.getNotificationType());
		notificationDetailLog.setRecipientIds(notificationDetailDefinition.getRecipientIds());

		return notificationDetailLog;
	}

	public NotificationDetailDefinition transformToNotificationDetailDefinition(
			NotificationDetailLog notificationDetailLog) {

		NotificationDetailDefinition notificationDetailDefinition = new NotificationDetailDefinition();

		notificationDetailDefinition.setNotificationId(notificationDetailLog.getNotificationId());
		notificationDetailDefinition.setClientCredentialId(notificationDetailLog.getClientCredentialId());
		notificationDetailDefinition.setCommunicationMode(notificationDetailLog.getCommunicationMode());

		if (null != notificationDetailLog.getNotificationSentTimestamp()) {

			notificationDetailDefinition
					.setNotificationSentTimestamp(notificationDetailLog.getNotificationSentTimestamp().toString());
		}

		notificationDetailDefinition.setNotificationStatus(notificationDetailLog.getNotificationStatus());
		notificationDetailDefinition.setNotificationTopic(notificationDetailLog.getNotificationTopic());
		notificationDetailDefinition.setNotificationType(notificationDetailLog.getNotificationType());
		notificationDetailDefinition.setRecipientIds(notificationDetailLog.getRecipientIds());

		return notificationDetailDefinition;
	}
}