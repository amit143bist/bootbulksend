package com.ds.proserv.feign.appdata.domain;

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
@JsonPropertyOrder({ "totalRecords", "drawApplicationIds" })
public class DrawApplicationIdResult implements IDocuSignInformation {

	@JsonProperty("totalRecords")
	private Long totalRecords = null;
	@JsonProperty("drawApplicationIds")
	private List<String> drawApplicationIds = null;
}