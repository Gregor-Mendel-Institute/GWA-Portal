package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created by uemit.seren on 1/15/16.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysis")
public interface MetaAnalysisProxy extends ValueProxy {

    String getAnalysis();

    Long getAnalysisId();

    String getPhenotype();

    String getStudy();

    String getMethod();

    String getGenotype();

    Long getPhenotypeId();

    Long getStudyId();

    Long getTotalAssocCount();

    List<AssociationProxy> getAssociations();
}
