package com.ds.proserv.feign.notificationdata.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "credentialType", "credentialVendor", "applicationIdentifier", "applicationName", "accessToken" })
public class ClientCredentialRequest {

	@JsonProperty("credentialType")
	private String credentialType;// Mobile or Email

	@JsonProperty("credentialVendor")
	private String credentialVendor;// Gmail, Twilio

	@JsonProperty("applicationIdentifier")
	private String applicationIdentifier;

	@JsonProperty("applicationName")
	private String applicationName;

	@JsonProperty("accessToken")
	private String accessToken;

}