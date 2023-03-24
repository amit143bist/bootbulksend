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
@Table(name = "coreconcurrentprocesslog")
public class CoreConcurrentProcessLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4628517513041385132L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "processid")
	private String processId;

	@Column(name = "batchid")
	private String batchId;

	@Column(name = "processstartdatetime")
	private LocalDateTime processStartDateTime;

	@Column(name = "processenddatetime")
	private LocalDateTime processEndDateTime;

	@Column(name = "processstatus")
	private String processStatus;

	@Column(name = "totalrecordsinprocess")
	private Long totalRecordsInProcess;

	@Column(name = "groupid")
	private String groupId;

	@Column(name = "accountid")
	private String accountId;

	@Column(name = "userid")
	private String userId;

	@Override
	public String getId() {

		return this.processId;
	}
}