package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AclEntry {
	@Id
	@Column(unique=true,nullable=false)
	private Long id;
	
	private Integer mask;
	private boolean granting;
	private boolean audit_success;
	private boolean audit_failure;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="sid")
	private AclSid sid;
	
	
	public Long getId() {
		return id;
	}
	
	public AclSid getSid() {
		return sid;
	}
	public Integer getMask() {
		return mask;
	}
	public boolean isGranting() {
		return granting;
	}
	public boolean isAudit_success() {
		return audit_success;
	}
	public boolean isAudit_failure() {
		return audit_failure;
	}
}
