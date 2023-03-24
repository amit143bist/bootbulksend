package com.ds.proserv.notificationdata.model;

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
@Table(name = "clientcredential")
public class ClientCredential extends AuditData<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8382532703166825529L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "credentialid")
	private String credentialId;

	@Column(name = "credentialtype")
	private String credentialType;// Mobile or Email

	@Column(name = "credentialvendor")
	private String credentialVendor;// Gmail, Twilio

	@Column(name = "applicationidentifier")
	private String applicationIdentifier;

	@Column(name = "applicationname")
	private String applicationName;

	@Column(name = "refreshtoken")
	private String refreshToken;

	@Column(name = "accesstoken")
	private String accessToken;

	@Column(name = "tokentype")
	private String tokenType;

	@Column(name = "expiresin")
	private Long expiresIn;
	
	@Override
	public String getId() {

		return this.credentialId;
	}

}