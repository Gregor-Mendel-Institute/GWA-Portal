package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.acl.PermissionPrincipal;

public class CustomAccessControlEntry {

	private Long id;
	private int mask; 
	private boolean granting; 
	private PermissionPrincipal principal;
	
	public CustomAccessControlEntry() {
		
	}
	
	public CustomAccessControlEntry(Long id, int mask,boolean granting) {
		this(id,mask,granting,null);
	}
	
	public CustomAccessControlEntry(Long id,int mask,boolean granting,PermissionPrincipal principal) {
		this.id = id;
		this.mask = mask;
		this.granting = granting;
		this.principal = principal;
	}
	
	public Long getId() {
		return id; 
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	
	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
	}

	public boolean getIsGranting() {
		return granting;
	}
	
	public void setIsGranting(boolean granting) {
		this.granting = granting;
	}

	public void setGranting(boolean granting) {
		this.granting = granting;
	}
	
	public PermissionPrincipal getPrincipal() {
		return principal;
	}
	
	public void setPrincipal(PermissionPrincipal principal) { 
		this.principal = principal;
	}
}
