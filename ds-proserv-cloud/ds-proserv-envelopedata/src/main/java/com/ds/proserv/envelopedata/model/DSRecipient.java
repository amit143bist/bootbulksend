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
@Table(name = "dsrecipient")
public class DSRecipient extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -219786250456928350L;

	@Id
	@Column(name = "recipientid")
	private String recipientId;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "status")
	private String status;

	@Column(name = "routingorder")
	private Long routingOrder;

	@Column(name = "recipientemail")
	private String recipientEmail;

	@Column(name = "recipientname")
	private String recipientName;

	@Column(name = "declinereason")
	private String declineReason;

	@Column(name = "recipientipaddress")
	private String recipientIPAddress;
	
	@Column(name = "clientuserid")
	private String clientUserId;

	@Column(name = "sentdatetime")
	private LocalDateTime sentDateTime;

	@Column(name = "delivereddatetime")
	private LocalDateTime deliveredDateTime;

	@Column(name = "signeddatetime")
	private LocalDateTime signedDateTime;

	@Column(name = "declineddatetime")
	private LocalDateTime declinedDateTime;

	@Override
	public String getId() {

		return this.recipientId;
	}

}