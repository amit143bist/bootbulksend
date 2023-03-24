package com.ds.proserv.appdata.model;

import java.time.LocalDate;
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
@Table(name = "customenvelopedata")
public class CustomEnvelopeData extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5609546007176679729L;

	@Id
	@Column(name = "envelopeid")
	private String envelopeId;
	@Column(name = "envdate")
	private LocalDate envDate;
	@Column(name = "envtimestamp")
	private LocalDateTime envTimeStamp;
	@Column(name = "senderidentifier")
	private String senderIdentifier;
	@Column(name = "downloadbucketname")
	private String downloadBucketName;
	@Column(name = "docdownloadstatusflag")
	private String docDownloadStatusFlag;
	@Column(name = "docdownloadtimestamp")
	private LocalDateTime docDownloadTimeStamp;
	@Column(name = "envprocessstatusflag")
	private String envProcessStatusFlag;
	@Column(name = "envprocessstartdatetime")
	private LocalDateTime envProcessStartDateTime;
	@Column(name = "envprocessenddatetime")
	private LocalDateTime envProcessEndDateTime;

	@Override
	public String getId() {

		return this.envelopeId;
	}

}