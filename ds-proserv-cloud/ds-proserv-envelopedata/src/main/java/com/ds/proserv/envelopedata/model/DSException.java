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
@Table(name = "dsexception")
public class DSException extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1267237401899242839L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "exceptionreason")
	private String exceptionReason;

	@Column(name = "envelopeid")
	private String envelopeId;

	@Column(name = "envelopexml")
	private String envelopeXml;

	@Column(name = "retrystatus")
	private String retryStatus;

	@Column(name = "retrycount")
	private Long retryCount;

	@Column(name = "retrydatetime")
	private LocalDateTime retryDateTime;

	@Column(name = "exceptioncode")
	private String exceptionCode;

	@Column(name = "exceptionstep")
	private String exceptionStep;

	@Column(name = "exceptiondatetime")
	private LocalDateTime exceptionDateTime;

}