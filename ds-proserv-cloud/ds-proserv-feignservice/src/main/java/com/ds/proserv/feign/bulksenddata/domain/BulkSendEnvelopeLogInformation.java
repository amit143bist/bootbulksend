package com.ds.proserv.feign.bulksenddata.domain;

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
		"contentAvailable", "previousAvailable", "bulkSendEnvelopeLogDefinitions", "nextUri" })
public class BulkSendEnvelopeLogInformation extends AbstractDocuSignInformation {

	@JsonProperty("bulkSendEnvelopeLogDefinitions")
	private List<BulkSendEnvelopeLogDefinition> bulkSendEnvelopeLogDefinitions = null;
}