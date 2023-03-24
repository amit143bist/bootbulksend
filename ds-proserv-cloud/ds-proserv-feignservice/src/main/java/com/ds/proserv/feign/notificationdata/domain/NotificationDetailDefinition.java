package com.ds.proserv.feign.notificationdata.domain;

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
@JsonPropertyOrder({ "notificationId", "notificationTopic", "notificationType", "communicationMode", "recipientIds",
		"notificationStatus", "clientCredentialId", "notificationSentTimestamp" })
public class NotificationDetailDefinition implements IDocuSignInformation {

	@JsonProperty("notificationId")
	private String notificationId;

	@JsonProperty("notificationTopic")
	private String notificationTopic;

	@JsonProperty("notificationType")
	private String notificationType;// DeadQueue,Failurelog,ApplicationEnvelopefailure,FolderReadyForCLM

	@JsonProperty("communicationMode")
	private String communicationMode;// sms, email

	@JsonProperty("recipientIds")
	private String recipientIds;// emails or mobile numbers

	@JsonProperty("notificationStatus")
	private String notificationStatus;// sent

	@JsonProperty("clientCredentialId")
	private String clientCredentialId;

	@JsonProperty("notificationSentTimestamp")
	private String notificationSentTimestamp;
}