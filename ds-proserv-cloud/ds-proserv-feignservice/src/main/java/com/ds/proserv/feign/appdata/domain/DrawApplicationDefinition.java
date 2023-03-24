package com.ds.proserv.feign.appdata.domain;

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
@JsonPropertyOrder({ "applicationId", "triggerEnvelopeId", "bridgeEnvelopeId", "bulkBatchId", "programType",
		"drawReference", "languageCode", "agentCode", "duplicateRecord", "applicationStatus", "noteAttribute" })
public class DrawApplicationDefinition implements IDocuSignInformation {

	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("triggerEnvelopeId")
	private String triggerEnvelopeId;

	@JsonProperty("bridgeEnvelopeId")
	private String bridgeEnvelopeId;

	@JsonProperty("bulkBatchId")
	private String bulkBatchId;

	@JsonProperty("programType")
	private String programType;

	@JsonProperty("drawReference")
	private String drawReference;

	@JsonProperty("languageCode")
	private String languageCode;

	@JsonProperty("agentCode")
	private String agentCode;

	@JsonProperty("duplicateRecord")
	private Boolean duplicateRecord;

	@JsonProperty("applicationStatus")
	private String applicationStatus;

	@JsonProperty("noteAttribute")
	private String noteAttribute;
}