package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.jpaontology.model.TermDefinition",locator="com.gmi.nordborglab.browser.server.service.SpringOntologyEntitiyLocator")
public interface TermDefinitionProxy extends EntityProxy {

	
	public String getTermDefinition();

	
	public String getTermComment();

	
	public String getReference();
	
}
