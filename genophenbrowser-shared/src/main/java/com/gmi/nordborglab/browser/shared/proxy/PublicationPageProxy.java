package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/3/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.PublicationPage")
public interface PublicationPageProxy extends ValueProxy{
    List<PublicationProxy> getContent();
    int getNumber();
    long getTotalElements();
    int getTotalPages();
}
