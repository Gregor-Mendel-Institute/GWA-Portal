package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.genotype.PolyType", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PolyTypeProxy extends EntityProxy {

	public Long getId();
	
	public String getPolyType();

	public void setPolyType(String polyType);
}
