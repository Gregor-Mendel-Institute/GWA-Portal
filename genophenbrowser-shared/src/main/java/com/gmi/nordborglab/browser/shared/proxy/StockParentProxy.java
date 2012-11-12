package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.StockParent", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StockParentProxy extends EntityProxy {
	public Long getId();
	
	public StockProxy getParent();
	public void setParent(StockProxy parent);
	
	public StockProxy getChild();
	public void setChild(StockProxy child);
	
	public String getRole();
	public void setRole(String role);
	
	public Integer getRecurrent();
	public void setRecurrent(Integer recurrent);

}
