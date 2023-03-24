package com.ds.proserv.notificationdata.transformer;

import org.springframework.stereotype.Component;

import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;
import com.ds.proserv.notificationdata.model.ClientCredential;

@Component
public class ClientCredentialTransformer {

	public ClientCredential transformToClientCredential(ClientCredentialDefinition clientCredentialDefinition) {

		ClientCredential clientCredential = new ClientCredential();

		clientCredential.setCredentialId(clientCredentialDefinition.getCredentialId());
		clientCredential.setAccessToken(clientCredentialDefinition.getAccessToken());
		clientCredential.setApplicationIdentifier(clientCredentialDefinition.getApplicationIdentifier());
		clientCredential.setApplicationName(clientCredentialDefinition.getApplicationName());
		clientCredential.setCredentialType(clientCredentialDefinition.getCredentialType());
		clientCredential.setCredentialVendor(clientCredentialDefinition.getCredentialVendor());
		clientCredential.setExpiresIn(clientCredentialDefinition.getExpiresIn());
		clientCredential.setRefreshToken(clientCredentialDefinition.getRefreshToken());
		clientCredential.setTokenType(clientCredentialDefinition.getTokenType());

		return clientCredential;
	}

	public ClientCredentialDefinition transformToClientCredentialDefinition(ClientCredential clientCredential) {

		ClientCredentialDefinition clientCredentialDefinition = new ClientCredentialDefinition();

		clientCredentialDefinition.setCredentialId(clientCredential.getCredentialId());
		clientCredentialDefinition.setAccessToken(clientCredential.getAccessToken());
		clientCredentialDefinition.setApplicationIdentifier(clientCredential.getApplicationIdentifier());
		clientCredentialDefinition.setApplicationName(clientCredential.getApplicationName());
		clientCredentialDefinition.setCredentialType(clientCredential.getCredentialType());
		clientCredentialDefinition.setCredentialVendor(clientCredential.getCredentialVendor());
		clientCredentialDefinition.setExpiresIn(clientCredential.getExpiresIn());
		clientCredentialDefinition.setRefreshToken(clientCredential.getRefreshToken());
		clientCredentialDefinition.setTokenType(clientCredential.getTokenType());

		return clientCredentialDefinition;
	}
}