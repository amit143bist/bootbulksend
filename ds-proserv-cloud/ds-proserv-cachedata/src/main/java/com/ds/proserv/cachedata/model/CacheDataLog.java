package com.ds.proserv.cachedata.model;

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
@Table(name = "cachedatalog")
public class CacheDataLog extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6236833973894023823L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "cacheid")
	private String cacheId;

	@Column(name = "cachekey")
	private String cacheKey;

	@Column(name = "cachevalue")
	private String cacheValue;

	@Column(name = "cachereference")
	private String cacheReference;

	@Override
	public String getId() {

		return this.cacheId;
	}

}