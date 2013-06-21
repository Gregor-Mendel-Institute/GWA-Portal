package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisTopResultsCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.06.13
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
@ServiceName(value = "com.gmi.nordborglab.browser.server.service.MetaAnalysisService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface MetaAnalysisRequest extends RequestContext {
    Request<List<MetaSNPAnalysisProxy>> findAllAnalysisForRegion(int start, int end, String chr);

    Request<List<FacetProxy>> findMetaStats(MetaAnalysisTopResultsCriteriaProxy criteria);

    Request<MetaSNPAnalysisPageProxy> findTopAnalysis(MetaAnalysisTopResultsCriteriaProxy criteria, int start, int size);
}
