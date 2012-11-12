package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StudyProtocolProxy extends EntityProxy {

	public Long getId();
	
	public String getAnalysisMethod();
	public void setAnalysisMethod(String analysisMethod);
}
