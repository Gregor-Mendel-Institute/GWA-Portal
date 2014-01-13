package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 01.08.13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.jpaontology.model.TermPage")
public interface TermPageProxy extends ValueProxy {


    public long getTotalElements();

    public List<TermProxy> getContents();
}
