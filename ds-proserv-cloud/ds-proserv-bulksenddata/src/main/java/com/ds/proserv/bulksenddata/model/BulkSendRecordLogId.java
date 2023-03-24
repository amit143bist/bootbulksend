package com.ds.proserv.bulksenddata.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BulkSendRecordLogId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6991706672580123080L;

	@Column(name = "recordid")
	private String recordId;

	@Column(name = "recordtype")
	private String recordType;
}