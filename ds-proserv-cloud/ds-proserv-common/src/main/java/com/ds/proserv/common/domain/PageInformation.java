package com.ds.proserv.common.domain;

import java.util.List;

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
@JsonPropertyOrder({ "pageNumber", "recordsPerPage", "pageSortParams", "pageQueryParams" })
public class PageInformation {

	@JsonProperty("pageNumber")
	public Integer pageNumber;
	@JsonProperty("recordsPerPage")
	public Integer recordsPerPage;
	@JsonProperty("sortParams")
	public List<PageSortParam> pageSortParams = null;
	@JsonProperty("queryParams")
	public List<PageQueryParam> pageQueryParams = null;

}