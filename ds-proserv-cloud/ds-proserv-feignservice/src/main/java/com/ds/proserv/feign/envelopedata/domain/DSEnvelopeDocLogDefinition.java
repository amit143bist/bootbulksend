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
@JsonPropertyOrder({ "id", "envelopeId", "timeGenerated", "docDownloaded", "docDownloadStatus", "docDownloadDateTime",
		"docDownloadFailureReason" })
public class DSEnvelopeDocLogDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("envelopeId")
	private String envelopeId;

	@JsonProperty("timeGenerated")
	private String timeGenerated;

	@JsonProperty("docDownloaded")
	private Boolean docDownloaded;

	@JsonProperty("docDownloadStatus")
	private String docDownloadStatus;

	@JsonProperty("docDownloadDateTime")
	private String docDownloadDateTime;

	@JsonProperty("docDownloadFailureReason")
	private String docDownloadFailureReason;
}