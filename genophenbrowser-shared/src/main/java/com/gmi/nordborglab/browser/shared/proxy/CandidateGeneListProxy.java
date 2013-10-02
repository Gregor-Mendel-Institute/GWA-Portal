package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 23.09.13
 * Time: 13:16
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface CandidateGeneListProxy extends SecureEntityProxy {

    Long getId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Date getCreated();

    Date getModified();

    Date getPublished();

    int getGeneCount();
}
