package com.ds.proserv.coredata.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "createddatetime", "updateddatetime", "createdby", "updatedby" }, allowGetters = true)
@Slf4j
public abstract class AuditData<ID> implements Serializable, Persistable<ID> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5957359283884761164L;

	@Column(name = "createddatetime", nullable = false, updatable = false)
	@CreatedDate
	private LocalDateTime createdDateTime;

	@Column(name = "updateddatetime", insertable = false)
	@LastModifiedDate
	private LocalDateTime updatedDateTime;

	@Column(name = "createdby", nullable = false, updatable = false)
	@CreatedBy
	private String createdBy;

	@Column(name = "updatedby", insertable = false)
	@LastModifiedBy
	private String updatedBy;

	@Override
	public boolean isNew() {

		log.debug("CoreData AuditData.getUpdatedBy() for {} is {}", getId(), getCreatedDateTime());
		log.debug("CoreData AuditData.getCreatedBy() for {} is {}", getId(), getCreatedDateTime());
		return (null == this.getCreatedDateTime());
	}
}