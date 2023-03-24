package com.ds.proserv.bulksenddata.model;

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
@Table(name = "bulksendenvelopelog")
//To Save all envelopeIds for a bulkSend batch
public class BulkSendEnvelopeLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7777328239488709416L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "bulkbatchid")
	private String bulkBatchId;

	@Column(name = "envelopeid")
	private String envelopeId;

}