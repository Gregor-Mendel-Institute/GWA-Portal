package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.jpaontology.model.Term2Term",locator="com.gmi.nordborglab.browser.server.service.SpringOntologyEntityLocator")
public interface Term2TermProxy extends EntityProxy {

	public Integer getId();
	
	//public TermProxy getRelationshipType();
	
	public TermProxy getParent();

	
	public TermProxy getChild();

	public Boolean getComplete();

    TermProxy getRelationshipType();
}
