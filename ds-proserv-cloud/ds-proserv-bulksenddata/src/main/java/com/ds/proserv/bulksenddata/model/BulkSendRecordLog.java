package com.ds.proserv.bulksenddata.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
@Table(name = "bulksendrecordlog")
public class BulkSendRecordLog extends AuditData<BulkSendRecordLogId> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8133207512546995315L;

	@EmbeddedId
	private BulkSendRecordLogId bulkSendRecordLogId;

	@Column(name = "bulkbatchid")
	private String bulkBatchId;

	@Column(name = "startdatetime")
	private LocalDateTime startDateTime;

	@Column(name = "enddatetime")
	private LocalDateTime endDateTime;

	@Override
	public BulkSendRecordLogId getId() {

		return bulkSendRecordLogId;
	}
}