package com.gmi.nordborglab.browser.server.domain.acl;

public class PermissionPrincipal {

	protected String id;
	protected String name;
	protected boolean isUser = true;
	
	public PermissionPrincipal() {}
	
	public PermissionPrincipal(String id, String name, boolean isUser) {
		this.id = id;
		this.name = name;
		this.isUser = isUser;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsUser() {
		return isUser;
	}

	public void setIsUser(boolean isUser) {
		this.isUser = isUser;
	}
	
}
