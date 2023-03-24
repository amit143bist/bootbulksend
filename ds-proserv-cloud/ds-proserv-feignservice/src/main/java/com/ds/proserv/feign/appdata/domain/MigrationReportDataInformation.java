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
@JsonPropertyOrder({ "reportDataList", "totalRecords" })
public class MigrationReportDataInformation implements IDocuSignInformation {

	@JsonProperty("reportDataList")
	private List<Map<String, Object>> reportDataList;

	@JsonProperty("totalRecords")
	private Long totalRecords;
}