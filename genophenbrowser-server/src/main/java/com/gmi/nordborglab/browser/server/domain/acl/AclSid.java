package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="acl_sid", schema="acl")
public class AclSid {

	@Id
	@Column(unique=true,nullable=false)
	private Long id;
	
	private boolean principal;
	@Column(unique=true)
	private String sid;
	
	public Long getId() {
		return id;
	}
	
	public String getSid() {
		return sid;
	}
	
	public boolean getPrincipal() {
		return principal;
	}
	
	 
}
