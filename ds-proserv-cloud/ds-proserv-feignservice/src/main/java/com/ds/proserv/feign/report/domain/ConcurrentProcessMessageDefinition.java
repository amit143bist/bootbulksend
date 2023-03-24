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
@JsonPropertyOrder({ "batchId", "nextUri", "userId", "processId", "accountId", "parentGroupId", "paginationJson",
		"prepareAPI", "inputParams", "tableColumnMetaData" })
public class ConcurrentProcessMessageDefinition implements IDocuSignInformation {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("nextUri")
	private String nextUri;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("processId")
	private String processId;
	@JsonProperty("accountId")
	private String accountId;
	@JsonProperty("parentGroupId")
	private String parentGroupId;
	@JsonProperty("paginationJson")
	private String paginationJson;
	@JsonProperty("prepareAPI")
	private PrepareDataAPI prepareAPI;
	@JsonProperty("inputParams")
	private Map<String, Object> inputParams;
	@JsonProperty("tableColumnMetaData")
	private TableColumnMetaData tableColumnMetaData;

}