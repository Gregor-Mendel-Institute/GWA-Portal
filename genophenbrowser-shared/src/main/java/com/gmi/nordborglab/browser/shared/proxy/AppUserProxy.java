package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;


@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.acl.AppUser")
public interface AppUserProxy extends ValueProxy {

    Long getId();

    String getUsername();

    String getFirstname();

    void setFirstname(String firstname);

    String getLastname();

    void setLastname(String lastname);

    String getEmail();

    void setEmail(String email);

    List<AuthorityProxy> getAuthorities();

    void setAuthorities(List<AuthorityProxy> authorities);
}
