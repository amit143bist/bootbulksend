package com.ds.proserv.feign.authentication.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "error", "urlCodeGrant", "access_token", "token_type", "expires_in", "api_base_url" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse implements IDocuSignInformation {

	@JsonProperty("error")
	private String error;
	@JsonProperty("urlCodeGrant")
	private String urlCodeGrant;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("expires_in")
	private Integer expiresIn;
	@JsonProperty("api_base_url")
	private String apiBaseUrl;

}