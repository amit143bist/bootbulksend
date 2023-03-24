package com.ds.proserv.feign.envelopeapi.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "fullName", "email", "roleName", "languagePreference", "items" })
public class Recipient {

	@JsonProperty("fullName")
	private String fullName;
	@JsonProperty("email")
	private String email;
	@JsonProperty("roleName")
	private String roleName;
	@JsonProperty("languagePreference")
	private String languagePreference;
	@JsonProperty("items")
	private List<Item> items;

	public Recipient() {

		items = new ArrayList<>();
	}
}