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
@Table(name = "dsrecipientauth")
public class DSRecipientAuth extends AuditData<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521759740934633392L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;
	
	@Column(name = "envelopeid")
	private String envelopeId;
	
	@Column(name = "recipientid")
	private String recipientId;

	@Column(name = "type")
	private String type;
	
	@Column(name = "status")
	private String status;

	@Column(name = "eventdatetime")
	private LocalDateTime eventDateTime;
}