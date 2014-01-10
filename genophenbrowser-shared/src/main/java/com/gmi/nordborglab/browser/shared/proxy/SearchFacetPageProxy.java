package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage")
public interface SearchFacetPageProxy extends ValueProxy {

    public List<SearchItemProxy> getContents();

    public SUB_CATEGORY getCategory();

    public long getTotal();
}
