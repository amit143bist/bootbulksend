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
@JsonPropertyOrder({ "processId", "batchId", "groupId", "bulkSendRecordLogDefinitions" })
public class BulkSendRecordLogMessageDefinition implements IDocuSignInformation {

	@JsonProperty("processId")
	private String processId;

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("groupId")
	private String groupId;

	@JsonProperty("bulkSendRecordLogDefinitions")
	private List<BulkSendRecordLogDefinition> bulkSendRecordLogDefinitions = null;
}