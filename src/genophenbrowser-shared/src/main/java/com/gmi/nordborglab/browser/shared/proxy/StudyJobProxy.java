package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/8/13
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.util.StudyJob", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StudyJobProxy extends EntityProxy {

    public Long getId();
    public String getStatus();
    public Integer getProgress();
    public String getTask();
    public Date getCreateDate();
    public Date getModificationDate();

    void setStatus(String status);

    void setProgress(Integer progress);

    void setCreateDate(Date date);

    void setModificationDate(Date date);

    void setTask(String task);
}
