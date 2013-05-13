package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.ontology.BasicTerm2Term", locator="com.gmi.nordborglab.browser.server.service.SpringGraphOntologyEntityLocator")
public interface GraphTerm2TermProxy extends EntityProxy {
    public Long getNodeId();
    public String getType();
    public GraphTermProxy getParent();
    public GraphTermProxy getChild();
}
