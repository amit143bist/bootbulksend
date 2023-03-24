package com.ds.proserv.feign.appdata.domain;

import java.util.List;
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
@JsonPropertyOrder({ "apiDataTableName", "recordIds", "processId", "batchId", "rowDataMapList" })
public class MigrationDataDefinition implements IDocuSignInformation {

	@JsonProperty("apiDataTableName")
	private String apiDataTableName;
	@JsonProperty("recordIds")
	private String recordIds;
	@JsonProperty("processId")
	private String processId;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("rowDataMapList")
	private List<Map<String, Object>> rowDataMapList = null;

}