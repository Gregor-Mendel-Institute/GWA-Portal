package com.gmi.nordborglab.browser.server.domain.acl;

import java.io.Serializable;

import javax.persistence.Embeddable;


@Embeddable
public class AuthorityPK implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
	private String authority;
	
	public AuthorityPK() { }
	public AuthorityPK(String username,String authority) {
		this.username = username;
		this.authority = authority;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	public String getAuthority() {
		return authority;
	}
	
}
