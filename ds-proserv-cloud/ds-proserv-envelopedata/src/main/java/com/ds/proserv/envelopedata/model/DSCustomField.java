package com.ds.proserv.envelopedata.model;

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
@Table(name = "dscustomfield")
public class DSCustomField extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2295864271117794182L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "recipientid")
	private String recipientId;

	@Column(name = "documentid")
	private Long documentId;

	@Column(name = "documentname")
	private String documentName;

	@Column(name = "documentsequence")
	private Long documentSequence;

	@Column(name = "fieldname")
	private String fieldName;

	@Column(name = "fieldvalue")
	private String fieldValue;

	@Column(name = "fieldtype")
	private String fieldType;
}