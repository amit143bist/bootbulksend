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
@JsonPropertyOrder({ "id", "exceptionReason", "envelopeId", "envelopeXml", "retryStatus", "retryCount", "retryDateTime",
		"exceptionCode", "exceptionStep", "exceptionDateTime" })
public class DSExceptionDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("exceptionReason")
	private String exceptionReason;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("envelopeXml")
	private String envelopeXml;

	@JsonProperty("retryStatus")
	private String retryStatus;

	@JsonProperty("retryCount")
	private Long retryCount;

	@JsonProperty("retryDateTime")
	private String retryDateTime;

	@JsonProperty("exceptionCode")
	private String exceptionCode;

	@JsonProperty("exceptionStep")
	private String exceptionStep;

	@JsonProperty("exceptionDateTime")
	private String exceptionDateTime;
}