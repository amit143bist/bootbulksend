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
@JsonPropertyOrder({ "attachments", "recipients", "status", "emailSubject", "documents" })
public class EnvelopeArchiveDefinition implements IDocuSignInformation {

	@JsonProperty("attachments")
	private List<Attachment> attachments = null;
	@JsonProperty("recipients")
	private Recipients recipients;
	@JsonProperty("status")
	private String status;
	@JsonProperty("emailSubject")
	private String emailSubject;
	@JsonProperty("documents")
	private List<Document> documents = null;

}