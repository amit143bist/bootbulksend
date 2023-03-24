package com.ds.proserv.feign.envelopedata.domain;

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
@JsonPropertyOrder({ "recipientId", "envelopeId", "status", "routingOrder", "recipientEmail", "recipientName",
		"declineReason", "recipientIPAddress", "clientUserId", "sentDateTime", "deliveredDateTime", "signedDateTime",
		"declinedDateTime", "dsTabDefinitions", "dsRecipientAuthDefinitions" })
public class DSRecipientDefinition implements IDocuSignInformation {

	@JsonProperty("recipientId")
	private String recipientId;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("status")
	private String status;

	@JsonProperty("routingOrder")
	private Long routingOrder;

	@JsonProperty("recipientEmail")
	private String recipientEmail;

	@JsonProperty("recipientName")
	private String recipientName;

	@JsonProperty("declineReason")
	private String declineReason;

	@JsonProperty("recipientIPAddress")
	private String recipientIPAddress;

	@JsonProperty("clientUserId")
	private String clientUserId;

	@JsonProperty("sentDateTime")
	private String sentDateTime;

	@JsonProperty("deliveredDateTime")
	private String deliveredDateTime;

	@JsonProperty("signedDateTime")
	private String signedDateTime;

	@JsonProperty("declinedDateTime")
	private String declinedDateTime;

	@JsonProperty("dsTabDefinitions")
	private List<DSTabDefinition> dsTabDefinitions = null;

	@JsonProperty("dsRecipientAuthDefinitions")
	private List<DSRecipientAuthDefinition> dsRecipientAuthDefinitions = null;
}