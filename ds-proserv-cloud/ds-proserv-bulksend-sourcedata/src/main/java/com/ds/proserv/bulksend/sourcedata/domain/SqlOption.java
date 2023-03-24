package com.ds.proserv.bulksend.sourcedata.domain;

import java.util.List;

import com.ds.proserv.feign.report.domain.PathParam;
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
@JsonPropertyOrder({ "queryIdentifier", "queryType", "selectSql", "updateSql", "whereClause", "orderByClause",
		"primaryKey", "sqlParams" })
public class SqlOption {

	@JsonProperty("queryIdentifier")
	private String queryIdentifier;
	@JsonProperty("queryType")
	private String queryType;
	@JsonProperty("selectSql")
	private String selectSql;
	@JsonProperty("updateSql")
	private String updateSql;
	@JsonProperty("whereClause")
	private String whereClause;
	@JsonProperty("orderByClause")
	private String orderByClause;
	@JsonProperty("primaryKey")
	private String primaryKey;
	@JsonProperty("sqlParams")
	private List<PathParam> pathParam = null;
}