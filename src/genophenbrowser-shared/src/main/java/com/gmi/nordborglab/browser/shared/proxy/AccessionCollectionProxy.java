package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.AccessionCollection", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AccessionCollectionProxy extends EntityProxy {

	public Long getId();
	
	public LocalityProxy getLocality();
	public void setLocality(LocalityProxy locality);
	
	public String getCollector();
	public void setCollector(String collector);
	
	public String getCollNumb();
	public void setCollNumb(String collNumb);
	
	public String getCollSrc();
	public void setCollSrc(String collsrc);
	
	public String getCollCode();
	public void setCollCode(String collcode);
	
	public String getCollDate();
	public void setCollDate(String collDate);
}
