package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 06.12.13
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public interface EnrichmentProvider {

    void setEntityId(Long studyId);

    void fetchData(ConstEnums.ENRICHMENT_FILTER filter, String searchString, int start, int size, Receiver<CandidateGeneListEnrichmentPageProxy> receiver);

    void createEnrichments(Set<CandidateGeneListEnrichmentProxy> records, boolean isAllChecked, Receiver<Void> receiver);

    void findEnrichmentStats(String searchString, Receiver<List<FacetProxy>> receiver);

    ConstEnums.ENRICHMENT_TYPE getViewType();

    Long getEntityId();
}
