package com.ds.proserv.envelopedata.domain;

import java.util.List;

import com.ds.proserv.envelopedata.model.DSEnvelope;
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
@JsonPropertyOrder({ "dsEnvelopes" })
public class EnvelopeSPRequest {

	@JsonProperty("dsEnvelopes")
	private List<DSEnvelope> dsEnvelopes = null;
}