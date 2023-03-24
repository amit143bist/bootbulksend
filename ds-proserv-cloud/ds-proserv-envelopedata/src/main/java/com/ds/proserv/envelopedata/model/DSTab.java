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
@Table(name = "dstab")
public class DSTab extends AuditData<String> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3477652190512853169L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "recipientid")
	private String recipientId;

	@Column(name = "tablabel")
	private String tabLabel;

	@Column(name = "tabname")
	private String tabName;

	@Column(name = "tabvalue")
	private String tabValue;
	
	@Column(name = "tabstatus")
	private String tabStatus;

	@Column(name = "taboriginalvalue")
	private String tabOriginalValue;

}