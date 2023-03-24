package com.ds.proserv.email.common.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.CredentialType;
import com.ds.proserv.common.constant.CredentialVendor;
import com.ds.proserv.common.constant.MailProcessorType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ResourceConditionFailedException;
import com.ds.proserv.email.common.client.ClientCredentialClient;
import com.ds.proserv.email.common.domain.EmailAccessToken;
import com.ds.proserv.email.common.domain.RecipientInformation;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleMailProcessor extends AbstractMailProcessor {

	@Autowired
	private HttpTransport HTTP_TRANSPORT;

	@Autowired
	private JsonFactory JSON_FACTORY;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClientCredentialClient clientCredentialClient;

	private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_COMPOSE);

	@Override
	public boolean canProcessRequest(MailProcessorType mailProcessorType) {

		return mailProcessorType == MailProcessorType.GMAIL;
	}

	@Override
	public void send(RecipientInformation recipientInformation, String fromEmail, String title, String message,
			ClientCredentialDefinition clientCredentialDefinition) throws IOException, MessagingException {

		if (StringUtils.isEmpty(fromEmail)) {

			fromEmail = clientCredentialDefinition.getApplicationIdentifier();
		}

		Message m = createMessageWithEmail(createEmail(recipientInformation, fromEmail, title, message));

		HttpHeaders httpHeaders = getHttpHeaders(clientCredentialDefinition.getTokenType(),
				clientCredentialDefinition.getAccessToken());

		String msgBody = objectMapper.writeValueAsString(m);
		HttpEntity<String> requestEntity = new HttpEntity<String>(msgBody, httpHeaders);

		log.debug("requestEntity- {}", requestEntity);
		ResponseEntity<String> googleResponseEntity = restTemplate.exchange(
				"https://www.googleapis.com/gmail/v1/users/me/messages/send", HttpMethod.POST, requestEntity,
				String.class);

		log.info("GoogleMailService.Send() respbody -> {}", googleResponseEntity.getBody());
	}

	private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		email.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());

		log.debug("GoogleMailProcessor.createMessageWithEmail() for email -> {}", encodedEmail);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	@Override
	public String getAuthUrl(HttpServletRequest request) {

		String clientId = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTID);
		String clientSecret = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTSECRET);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientId, clientSecret, SCOPES).setAccessType("offline").build();

		AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl()
				.setRedirectUri(createRedirectUri(request, "/successcallback"));

		return authorizationUrl.build();
	}

	@Override
	public void doAuthFlowAndSaveToken(HttpServletRequest request, String authCode) {

		String clientId = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTID);
		String clientSecret = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTSECRET);

		String applicationIdentifier = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_APP_IDENTIFIER);
		String applicationName = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_APP_NAME);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientId, clientSecret, SCOPES).setAccessType("offline").build();

		ClientCredentialDefinition clientCredentialDefinition = null;
		try {

			TokenResponse tokenResponse = flow.newTokenRequest(authCode)
					.setRedirectUri(createRedirectUri(request, "/successcallback")).execute();

			log.debug("TokenType in doAuthFlowAndFetchToken-> {}, AccessToken -> {}, RefreshToken -> {} ",
					tokenResponse.getTokenType(), tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
			String accessToken = tokenResponse.getAccessToken();

			clientCredentialDefinition = new ClientCredentialDefinition();
			clientCredentialDefinition.setAccessToken(accessToken);
			clientCredentialDefinition.setApplicationIdentifier(applicationIdentifier);
			clientCredentialDefinition.setApplicationName(applicationName);
			clientCredentialDefinition.setCredentialType(CredentialType.EMAIL.toString());
			clientCredentialDefinition.setCredentialVendor(CredentialVendor.GMAIL.toString());
			clientCredentialDefinition.setExpiresIn(tokenResponse.getExpiresInSeconds());
			clientCredentialDefinition.setRefreshToken(tokenResponse.getRefreshToken());
			clientCredentialDefinition.setTokenType(tokenResponse.getTokenType());

			clientCredentialClient.saveClientCredential(clientCredentialDefinition);

		} catch (IOException exp) {
			log.error("Exception -> {} occurred in doAuthFlowAndFetchToken", exp);
			exp.printStackTrace();

			ResourceConditionFailedException resourceConditionFailedException = new ResourceConditionFailedException(
					exp.getMessage());
			throw resourceConditionFailedException;
		}

	}

	@Override
	public boolean validateAccessToken(String accessToken) {

		boolean accessTokenValid = true;

		try {

			ResponseEntity<String> envelopeResponseEntity = restTemplate.getForEntity(
					"https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken, String.class);

			log.debug("Body envelopeResponseEntity {}", envelopeResponseEntity.getBody());
		} catch (Exception e) {

			log.info("Old AccessToken expired for GMAIL");
			accessTokenValid = false;

			if (e instanceof HttpClientErrorException) {

				HttpClientErrorException exp = (HttpClientErrorException) e;
				log.error("ResponseBodyAsString in validateAccessToken is {}", exp.getResponseBodyAsString());
				log.error("MostSpecificCause in validateAccessToken is {}", exp.getMostSpecificCause());
			} else {

				e.printStackTrace();
			}
		}

		return accessTokenValid;
	}

	@Override
	public String refreshAccessToken(String refreshToken) {

		String accessToken = null;
		try {

			String clientId = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTID);
			String clientSecret = dsCacheManager
					.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_CLIENTSECRET);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			String msgBody = "client_secret=" + clientSecret + "&grant_type=refresh_token&refresh_token=" + refreshToken
					+ "&client_id=" + clientId;
			HttpEntity<String> requestEntity = new HttpEntity<String>(msgBody, httpHeaders);

			ResponseEntity<EmailAccessToken> googleResponseEntity = restTemplate.exchange(
					"https://www.googleapis.com/oauth2/v4/token", HttpMethod.POST, requestEntity,
					EmailAccessToken.class);

			accessToken = googleResponseEntity.getBody().getAccessToken();

		} catch (Exception e) {
			e.printStackTrace();

			if (e instanceof HttpClientErrorException) {

				HttpClientErrorException exp = (HttpClientErrorException) e;
				log.error("ResponseBodyAsString in refreshAccessToken is {}", exp.getResponseBodyAsString());
				log.error("MostSpecificCause in refreshAccessToken is {}", exp.getMostSpecificCause());
			}
		}

		return accessToken;
	}

	@Override
	public ClientCredentialDefinition getClientCredentialDefinition() {

		String applicationIdentifier = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_APP_IDENTIFIER);
		String applicationName = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.GMAIL_APP_NAME);

		ClientCredentialRequest clientCredentialRequest = new ClientCredentialRequest();
		clientCredentialRequest.setApplicationIdentifier(applicationIdentifier);
		clientCredentialRequest.setApplicationName(applicationName);
		clientCredentialRequest.setCredentialType(CredentialType.EMAIL.toString());
		clientCredentialRequest.setCredentialVendor(CredentialVendor.GMAIL.toString());
		return clientCredentialClient.findByClientCredentialRequest(clientCredentialRequest).getBody();

	}

	@Override
	public void updateAccessToken(String credentialId, String newAccessToken) {

		ClientCredentialRequest clientCredentialRequest = new ClientCredentialRequest();
		clientCredentialRequest.setAccessToken(newAccessToken);

		clientCredentialClient.updateClientCredentialAccessToken(credentialId, clientCredentialRequest);

	}

}