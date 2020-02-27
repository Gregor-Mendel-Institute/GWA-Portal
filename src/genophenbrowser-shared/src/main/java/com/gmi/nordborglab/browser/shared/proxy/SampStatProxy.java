package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface SampStatProxy extends EntityProxy {
	public Long getId();
	public String getSampstat();
	public String getGermplasmType();
}
