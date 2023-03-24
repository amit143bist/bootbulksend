package com.ds.proserv.feign.envelopedata.domain;

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
@JsonPropertyOrder({ "id", "recipientId", "envelopeId", "type", "status", "eventDateTime" })
public class DSRecipientAuthDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("recipientId")
	private String recipientId;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("type")
	private String type;

	@JsonProperty("status")
	private String status;

	@JsonProperty("eventDateTime")
	private String eventDateTime;
}