package com.ds.proserv.feign.envelopeapi.domain;

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
@JsonPropertyOrder({ "applicationId", "applicationType", "recipients" })
public class GenericEnvelopeMessageDefinition implements IDocuSignInformation {

	@JsonProperty("applicationId")
	private Long applicationId;
	@JsonProperty("applicationType")
	private String applicationType;
	@JsonProperty("recipients")
	private List<Recipient> recipients = null;

}