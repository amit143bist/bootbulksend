package com.ds.proserv.email.common.processor;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.email.common.domain.RecipientInformation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMailProcessor implements IEmailProcessor {

	public String createRedirectUri(HttpServletRequest request, String callbackUri) {

		log.debug(
				"ServerName -> {}, Port -> {} Path -> {} isSecure -> {} ContextPath -> {} ServletPath -> {} RequestUrl -> {}",
				request.getServerName(), request.getServerPort(), request.getServletPath(), request.isSecure(),
				request.getContextPath(), request.getServletPath(), request.getRequestURL());

		StringBuilder redirectUriBuilder = new StringBuilder();

		if (request.isSecure()) {
			redirectUriBuilder.append("https://");
		} else {
			redirectUriBuilder.append("http://");
		}

		redirectUriBuilder.append(request.getServerName());
		if (request.getServerPort() > 0) {
			redirectUriBuilder.append(":" + request.getServerPort());
		}

		redirectUriBuilder.append(callbackUri);

		String redirectUri = redirectUriBuilder.toString();

		log.info("RedirectUri in createRedirectUri() {}", redirectUri);

		return redirectUri;
	}

	protected MimeMessage createEmail(RecipientInformation recipientInformation, String from, String subject,
			String bodyText) throws MessagingException {

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		InternetAddress fAddress = new InternetAddress(from);

		email.setFrom(fAddress);

		recipientInformation.getRecipientDefinitions().forEach(recipientDefinition -> {

			log.info("Recipient Email Addresses -> {}", recipientDefinition.getRecipientEmailAddresses());
			try {
				email.addRecipients(recipientDefinition.getRecipientType(),
						String.join(AppConstants.COMMA_DELIMITER, recipientDefinition.getRecipientEmailAddresses()));
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		});

		email.setSubject(subject);

		Multipart multiPart = new MimeMultipart("alternative");

		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText("---------- Notification ----------", "utf-8");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(bodyText, "text/html; charset=utf-8");

		multiPart.addBodyPart(textPart);
		multiPart.addBodyPart(htmlPart);
		email.setContent(multiPart);

		return email;
	}

	protected HttpHeaders getHttpHeaders(String accessTokenType, String accessToken) {

		HttpHeaders httpHeaders = new HttpHeaders();

		httpHeaders.add("Authorization", accessTokenType + " " + accessToken);
		httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return httpHeaders;
	}

}