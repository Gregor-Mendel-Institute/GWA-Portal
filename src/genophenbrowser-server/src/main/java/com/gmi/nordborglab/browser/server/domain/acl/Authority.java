package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(schema = "acl", name = "authorities")
public class Authority {


    @EmbeddedId
    private AuthorityPK id;


    @ManyToOne(cascade = CascadeType.PERSIST)
    @MapsId("userId")
    private AppUser user;


    public Authority() {
        this.id = new AuthorityPK();
    }

    public Authority(AppUser user, String authority) {
        this.id = new AuthorityPK(user.getId(), authority);
    }

    public String getAuthority() {
        return id.getAuthority();
    }

    public void setAuthority(String authority) {
        this.id.setAuthority(authority);
    }

    public Long getUserId() {
        return this.id.getUserId();
    }

    public void setUserId(Long id) {
        this.id.setUserId(id);
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }
}
