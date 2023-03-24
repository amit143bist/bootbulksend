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
@JsonPropertyOrder({ "batchId", "batchSize", "sent", "queued", "failed", "envelopeIdOrTemplateId", "mailingListId",
		"ownerUserId", "senderUserId", "batchName", "submittedDate", "envelopesUri", "bulkErrors" })
public class BulkSendBatchMonitorResponse {

	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("batchSize")
	private String batchSize;
	@JsonProperty("sent")
	private String sent;
	@JsonProperty("queued")
	private String queued;
	@JsonProperty("failed")
	private String failed;
	@JsonProperty("envelopeIdOrTemplateId")
	private String envelopeIdOrTemplateId;
	@JsonProperty("mailingListId")
	private String mailingListId;
	@JsonProperty("ownerUserId")
	private String ownerUserId;
	@JsonProperty("senderUserId")
	private String senderUserId;
	@JsonProperty("batchName")
	private String batchName;
	@JsonProperty("submittedDate")
	private String submittedDate;
	@JsonProperty("envelopesUri")
	private String envelopesUri;
	@JsonProperty("bulkErrors")
	private List<BulkError> bulkErrors = null;

}