package com.ds.proserv.feign.appdata.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
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
@JsonPropertyOrder({ "id", "applicationId", "applicationType", "envelopeId", "recipientEmails", "failureReason",
		"failureTimestamp", "envelopeSentTimestamp", "communityPartnerCode" })
public class ApplicationEnvelopeDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("applicationType")
	private String applicationType;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("recipientEmails")
	private List<String> recipientEmails;

	@JsonProperty("failureReason")
	private String failureReason;

	@JsonProperty("failureTimestamp")
	private String failureTimestamp;

	@JsonProperty("envelopeSentTimestamp")
	private String envelopeSentTimestamp;
	
	@JsonProperty("communityPartnerCode")
	private String communityPartnerCode;
}