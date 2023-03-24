
package com.ds.proserv.feign.envelopeapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "email", "name", "recipientId", "roleName", "tabs" })
public class Signer {

	@JsonProperty("email")
	private String email;
	@JsonProperty("name")
	private String name;
	@JsonProperty("recipientId")
	private String recipientId;
	@JsonProperty("roleName")
	private String roleName;
	@JsonProperty("tabs")
	private Tabs tabs;

	public Signer() {
		tabs = new Tabs();
	}
}