package com.ds.proserv.appdata.model;

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
@Table(name = "foldernotificationlog")
public class FolderNotificationLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7327206393617377277L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;
	@Column(name = "eventtype")
	private String eventType;
	@Column(name = "foldername")
	private String folderName;
	@Column(name = "filecount")
	private Long fileCount;
	@Column(name = "eventtimestamp")
	private LocalDateTime eventTimestamp;
}