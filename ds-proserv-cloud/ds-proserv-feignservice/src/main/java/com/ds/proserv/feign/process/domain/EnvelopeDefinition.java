package com.ds.proserv.feign.process.domain;

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
@JsonPropertyOrder({ "compositeTemplates", "status", "emailSubject" })
public class EnvelopeDefinition implements IDocuSignInformation{

	@JsonProperty("compositeTemplates")
	private List<CompositeTemplate> compositeTemplates = null;
	@JsonProperty("status")
	private String status;
	@JsonProperty("emailSubject")
	private String emailSubject;

}