package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.06.13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */

public interface MetaAnalysisService {
    public List<MetaSNPAnalysis> findAllAnalysisForRegion(int start, int end, String chr);

    public List<ESFacet> findMetaStats(MetaAnalysisTopResultsCriteria criteria);

    public MetaSNPAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, int start, int size);
}
