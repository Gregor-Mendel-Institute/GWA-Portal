package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.security.CustomAcl")
public interface CustomAclProxy extends ValueProxy {

	public List<AccessControlEntryProxy> getEntries();
	public boolean getIsEntriesInheriting();
}
