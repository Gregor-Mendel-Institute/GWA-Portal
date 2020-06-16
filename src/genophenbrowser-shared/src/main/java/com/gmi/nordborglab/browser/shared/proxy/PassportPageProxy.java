package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.PassportPage")
public interface PassportPageProxy extends ValueProxy {
    List<PassportProxy> getContents();

    int getNumber();

    long getTotalElements();

    int getTotalPages();
}
