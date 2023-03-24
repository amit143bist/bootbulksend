package com.ds.proserv.feign.appdata.domain;

import java.util.Map;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
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
@JsonPropertyOrder({ "processId", "batchId", "inputParams", "columnNameHeaderMap", "csvReportDataExport" })
public class MigrationReportDataDefinition implements IDocuSignInformation {

	@JsonProperty("processId")
	private String processId;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("inputParams")
	private Map<String, Object> inputParams;
	@JsonProperty("columnNameHeaderMap")
	private Map<String, String> columnNameHeaderMap;
	@JsonProperty("csvReportDataExport")
	private ManageDataAPI csvReportDataExport;
}