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
@JsonPropertyOrder({ "paramName", "paramValue", "delimitedList", "delimiter" })
public class PageQueryParam {

	@JsonProperty("paramName")
	private String paramName;
	@JsonProperty("paramValue")
	private String paramValue;
	@JsonProperty("delimitedList")
	private Boolean delimitedList;
	@JsonProperty("delimiter")
	private String delimiter;
}