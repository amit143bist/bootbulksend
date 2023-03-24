package com.ds.proserv.feign.envelopedata.domain;

import java.util.List;

import com.ds.proserv.feign.domain.AbstractDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "result", "batchId", "processId", "totalPages", "currentPage", "totalRecords", "nextAvailable",
		"contentAvailable", "previousAvailable", "dsEnvelopeDefinitions", "nextUri" })
public class DSEnvelopeInformation extends AbstractDocuSignInformation {

	@JsonProperty("result")
	private String result = null;
	@JsonProperty("dsEnvelopeDefinitions")
	private List<DSEnvelopeDefinition> dsEnvelopeDefinitions = null;
}