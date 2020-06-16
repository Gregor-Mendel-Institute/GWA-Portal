package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.BreadcrumbItem")
public interface BreadcrumbItemProxy extends ValueProxy {

	String getText();

	String getType();

	Long getId();

}
