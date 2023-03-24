package com.ds.proserv.appdata.model;

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
@Table(name = "applicationenvelopedata")
public class ApplicationEnvelopeData extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5030285206428149885L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "applicationid")
	private String applicationId;

	@Column(name = "applicationtype")
	private String applicationType;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "recipientemails")
	private String recipientEmails;

	@Column(name = "failurereason")
	private String failureReason;

	@Column(name = "failuretimestamp")
	private LocalDateTime failureTimestamp;

	@Column(name = "envelopesenttimestamp")
	private LocalDateTime envelopeSentTimestamp;
	
	@Column(name = "communitypartnercode")
	private String communityPartnerCode;

}