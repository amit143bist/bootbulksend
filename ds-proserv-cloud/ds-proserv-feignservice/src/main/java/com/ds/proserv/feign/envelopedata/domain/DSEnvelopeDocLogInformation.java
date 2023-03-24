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
@JsonPropertyOrder({ "batchId", "processId", "totalPages", "currentPage", "totalRecords", "nextAvailable",
		"contentAvailable", "previousAvailable", "dsEnvelopeDocLogDefinitions", "nextUri" })
public class DSEnvelopeDocLogInformation extends AbstractDocuSignInformation {

	@JsonProperty("dsEnvelopeDocLogDefinitions")
	private List<DSEnvelopeDocLogDefinition> dsEnvelopeDocLogDefinitions = null;
}