package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/19/13
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.Publication", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PublicationProxy extends EntityProxy {

    public Long getId();

    public String getVolume();

    public void setVolume(String volume);

    public String getIssue();

    public void setIssue(String issue);

    public String getDOI();

    public void setDOI(String DOI);

    public String getURL();

    public void setURL(String URL);

    public String getTitle();

    public void setTitle(String title);

    public String getPage();

    public void setPage(String page);

    public String getFirstAuthor();

    public void setFirstAuthor(String firstAuthor);

    public String getJournal();

    public void setJournal(String journal);

    public Date getPubDate();

    public void setPubDate(Date date);

    public Date getCreated();

}
