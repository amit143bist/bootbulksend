package com.ds.proserv.feign.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractDocuSignInformation implements IDocuSignInformation {

	@JsonProperty("batchId")
	private String batchId = null;
	@JsonProperty("processId")
	private String processId = null;
	@JsonProperty("groupId")
	private String groupId = null;
	@JsonProperty("totalPages")
	private Long totalPages = null;
	@JsonProperty("currentPage")
	private Long currentPage = null;
	@JsonProperty("totalRecords")
	private Long totalRecords = null;
	@JsonProperty("nextAvailable")
	private Boolean nextAvailable = null;
	@JsonProperty("contentAvailable")
	private Boolean contentAvailable = null;
	@JsonProperty("previousAvailable")
	private Boolean previousAvailable = null;
	@JsonProperty("nextUri")
	private String nextUri = null;
}