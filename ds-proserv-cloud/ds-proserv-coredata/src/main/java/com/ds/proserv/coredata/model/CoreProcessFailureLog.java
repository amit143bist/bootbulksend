package com.ds.proserv.coredata.model;

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
@Table(name = "coreprocessfailurelog")
public class CoreProcessFailureLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2729284565481864993L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "processfailureid")
	private String processFailureId;

	@Column(name = "batchid")
	private String batchId;

	@Column(name = "processid")
	private String processId;

	@Column(name = "failurecode")
	private String failureCode;

	@Column(name = "failurereason")
	private String failureReason;

	@Column(name = "failuredatetime")
	private LocalDateTime failureDateTime;

	@Column(name = "successdatetime")
	private LocalDateTime successDateTime;

	@Column(name = "failurerecordid")
	private String failureRecordId;

	@Column(name = "failurestep")
	private String failureStep;

	@Column(name = "retrystatus")
	private String retryStatus;

	@Column(name = "retrycount")
	private Long retryCount;

	@Override
	public String getId() {

		return this.processFailureId;
	}
}