package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo")
public interface SNPAlleleInfoProxy extends ValueProxy {

    SNPInfoProxy getSnpInfo();

    List<Byte> getAlleles();

    List<PassportProxy> getPassports();
}
