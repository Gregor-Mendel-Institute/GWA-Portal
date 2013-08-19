package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.*;

@Entity
@Table(name = "acl_sid", schema = "acl")
@SequenceGenerator(name = "idSequence", sequenceName = "acl.acl_sid_id_seq", allocationSize = 1)
public class AclSid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSequence")
    @Column(unique = true, nullable = false)
    private Long id;

    private boolean principal;
    @Column(unique = true)
    private String sid;

    public AclSid() {

    }

    public AclSid(boolean principal, String sid) {
        this.principal = principal;
        this.sid = sid;
    }

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
