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
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "tableName", "columns", "primaryKey" })
public class TableDefinition implements IDocuSignInformation{

	@JsonProperty("tableName")
	private String tableName;
	@JsonProperty("columns")
	private List<Column> columns = null;
	@JsonProperty("primaryKey")
	private List<String> primaryKey = null;

}