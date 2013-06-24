package com.gmi.nordborglab.browser.server.domain.acl;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MapsId;


@Embeddable
public class AuthorityPK implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    @Column(name = "user_id")
    private Long userId;
    private String authority;

    public AuthorityPK() {
    }

    public AuthorityPK(Long userId, String authority) {
        this.userId = userId;
        this.authority = authority;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

}
