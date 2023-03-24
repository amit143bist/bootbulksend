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
@JsonPropertyOrder({ "id", "eventType", "folderName", "fileCount", "eventTimestamp" })
public class FolderNotificationDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;
	@JsonProperty("eventType")
	private String eventType;
	@JsonProperty("folderName")
	private String folderName;
	@JsonProperty("fileCount")
	private Long fileCount;
	@JsonProperty("eventTimestamp")
	private String eventTimestamp;
}