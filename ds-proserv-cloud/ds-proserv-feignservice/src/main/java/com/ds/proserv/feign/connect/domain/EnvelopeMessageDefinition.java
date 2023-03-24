package com.ds.proserv.feign.connect.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
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
@JsonPropertyOrder({ "dsEnvelopeDefinition", "recordIds", "processId", "batchId", "groupId" })
public class EnvelopeMessageDefinition implements IDocuSignInformation {

	@JsonProperty("dsEnvelopeDefinition")
	private DSEnvelopeDefinition dsEnvelopeDefinition;

	@JsonProperty("recordIds")
	private List<String> recordIds;

	@JsonProperty("processId")
	private String processId;

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("groupId")
	private String groupId;
}