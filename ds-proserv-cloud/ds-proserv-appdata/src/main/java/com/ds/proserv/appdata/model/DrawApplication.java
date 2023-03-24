package com.ds.proserv.appdata.model;

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
@Table(name = "drawapplication")
public class DrawApplication extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978210398932482077L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "applicationid")
	private String applicationId;

	@Column(name = "triggerenvelopeid")
	private String triggerEnvelopeId;

	@Column(name = "bridgeenvelopeid")
	private String bridgeEnvelopeId;

	@Column(name = "bulkbatchid")
	private String bulkBatchId;

	@Column(name = "programtype")
	private String programType;

	@Column(name = "drawreference")
	private String drawReference;

	@Column(name = "languagecode")
	private String languageCode;

	@Column(name = "agentcode")
	private String agentCode;

	@Column(name = "duplicaterecord")
	private Boolean duplicateRecord;

	@Column(name = "applicationstatus")
	private String applicationStatus;

	@Override
	public String getId() {

		return this.applicationId;
	}

}