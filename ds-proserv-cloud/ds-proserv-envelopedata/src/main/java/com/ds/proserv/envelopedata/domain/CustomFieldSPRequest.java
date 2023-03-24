package com.ds.proserv.envelopedata.domain;

import java.util.List;

import com.ds.proserv.envelopedata.model.DSCustomField;
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
@JsonPropertyOrder({ "dsCustomFields" })
public class CustomFieldSPRequest {

	@JsonProperty("dsCustomFields")
	private List<DSCustomField> dsCustomFields = null;
}