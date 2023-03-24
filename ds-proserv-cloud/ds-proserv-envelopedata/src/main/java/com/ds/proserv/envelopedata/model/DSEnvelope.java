package com.ds.proserv.envelopedata.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dsenvelope")
public class DSEnvelope extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3213895086570100912L;

	@Id
	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "envelopesubject")
	private String envelopeSubject;

	@Column(name = "status")
	private String status;

	@Column(name = "sentdatetime")
	private LocalDateTime sentDateTime;

	@Column(name = "delivereddatetime")
	private LocalDateTime deliveredDateTime;

	@Column(name = "completeddatetime")
	private LocalDateTime completedDateTime;

	@Column(name = "declineddatetime")
	private LocalDateTime declinedDateTime;

	@Column(name = "senderemail")
	private String senderEmail;

	@Column(name = "sendername")
	private String senderName;

	@Column(name = "terminalreason")
	private String terminalReason;

	@Column(name = "timezone")
	private String timeZone;

	@Column(name = "timezoneoffset")
	private Long timeZoneoffset;

	@Column(name = "timegenerated")
	private LocalDateTime timeGenerated;

	@Column(name = "filenames")
	private String fileNames;

	@Override
	public String getId() {

		return this.envelopeId;
	}
}