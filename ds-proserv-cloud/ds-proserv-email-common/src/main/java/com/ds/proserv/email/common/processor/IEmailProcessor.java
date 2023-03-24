package com.ds.proserv.email.common.processor;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import com.ds.proserv.common.constant.MailProcessorType;
import com.ds.proserv.email.common.domain.RecipientInformation;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;

public interface IEmailProcessor {

	boolean canProcessRequest(MailProcessorType mailProcessorType);

	void send(RecipientInformation recipientInformation, String fromEmail, String title, String message,
			ClientCredentialDefinition clientCredentialDefinition) throws IOException, MessagingException;

	String getAuthUrl(HttpServletRequest request);

	void doAuthFlowAndSaveToken(HttpServletRequest request, String authCode);

	boolean validateAccessToken(String accessToken);

	String refreshAccessToken(String refreshToken);

	ClientCredentialDefinition getClientCredentialDefinition();

	void updateAccessToken(String credentialId, String newAccessToken);
}