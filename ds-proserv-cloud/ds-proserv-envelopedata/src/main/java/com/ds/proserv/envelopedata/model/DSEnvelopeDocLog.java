package com.ds.proserv.envelopedata.model;

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
@Table(name = "dsenvelopedoclog")
public class DSEnvelopeDocLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1543702516094067853L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "timegenerated")
	private LocalDateTime timeGenerated;

	@Column(name = "docdownloaded")
	private Boolean docDownloaded;

	@Column(name = "docdownloadstatus")
	private String docDownloadStatus;

	@Column(name = "docdownloaddatetime")
	private LocalDateTime docDownloadDateTime;

	@Column(name = "docdownloadfailurereason")
	private String docDownloadFailureReason;
}