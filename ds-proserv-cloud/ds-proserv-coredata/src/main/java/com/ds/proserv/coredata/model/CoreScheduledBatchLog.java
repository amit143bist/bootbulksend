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
@Table(name = "corescheduledbatchlog")
public class CoreScheduledBatchLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6231169563307669261L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "batchid")
	private String batchId;

	@Column(name = "batchtype")
	private String batchType;

	@Column(name = "batchstartdatetime")
	private LocalDateTime batchStartDateTime;

	@Column(name = "batchenddatetime")
	private LocalDateTime batchEndDateTime;

	@Column(name = "batchstartparameters")
	private String batchStartParameters;

	@Column(name = "totalrecords")
	private Long totalRecords;

	@Override
	public String getId() {

		return this.batchId;
	}
}