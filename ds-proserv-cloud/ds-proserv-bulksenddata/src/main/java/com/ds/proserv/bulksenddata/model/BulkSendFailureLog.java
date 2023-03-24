package com.ds.proserv.bulksenddata.model;

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
@Table(name = "bulksendfailurelog")
//Failure during BulkSend
public class BulkSendFailureLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7712070353818442894L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "errormessage")
	private String errorMessage;

	@Column(name = "applicationids")
	private String applicationIds;

	@Column(name = "batchsize")
	private Long batchSize;

	@Column(name = "batchfailuredatetime")
	private LocalDateTime batchFailureDateTime;

}