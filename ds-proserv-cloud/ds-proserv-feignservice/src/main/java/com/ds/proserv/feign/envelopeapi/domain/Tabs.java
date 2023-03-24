
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
@JsonPropertyOrder({ "textTabs", "checkboxTabs" })
public class Tabs {

	@JsonProperty("textTabs")
	private List<TextTab> textTabs;
	@JsonProperty("checkboxTabs")
	private List<CheckboxTab> checkboxTabs;

	public Tabs() {
		textTabs = new ArrayList<>();
		checkboxTabs = new ArrayList<>();
	}
}