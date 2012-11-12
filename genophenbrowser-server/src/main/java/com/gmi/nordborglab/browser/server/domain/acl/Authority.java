package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(schema="acl",name="authorities")
public class Authority  {
	
	
	@EmbeddedId
	private AuthorityPK id;

	
	@MapsId("username")
	@JoinColumn(name="username")
	@ManyToOne() private AppUser user;
	

    public Authority() {
    	this.id = new AuthorityPK();
    }
    
    public Authority(AppUser user,String authority) {
    	this.id = new AuthorityPK(user.getUsername(),authority);
    }

    public String getAuthority() {
        return id.getAuthority();
    }
    public void setAuthority(String authority) {
    	this.id.setAuthority(authority);
    }
    
    public AppUser getUser() {
    	return user;
    }
    
    public void setUser(AppUser user) {
    	this.user = user;
    }
}
