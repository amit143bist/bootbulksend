package com.ds.proserv.feign.bulksenddata.domain;

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
@JsonPropertyOrder({ "processId", "batchId", "groupId", "recordType", "startDateTime", "endDateTime", "recordIds",
		"updateRecordDataQueryType", "updateRecordDataQueryTypePrimaryKeyName", "bulkSendProcessLogDefinition",
		"bulkSendFailureLogDefinition" })
public class BulkSendProcessFailureMessageDefinition implements IDocuSignInformation {

	@JsonProperty("processId")
	private String processId;

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("groupId")
	private String groupId;

	@JsonProperty("recordType")
	private String recordType;

	@JsonProperty("startDateTime")
	private String startDateTime;

	@JsonProperty("endDateTime")
	private String endDateTime;

	@JsonProperty("recordIds")
	private List<String> recordIds;

	@JsonProperty("updateRecordDataQueryType")
	private String updateRecordDataQueryType;

	@JsonProperty("updateRecordDataQueryTypePrimaryKeyName")
	private String updateRecordDataQueryTypePrimaryKeyName;

	@JsonProperty("bulkSendProcessLogDefinition")
	private BulkSendProcessLogDefinition bulkSendProcessLogDefinition;

	@JsonProperty("bulkSendFailureLogDefinition")
	private BulkSendFailureLogDefinition bulkSendFailureLogDefinition;
}