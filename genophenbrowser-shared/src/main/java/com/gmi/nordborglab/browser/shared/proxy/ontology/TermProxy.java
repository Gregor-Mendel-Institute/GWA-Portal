package com.gmi.nordborglab.browser.shared.proxy.ontology;

import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.jpaontology.model.Term",locator="com.gmi.nordborglab.browser.server.service.SpringOntologyEntityLocator")
public interface TermProxy extends EntityProxy {

	public Integer getId();
	public String getName();
	
	public String getTermType();
	public String getAcc();
	public TermDefinitionProxy getTermDefinition();
	public Boolean getIsObsolete();
	public Boolean getIsRoot() ;
	public Boolean getIsRelation();
	
	public Set<Term2TermProxy> getParents();
	public Set<Term2TermProxy> getChilds();
}
