package com.ds.proserv.feign.report.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "completeBatchOnError", "pathParams", "batchType" })
public class ReportRunArgs implements IDocuSignInformation{

	@JsonProperty("completeBatchOnError")
	private boolean completeBatchOnError = true;
	@JsonProperty("pathParams")
	private List<PathParam> pathParams = null;
	@JsonProperty("batchType")
	private String batchType = null;
}