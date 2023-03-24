package com.ds.proserv.notificationdata.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialDefinition;
import com.ds.proserv.feign.notificationdata.domain.ClientCredentialRequest;
import com.ds.proserv.feign.notificationdata.service.ClientCredentialService;
import com.ds.proserv.notificationdata.model.ClientCredential;
import com.ds.proserv.notificationdata.repository.ClientCredentialRepository;
import com.ds.proserv.notificationdata.transformer.ClientCredentialTransformer;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
@Transactional
public class ClientCredentialController implements ClientCredentialService {

	@Autowired
	private ClientCredentialRepository clientCredentialRepository;
	
	@Autowired
	private ClientCredentialTransformer clientCredentialTransformer;

	@Override
	public ResponseEntity<ClientCredentialDefinition> saveClientCredential(
			ClientCredentialDefinition clientCredentialDefinition) {

		log.info("saveClientCredential is called for vendor -> {}", clientCredentialDefinition.getCredentialVendor());
		ClientCredential clientCredential = clientCredentialRepository
				.save(clientCredentialTransformer.transformToClientCredential(clientCredentialDefinition));
		return new ResponseEntity<ClientCredentialDefinition>(
				clientCredentialTransformer.transformToClientCredentialDefinition(clientCredential),
				HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ClientCredentialDefinition> findByClientCredentialRequest(
			ClientCredentialRequest clientCredentialRequest) {

		ClientCredential clientCredential = clientCredentialRepository
				.findByCredentialTypeAndCredentialVendorAndApplicationIdentifierAndApplicationName(
						clientCredentialRequest.getCredentialType(), clientCredentialRequest.getCredentialVendor(),
						clientCredentialRequest.getApplicationIdentifier(),
						clientCredentialRequest.getApplicationName());

		return new ResponseEntity<ClientCredentialDefinition>(
				clientCredentialTransformer.transformToClientCredentialDefinition(clientCredential), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> updateClientCredentialAccessToken(String credentialId,
			ClientCredentialRequest clientCredentialRequest) {

		clientCredentialRepository.updateClientCredentialAccessToken(clientCredentialRequest.getAccessToken(), credentialId);
		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

}