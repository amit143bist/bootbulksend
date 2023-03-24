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
@JsonPropertyOrder({ "name", "order", "display", "documentId", "fileExtension", "documentBase64" })
public class Document implements IDocuSignInformation {

	@JsonProperty("name")
	private String name;
	@JsonProperty("order")
	private Integer order;
	@JsonProperty("display")
	private String display;
	@JsonProperty("documentId")
	private String documentId;
	@JsonProperty("fileExtension")
	private String fileExtension;
	@JsonProperty("documentBase64")
	private String documentBase64;

}