package com.ds.proserv.feign.bulksend.sourcedata.domain;

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
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "recordIds", "totalRecords", "selectedRows" })
public class BulkSendPrepareDefinition implements IDocuSignInformation {

	@JsonProperty("recordIds")
	private List<Object> recordIds = null;

	@JsonProperty("totalRecords")
	private Integer totalRecords = null;

	@JsonProperty("selectedRows")
	private List<Map<String, Object>> selectedRows = null;
}