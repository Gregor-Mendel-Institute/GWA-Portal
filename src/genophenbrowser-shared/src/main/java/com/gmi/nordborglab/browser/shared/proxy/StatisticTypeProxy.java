package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StatisticTypeProxy extends EntityProxy {

	public void setId(Long id);
	public Long getId();

	public String getStatType();
	public void setStatType(String statType);

	public Long getNumberOfTraits();
	
}
