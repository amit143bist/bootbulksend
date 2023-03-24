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
@JsonPropertyOrder({ "id", "envelopeId", "recipientId", "tabLabel", "tabName", "tabValue", "tabStatus",
		"tabOriginalValue" })
public class DSTabDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("recipientId")
	private String recipientId;

	@JsonProperty("tabLabel")
	private String tabLabel;

	@JsonProperty("tabName")
	private String tabName;

	@JsonProperty("tabValue")
	private String tabValue;

	@JsonProperty("tabStatus")
	private String tabStatus;

	@JsonProperty("tabOriginalValue")
	private String tabOriginalValue;
}