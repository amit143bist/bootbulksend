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
@JsonPropertyOrder({ "batchId", "mailingListId", "batchName", "batchSize", "batchStatus", "successSize", "failedSize",
		"queueSize", "batchSubmittedDateTime", "bulkErrors" })
public class BulkSendProcessLogDefinition implements IDocuSignInformation {

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("mailingListId")
	private String mailingListId;

	@JsonProperty("batchName")
	private String batchName;

	@JsonProperty("batchSize")
	private Long batchSize;

	@JsonProperty("batchStatus")
	private String batchStatus;

	@JsonProperty("successSize")
	private Long successSize;

	@JsonProperty("failedSize")
	private Long failedSize;

	@JsonProperty("queueSize")
	private Long queueSize;

	@JsonProperty("batchSubmittedDateTime")
	private String batchSubmittedDateTime;

	@JsonProperty("bulkErrors")
	private String bulkErrors;
}