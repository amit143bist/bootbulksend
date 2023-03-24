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
@JsonPropertyOrder({ "id", "bulkBatchId", "envelopeId" })
public class BulkSendEnvelopeLogDefinition implements IDocuSignInformation {

	@JsonProperty("id")
	private String id;

	@JsonProperty("bulkBatchId")
	private String bulkBatchId;

	@JsonProperty("envelopeId")
	private String envelopeId;
}