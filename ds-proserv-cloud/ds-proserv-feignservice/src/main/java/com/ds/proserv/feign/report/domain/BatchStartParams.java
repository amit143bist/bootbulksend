package com.ds.proserv.feign.report.domain;

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
@JsonPropertyOrder({ "beginDateTime", "endDateTime", "totalRecordIds" })
public class BatchStartParams implements IDocuSignInformation{

	@JsonProperty("beginDateTime")
	private String beginDateTime;
	@JsonProperty("endDateTime")
	private String endDateTime;
	@JsonProperty("totalRecordIds")
	private Integer totalRecordIds;
}