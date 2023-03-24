package com.ds.proserv.notificationdata.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificationdetaillog")
public class NotificationDetailLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3955444924536410527L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "notificationid")
	private String notificationId;

	@Column(name = "notificationtopic")
	private String notificationTopic;

	@Column(name = "notificationtype")
	private String notificationType;// DeadQueue,Failurelog,ApplicationEnvelopefailure,FolderReadyForCLM

	@Column(name = "communicationmode")
	private String communicationMode;// sms, email

	@Column(name = "recipientids")
	private String recipientIds;// emails or mobile numbers

	@Column(name = "notificationstatus")
	private String notificationStatus;// sent

	@Column(name = "clientcredentialid")
	private String clientCredentialId;

	@Column(name = "notificationsenttimestamp")
	private LocalDateTime notificationSentTimestamp;

	@Override
	public String getId() {

		return this.notificationId;
	}
}