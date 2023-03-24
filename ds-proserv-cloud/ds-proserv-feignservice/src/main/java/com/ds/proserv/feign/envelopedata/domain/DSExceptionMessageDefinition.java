package com.ds.proserv.feign.envelopedata.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "processId", "batchId", "groupId", "retryStatus", "recordIds" })
public class DSExceptionMessageDefinition implements IDocuSignInformation {

	private String processId;
	private String batchId;
	private String groupId;
	private String retryStatus;
	private List<String> recordIds;
}