package com.ds.proserv.feign.coredata.domain;

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
@JsonPropertyOrder({ "totalBatchesCount", "scheduledBatchLogResponses" })
public class ScheduledBatchLogsInformation implements IDocuSignInformation {

	@JsonProperty("totalBatchesCount")
	private Long totalBatchesCount;
	@JsonProperty("scheduledBatchLogResponses")
	private List<ScheduledBatchLogResponse> scheduledBatchLogResponses = null;

}