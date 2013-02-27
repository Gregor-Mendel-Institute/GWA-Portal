package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.cdv.Transformation", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface TransformationProxy extends EntityProxy {

    public Long getId();

    public String getName();

    public String getDescription();
}
