package com.gmi.nordborglab.browser.shared.proxy.annotation;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 7:14 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.annotation.Gene")
public interface GeneProxy extends ValueProxy {

    public String getName();

    public long getStart();

    public long getEnd();

    public int getStrand();

    public String getChr();

}
