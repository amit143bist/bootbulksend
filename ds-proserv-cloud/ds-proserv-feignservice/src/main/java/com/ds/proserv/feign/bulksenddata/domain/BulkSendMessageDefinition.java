package com.ds.proserv.feign.bulksenddata.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
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
@JsonPropertyOrder({ "recordIds", "processId", "batchId", "groupId", "userId", "accountId", "queryIdentifier",
		"useTemplate", "selectRecordDataQueryType", "selectRecordDataQueryTypePrimaryKeyName",
		"updateRecordDataQueryType", "updateRecordDataQueryTypePrimaryKeyName", "draftEnvelopeIdOrTemplateId",
		"ruleEngineDefinition", "baseUri", "startDateTime", "endDateTime" })
public class BulkSendMessageDefinition implements IDocuSignInformation {

	@JsonProperty("recordIds")
	private List<String> recordIds;

	@JsonProperty("processId")
	private String processId;

	@JsonProperty("batchId")
	private String batchId;

	@JsonProperty("groupId")
	private String groupId;

	@JsonProperty("userId")
	private String userId;

	@JsonProperty("accountId")
	private String accountId;

	@JsonProperty("queryIdentifier")
	private String queryIdentifier;

	@JsonProperty("useTemplate")
	private Boolean useTemplate;

	@JsonProperty("selectRecordDataQueryType")
	private String selectRecordDataQueryType;

	@JsonProperty("selectRecordDataQueryTypePrimaryKeyName")
	private String selectRecordDataQueryTypePrimaryKeyName;

	@JsonProperty("updateRecordDataQueryType")
	private String updateRecordDataQueryType;

	@JsonProperty("updateRecordDataQueryTypePrimaryKeyName")
	private String updateRecordDataQueryTypePrimaryKeyName;

	@JsonProperty("draftEnvelopeIdOrTemplateId")
	private String draftEnvelopeIdOrTemplateId;

	@JsonProperty("ruleEngineDefinition")
	private RuleEngineDefinition ruleEngineDefinition;

	@JsonProperty("baseUri")
	private String baseUri;

	@JsonProperty("startDateTime")
	private String startDateTime;

	@JsonProperty("endDateTime")
	private String endDateTime;
}