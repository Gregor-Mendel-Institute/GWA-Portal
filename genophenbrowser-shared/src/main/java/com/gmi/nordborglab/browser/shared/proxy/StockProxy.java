package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.Stock", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StockProxy extends EntityProxy {

	public Long getId();
	
	public GenerationProxy getGeneration();
	public void setGeneration(GenerationProxy generation);
	
	public PassportProxy getPassport();
	public void setPassport(PassportProxy passport);
	
    public Set<StockParentProxy> getParents();
	
    public Set<StockParentProxy> getChilds();
    
	public String getSeedLot();
	public void setSeedLot(String seedLot);
	
	public String getStockSource();
	public void setStockSource(String stockSource);
	
	
	public String getComments();
	public void setComments(String comments);

	public String getPedigreeData();
	
}
