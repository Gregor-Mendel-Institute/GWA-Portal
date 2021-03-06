package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface UnitOfMeasureProxy extends EntityProxy {

	Long getId();
	void setId(Long id);
	String getUnitType();
	void setUnitType(String unitType);
	
}
