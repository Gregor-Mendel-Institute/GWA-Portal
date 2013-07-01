package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.NewsItem", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface NewsItemProxy extends EntityProxy {
    Long getId();

    String getTitle();

    String getContent();

    Date getCreateDate();

    AppUserProxy getAuthor();

    String getType();

    boolean isRead();
}
