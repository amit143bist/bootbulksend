package com.ds.proserv.email.common.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.CommunicationMode;
import com.ds.proserv.common.constant.MailProcessorType;
import com.ds.proserv.common.constant.NotificationType;
import com.ds.proserv.email.common.client.NotificationDetailClient;
import com.ds.proserv.email.common.domain.RecipientDefinition;
import com.ds.proserv.email.common.domain.RecipientInformation;
import com.ds.proserv.email.common.factory.EmailFactory;
import com.ds.proserv.email.common.processor.IEmailProcessor;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;
import com.ds.proserv.feign.notificationdata.domain.NotificationDetailDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

	@Autowired
	private EmailFactory emailFactory;

	@Autowired
	private NotificationDetailClient notificationDetailClient;

	public void sentNotification(MailProcessorType mailProcessorType, NotificationType notificationType,
			List<String> toRecipients, List<String> ccRecipients, List<String> bccRecipients, String emailSubject,
			String emailBody, String fromEmail) throws IOException, MessagingException {

		try {
			IEmailProcessor emailProcessor = emailFactory.findAllowedProcessor(mailProcessorType);

			ClientCredentialDefinition clientCredentialDefinition = emailProcessor.getClientCredentialDefinition();
			boolean accessTokenValid = emailProcessor.validateAccessToken(clientCredentialDefinition.getAccessToken());
			if (!accessTokenValid) {

				log.info("Old AccessToken is invalid so refreshing AccessToken using saved RefreshToken");
				String newAccessToken = emailProcessor.refreshAccessToken(clientCredentialDefinition.getRefreshToken());
				clientCredentialDefinition.setAccessToken(newAccessToken);
				emailProcessor.updateAccessToken(clientCredentialDefinition.getCredentialId(), newAccessToken);
			} else {

				log.info("Old AccessToken is still valid for notificationType -> {}", notificationType);
			}

			List<RecipientDefinition> recipientDefinitionList = new ArrayList<RecipientDefinition>();

			if (null != toRecipients && !toRecipients.isEmpty()) {

				RecipientDefinition toRecipientDefinition = new RecipientDefinition();
				toRecipientDefinition.setRecipientType(RecipientType.TO);
				toRecipientDefinition.setRecipientEmailAddresses(toRecipients);

				recipientDefinitionList.add(toRecipientDefinition);
			}

			if (null != ccRecipients && !ccRecipients.isEmpty()) {

				RecipientDefinition toRecipientDefinition = new RecipientDefinition();
				toRecipientDefinition.setRecipientType(RecipientType.CC);
				toRecipientDefinition.setRecipientEmailAddresses(ccRecipients);

				recipientDefinitionList.add(toRecipientDefinition);
			}

			if (null != bccRecipients && !bccRecipients.isEmpty()) {

				RecipientDefinition toRecipientDefinition = new RecipientDefinition();
				toRecipientDefinition.setRecipientType(RecipientType.BCC);
				toRecipientDefinition.setRecipientEmailAddresses(bccRecipients);

				recipientDefinitionList.add(toRecipientDefinition);
			}

			RecipientInformation recipientInformation = new RecipientInformation();
			recipientInformation.setRecipientDefinitions(recipientDefinitionList);

			emailProcessor.send(recipientInformation, fromEmail, emailSubject, emailBody, clientCredentialDefinition);

			List<String> recipientIds = new ArrayList<String>();

			if (null != toRecipients && !toRecipients.isEmpty()) {
				recipientIds.addAll(toRecipients);
			}

			if (null != ccRecipients && !ccRecipients.isEmpty()) {

				recipientIds.addAll(ccRecipients);
			}

			if (null != bccRecipients && !bccRecipients.isEmpty()) {

				recipientIds.addAll(bccRecipients);
			}

			NotificationDetailDefinition notificationDetailDefinition = new NotificationDetailDefinition();
			notificationDetailDefinition.setClientCredentialId(clientCredentialDefinition.getCredentialId());
			notificationDetailDefinition.setCommunicationMode(CommunicationMode.EMAIL.toString());
			notificationDetailDefinition.setNotificationSentTimestamp(LocalDateTime.now().toString());
			notificationDetailDefinition.setNotificationStatus("sent");
			notificationDetailDefinition.setNotificationTopic(emailSubject);
			notificationDetailDefinition.setNotificationType(notificationType.toString());
			notificationDetailDefinition.setRecipientIds(String.join(AppConstants.COMMA_DELIMITER, recipientIds));

			log.info(
					"Notification successfully sent via GMAIL to {} and saved in DB as well for notificationType -> {}",
					toRecipients, notificationType);

			notificationDetailClient.saveNotification(notificationDetailDefinition);
		} catch (Exception exp) {

			log.error("Exception -> {} occurred for notificationType -> {}", exp, notificationType);
			exp.printStackTrace();
		}

	}

	public void doAuthFlowAndSaveToken(MailProcessorType mailProcessorType, HttpServletRequest request,
			String authCode) {

		IEmailProcessor emailProcessor = emailFactory.findAllowedProcessor(mailProcessorType);

		emailProcessor.doAuthFlowAndSaveToken(request, authCode);
	}

	public String getAuthUrl(MailProcessorType mailProcessorType, HttpServletRequest request) {

		IEmailProcessor emailProcessor = emailFactory.findAllowedProcessor(mailProcessorType);

		String gmailAuthUrl = emailProcessor.getAuthUrl(request);

		log.info("gmailAuthUrl -> {}", gmailAuthUrl);
		return gmailAuthUrl;
	}

}