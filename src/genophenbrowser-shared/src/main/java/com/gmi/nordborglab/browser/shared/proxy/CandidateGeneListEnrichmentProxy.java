package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface CandidateGeneListEnrichmentProxy extends EntityProxy {

    Long getId();

    String getStatus();

    int getProgress();

    String getTask();

    Double getPvalue();


    Date getCreated();

    Date getModified();

    StudyProxy getStudy();

    void setStudy(StudyProxy study);

    CandidateGeneListProxy getCandidateGeneList();

    void setCandidateGeneList(CandidateGeneListProxy list);
}
