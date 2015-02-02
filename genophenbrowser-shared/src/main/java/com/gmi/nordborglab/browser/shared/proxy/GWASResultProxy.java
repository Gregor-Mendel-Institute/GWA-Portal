package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 8:58 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.GWASResult", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface GWASResultProxy extends SecureEntityProxy {

    public Long getId();

    public String getName();

    public void setName(String name);

    public String getType();

    public void setType(String type);

    public String getComments();

    public void setComments(String comments);

    public int getNumberOfSNPs();

    public float getMaxScore();
}
