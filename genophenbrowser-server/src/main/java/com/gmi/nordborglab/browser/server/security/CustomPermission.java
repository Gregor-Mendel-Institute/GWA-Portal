package com.gmi.nordborglab.browser.server.security;

import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.model.Permission;

public class CustomPermission extends AbstractPermission {

    public static final Permission READ = new CustomPermission(1 << 0, 'R'); // 1
    public static final Permission EDIT = new CustomPermission(1 << 1, 'E'); // 2
    public static final Permission ADMINISTRATION = new CustomPermission(1 << 2, 'A'); // 4
    public static final Permission USE = new CustomPermission(1 << 3, 'U'); // 8

    public CustomPermission(int mask) {
        super(mask);
    }

    protected CustomPermission(int mask, char code) {
        super(mask, code);
    }
}
