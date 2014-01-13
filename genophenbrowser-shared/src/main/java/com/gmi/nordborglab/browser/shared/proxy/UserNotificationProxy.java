package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/18/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.UserNotification", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface UserNotificationProxy extends EntityProxy {

    public Long getId();

    public String getType();

    public String getText();

    public Date getCreateDate();

    boolean isRead();

    void setRead(boolean isRead);
}
