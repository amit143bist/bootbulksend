package com.ds.proserv.feign.report.domain;

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
@JsonPropertyOrder({ "batchId", "processId", "groupId", "downloadDocs", "downloadDataMessages" })
public class ConcurrentDocDownloadDataMessageDefinition implements IDocuSignInformation {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("processId")
	private String processId;
	@JsonProperty("groupId")
	private String groupId;
	@JsonProperty("downloadDocs")
	private DownloadDocs downloadDocs;
	@JsonProperty("downloadDataMessages")
	private List<DownloadDataMessage> downloadDataMessages;
}