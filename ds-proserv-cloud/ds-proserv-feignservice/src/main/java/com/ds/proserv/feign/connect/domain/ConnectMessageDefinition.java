package com.ds.proserv.feign.connect.domain;

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
@JsonPropertyOrder({ "connectXML", "recordIds", "processId", "batchId", "groupId", "pageNumber" })
public class ConnectMessageDefinition implements IDocuSignInformation {

	@JsonProperty("connectXML")
	private String connectXML;

	@JsonProperty("recordIds")
	private List<String> recordIds;

	@JsonProperty("processId")
	private String processId;

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("groupId")
	private String groupId;
	
	@JsonProperty("pageNumber")
	private Integer pageNumber;
}