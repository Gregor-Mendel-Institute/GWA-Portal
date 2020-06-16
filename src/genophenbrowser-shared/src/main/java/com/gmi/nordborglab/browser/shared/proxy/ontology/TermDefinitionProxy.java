package com.gmi.nordborglab.browser.shared.proxy.ontology;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.gmi.nordborglab.jpaontology.model.TermDefinition")
public interface TermDefinitionProxy extends ValueProxy {


    public String getTermDefinition();


    public String getTermComment();


    public String getReference();

}
