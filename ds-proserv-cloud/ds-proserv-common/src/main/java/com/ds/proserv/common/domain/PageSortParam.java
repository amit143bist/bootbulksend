package com.ds.proserv.common.domain;

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
@JsonPropertyOrder({ "fieldName", "sortDirection" })
public class PageSortParam {

	@JsonProperty("fieldName")
	public String fieldName;
	@JsonProperty("sortDirection")
	public String sortDirection;
}