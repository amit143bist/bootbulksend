package com.ds.proserv.feign.dsapi.domain;

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
@JsonPropertyOrder({ "name", "value" })
public class CustomSetting {

	@JsonProperty("name")
	private String name;
	@JsonProperty("value")
	private String value;

}