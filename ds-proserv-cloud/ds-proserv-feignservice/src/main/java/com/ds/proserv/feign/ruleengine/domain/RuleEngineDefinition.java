package com.ds.proserv.feign.ruleengine.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.ds.proserv.feign.report.domain.Filter;
import com.ds.proserv.feign.report.domain.OutputColumn;
import com.ds.proserv.feign.report.domain.PathParam;
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
@JsonPropertyOrder({ "apiDataTableName", "commonFilters", "outputColumns", "pathParams" })
public class RuleEngineDefinition implements IDocuSignInformation {

	@JsonProperty("apiDataTableName")
	private String apiDataTableName;
	@JsonProperty("commonFilters")
	private List<Filter> commonFilters = null;
	@JsonProperty("outputColumns")
	private List<OutputColumn> outputColumns = null;
	@JsonProperty("pathParams")
	private List<PathParam> pathParams = null;

}