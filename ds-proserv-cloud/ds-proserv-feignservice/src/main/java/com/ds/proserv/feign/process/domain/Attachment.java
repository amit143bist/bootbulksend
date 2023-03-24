package com.ds.proserv.feign.process.domain;

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
@JsonPropertyOrder({ "attachmentId", "label", "attachmentType", "name", "accessControl", "data" })
public class Attachment implements IDocuSignInformation {

	@JsonProperty("attachmentId")
	private String attachmentId;
	@JsonProperty("label")
	private String label;
	@JsonProperty("attachmentType")
	private String attachmentType;
	@JsonProperty("name")
	private String name;
	@JsonProperty("accessControl")
	private String accessControl;
	@JsonProperty("data")
	private String data;

}