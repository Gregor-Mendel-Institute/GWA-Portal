package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created by uemit.seren on 1/15/16.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.meta.Association")
public interface AssociationProxy extends ValueProxy {

    SNPInfoProxy getSnpInfo();

    Double getPValue();

    boolean isOverFDR();

    Double getMaf();

    Integer getMac();
}
