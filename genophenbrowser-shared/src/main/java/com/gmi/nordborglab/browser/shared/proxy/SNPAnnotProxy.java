package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot")
public interface SNPAnnotProxy extends ValueProxy {

    public long getPosition();

    public String getChr();

    public Boolean isInGene();

    public String getAlt();

    public String getRef();

    public String getLyr();

    public String getAnnotation();

    public String getGene();
}
