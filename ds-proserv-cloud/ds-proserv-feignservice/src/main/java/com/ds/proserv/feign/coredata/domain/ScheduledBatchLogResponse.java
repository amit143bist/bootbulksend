package com.ds.proserv.feign.coredata.domain;

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
@JsonPropertyOrder({ "batchId", "batchType", "batchStartDateTime", "batchEndDateTime", "batchStartParameters" })
public class ScheduledBatchLogResponse implements IDocuSignInformation {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("batchType")
	private String batchType;
	@JsonProperty("batchStartDateTime")
	private String batchStartDateTime;
	@JsonProperty("batchEndDateTime")
	private String batchEndDateTime;
	@JsonProperty("batchStartParameters")
	private String batchStartParameters;
	@JsonProperty("totalRecords")
	private Long totalRecords;

}