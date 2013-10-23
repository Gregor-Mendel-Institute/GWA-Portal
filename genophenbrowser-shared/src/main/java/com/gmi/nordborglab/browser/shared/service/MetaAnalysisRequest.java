package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
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
    Request<MetaSNPAnalysisPageProxy> findAllAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItemProxy> filterItems);

    Request<List<FacetProxy>> findMetaStats(MetaAnalysisTopResultsCriteriaProxy criteria, List<FilterItemProxy> filterItems);

    Request<MetaSNPAnalysisPageProxy> findTopAnalysis(MetaAnalysisTopResultsCriteriaProxy criteria, List<FilterItemProxy> filterItems, int start, int size);

    Request<CandidateGeneListPageProxy> findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<CandidateGeneListProxy> saveCandidateGeneList(CandidateGeneListProxy candidateGeneList);

    Request<Void> deleteCandidateGeneList(CandidateGeneListProxy candidateGeneList);

    Request<CandidateGeneListProxy> findOneCandidateGeneList(Long id);

    Request<GenePageProxy> getGenesInCandidateGeneList(Long id, ConstEnums.GENE_FILTER filter, String searchString, int start, int size);

    Request<GeneProxy> addGeneToCandidateGeneList(CandidateGeneListProxy candidateGeneList, String geneId);

    Request<Void> removeGeneFromCandidateGeneList(CandidateGeneListProxy candidateGeneList, String geneId);
}
