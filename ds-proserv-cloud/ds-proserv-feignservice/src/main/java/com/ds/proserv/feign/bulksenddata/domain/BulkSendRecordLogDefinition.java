package com.ds.proserv.feign.bulksenddata.domain;

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
@JsonPropertyOrder({ "recordId", "recordType", "bulkBatchId", "startDateTime", "endDateTime" })
public class BulkSendRecordLogDefinition implements IDocuSignInformation {

	@JsonProperty("recordId")
	private String recordId;

	@JsonProperty("recordType")
	private String recordType;

	@JsonProperty("bulkBatchId")
	private String bulkBatchId;

	@JsonProperty("startDateTime")
	private String startDateTime;

	@JsonProperty("endDateTime")
	private String endDateTime;
}