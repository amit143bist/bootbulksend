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
@JsonPropertyOrder({ "result", "prepareResult", "envelopeid", "envelopeSubject", "status", "sentdatetime",
		"delivereddatetime", "completeddatetime", "declineddatetime", "senderemail", "sendername", "terminalreason",
		"timeZone", "timeZoneOffset", "timeGenerated", "fileNames", "dsEnvelopeDocLogDefinition",
		"dsRecipientDefinitions", "dsCustomFieldDefinitions" })
public class DSEnvelopeDefinition implements IDocuSignInformation {

	@JsonProperty("result")
	private String result = null;

	@JsonProperty("prepareResult")
	private String prepareResult = null;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("envelopeSubject")
	private String envelopeSubject;

	@JsonProperty("status")
	private String status;

	@JsonProperty("sentDateTime")
	private String sentDateTime;

	@JsonProperty("deliveredDateTime")
	private String deliveredDateTime;

	@JsonProperty("completedDateTime")
	private String completedDateTime;

	@JsonProperty("declinedDateTime")
	private String declinedDateTime;

	@JsonProperty("senderEmail")
	private String senderEmail;

	@JsonProperty("senderName")
	private String senderName;

	@JsonProperty("terminalReason")
	private String terminalReason;

	@JsonProperty("timeZone")
	private String timeZone;

	@JsonProperty("timeZoneOffset")
	private Long timeZoneoffset;

	@JsonProperty("timeGenerated")
	private String timeGenerated;

	@JsonProperty("fileNames")
	private String fileNames;

	@JsonProperty("dsEnvelopeDocLogDefinition")
	private DSEnvelopeDocLogDefinition dsEnvelopeDocLogDefinition;

	@JsonProperty("dsRecipientDefinitions")
	private List<DSRecipientDefinition> dsRecipientDefinitions = null;

	@JsonProperty("dsCustomFieldDefinitions")
	private List<DSCustomFieldDefinition> dsCustomFieldDefinitions = null;
}