package com.ds.proserv.bulksenddata.model;

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
@Table(name = "bulksendprocesslog")
//Success during BulkSend
public class BulkSendProcessLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2374987113405974610L;

	@Id
	@Column(name = "batchid")
	private String batchId;

	@Column(name = "mailinglistid")
	private String mailingListId;

	@Column(name = "batchname")
	private String batchName;

	@Column(name = "batchsize")
	private Long batchSize;

	@Column(name = "batchstatus")
	private String batchStatus;

	@Column(name = "successsize")
	private Long successSize;

	@Column(name = "failedsize")
	private Long failedSize;

	@Column(name = "queuesize")
	private Long queueSize;

	@Column(name = "batchsubmitteddatetime")
	private LocalDateTime batchSubmittedDateTime;
	
	@Column(name = "bulkerrors")
	private String bulkErrors;

	@Override
	public String getId() {

		return this.batchId;
	}
}