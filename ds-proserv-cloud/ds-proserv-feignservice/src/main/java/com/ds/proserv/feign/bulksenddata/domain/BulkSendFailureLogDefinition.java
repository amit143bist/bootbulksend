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
@JsonPropertyOrder({ "id", "errorMessage", "applicationIds", "batchSize", "batchFailureDateTime" })
public class BulkSendFailureLogDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("errorMessage")
	private String errorMessage;

	@JsonProperty("applicationIds")
	private String applicationIds;

	@JsonProperty("batchSize")
	private Long batchSize;

	@JsonProperty("batchFailureDateTime")
	private String batchFailureDateTime;
}