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
@JsonPropertyOrder({ "nextUri", "batchId", "processId", "groupId", "accountId", "primaryId", "reportRowsList",
		"tableColumnMetaData" })
public class ConcurrentReportDataMessageDefinition implements IDocuSignInformation {

	@JsonProperty("nextUri")
	private String nextUri;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("processId")
	private String processId;
	@JsonProperty("groupId")
	private String groupId;
	@JsonProperty("accountId")
	private String accountId;
	@JsonProperty("primaryId")
	private String primaryId;
	@JsonProperty("reportRowsList")
	private List<List<ReportData>> reportRowsList;
	@JsonProperty("tableColumnMetaData")
	private TableColumnMetaData tableColumnMetaData;
}