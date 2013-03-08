package com.gmi.nordborglab.browser.server.security;

import org.springframework.security.acls.domain.DefaultPermissionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/28/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomPermissionFactory extends DefaultPermissionFactory{

    public CustomPermissionFactory() {
        super(CustomPermission.class);
    }
}
