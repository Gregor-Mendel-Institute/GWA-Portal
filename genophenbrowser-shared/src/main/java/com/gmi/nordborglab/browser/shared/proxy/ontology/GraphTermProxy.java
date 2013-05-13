package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.ontology.Term",locator="com.gmi.nordborglab.browser.server.service.SpringGraphOntologyEntityLocator")
public interface GraphTermProxy extends EntityProxy {
    public Long getNodeId();
    public String getId();
    public String getDefinition();
    public String getName();
    public String getType();
    public String getComment();
    List<Long> getPathToRoot();

    Set<GraphTerm2TermProxy> getParents();
    Set<GraphTerm2TermProxy> getChildren();

    public int getChildCount();
}
