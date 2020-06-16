package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.Generation", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface GenerationProxy extends EntityProxy {

	public Long getId();
	
	public String getIcisId();
	public void setIcisId(String icisId);
	
	public String getComments();
	public void setComments(String comments);
	
	
	public Integer getSelfingNumber();
	public void setSelfingNumber(Integer selfingNumber);
	
	public Integer getSibbingNumber();
	public void setSibbingNumber(Integer sibbingNumber);
}
