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
@JsonPropertyOrder({ "envelopeId", "envDate", "envTimeStamp", "senderIdentifier", "downloadBucketName",
		"docDownloadStatusFlag", "docDownloadTimeStamp", "envProcessStatusFlag", "envProcessStartDateTime",
		"envProcessEndDateTime" })
public class CustomEnvelopeDataDefinition implements IDocuSignInformation {

	@JsonProperty("envelopeId")
	private String envelopeId;
	@JsonProperty("envDate")
	private String envDate;
	@JsonProperty("envTimeStamp")
	private String envTimeStamp;
	@JsonProperty("senderIdentifier")
	private String senderIdentifier;
	@JsonProperty("downloadBucketName")
	private String downloadBucketName;
	@JsonProperty("docDownloadStatusFlag")
	private String docDownloadStatusFlag;
	@JsonProperty("docDownloadTimeStamp")
	private String docDownloadTimeStamp;
	@JsonProperty("envProcessStatusFlag")
	private String envProcessStatusFlag;
	@JsonProperty("envProcessStartDateTime")
	private String envProcessStartDateTime;
	@JsonProperty("envProcessEndDateTime")
	private String envProcessEndDateTime;

}