package com.ds.proserv.notificationdata.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.notificationdata.model.ClientCredential;

@Repository(value = "clientCredentialRepository")
public interface ClientCredentialRepository extends CrudRepository<ClientCredential, String> {

	ClientCredential findByCredentialTypeAndCredentialVendorAndApplicationIdentifierAndApplicationName(
			String credentialType, String credentialVendor, String applicationIdentifier, String applicationName);

	@Modifying
	@Query("update ClientCredential cc set cc.accessToken = :accessToken where cc.credentialId = :credentialId")
	void updateClientCredentialAccessToken(@Param(value = "accessToken") String accessToken,
			@Param(value = "credentialId") String credentialId);
}