package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.proxy.annotation.SNPAnnotationProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.annotation.SNPInfo")
public interface SNPInfoProxy extends ValueProxy {

    long getPosition();

    String getChr();

    Boolean isInGene();

    String getAlt();

    String getRef();

    String getAnc();

    List<SNPAnnotationProxy> getAnnotations();

    String getGene();

    Integer getRefCount();

    Integer getAltCount();
}
