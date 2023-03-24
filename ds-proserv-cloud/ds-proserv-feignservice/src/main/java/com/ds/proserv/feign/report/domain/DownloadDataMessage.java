package com.ds.proserv.feign.report.domain;

import java.util.Map;

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
@JsonPropertyOrder({ "accountId", "recordId", "parentDirectory", "fileSaveFormat", "fileName", "folderName",
		"inputParams" })
public class DownloadDataMessage implements IDocuSignInformation {

	@JsonProperty("accountId")
	private String accountId;
	@JsonProperty("recordId")
	private String recordId;
	@JsonProperty("parentDirectory")
	private String parentDirectory;
	@JsonProperty("fileSaveFormat")
	private String fileSaveFormat;
	@JsonProperty("fileName")
	private String fileName;
	@JsonProperty("folderName")
	private String folderName;
	@JsonProperty("inputParams")
	private Map<String, Object> inputParams;
}