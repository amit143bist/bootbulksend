package com.ds.proserv.bulksendmonitor.batch.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "errorMessage", "recipientEmails", "created" })
public class BulkError {

	@JsonProperty("errorMessage")
	private String errorMessage;
	@JsonProperty("recipientEmails")
	private List<String> recipientEmails = null;
	@JsonProperty("created")
	private String created;
}