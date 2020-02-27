package com.gmi.nordborglab.browser.server.domain.acl;

import java.util.List;

public class SearchPermissionUserRole {

	
	public SearchPermissionUserRole() {principals=null;}
	
	
	private List<PermissionPrincipal> principals;
	
	
	public List<PermissionPrincipal> getPrincipals() {
		return principals;
	}
	public void setPrincipals(List<PermissionPrincipal> principals) {
		this.principals = principals;
	}
	
}
