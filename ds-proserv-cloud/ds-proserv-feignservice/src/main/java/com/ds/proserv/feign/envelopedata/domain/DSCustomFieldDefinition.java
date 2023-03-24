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
@JsonPropertyOrder({ "id", "envelopeId", "recipientId", "documentId", "fieldName", "fieldValue", "fieldType" })
public class DSCustomFieldDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("recipientId")
	private String recipientId;

	@JsonProperty("documentId")
	private Long documentId;

	@JsonProperty("documentName")
	private String documentName;

	@JsonProperty("documentSequence")
	private Long documentSequence;

	@JsonProperty("fieldName")
	private String fieldName;

	@JsonProperty("fieldValue")
	private String fieldValue;

	@JsonProperty("fieldType")
	private String fieldType;
}