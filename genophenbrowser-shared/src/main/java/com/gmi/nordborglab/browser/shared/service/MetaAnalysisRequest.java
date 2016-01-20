package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.FilterItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gmi.nordborglab.browser.shared.proxy.GenePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisTopResultsCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.ExtraTypes;
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
@ExtraTypes({GWASResultProxy.class, ExperimentProxy.class, PhenotypeProxy.class, StudyProxy.class, CandidateGeneListProxy.class})
public interface MetaAnalysisRequest extends RequestContext {
    Request<MetaAnalysisPageProxy> findAllAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItemProxy> filterItems, boolean isGrouped);

    Request<List<FacetProxy>> findMetaStats(MetaAnalysisTopResultsCriteriaProxy criteria, List<FilterItemProxy> filterItems);

    Request<MetaAnalysisPageProxy> findTopAnalysis(MetaAnalysisTopResultsCriteriaProxy criteria, List<FilterItemProxy> filterItems, int start, int size);

    Request<CandidateGeneListPageProxy> findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<CandidateGeneListProxy> saveCandidateGeneList(CandidateGeneListProxy candidateGeneList);

    Request<Void> deleteCandidateGeneList(CandidateGeneListProxy candidateGeneList);

    Request<CandidateGeneListProxy> findOneCandidateGeneList(Long id);

    Request<GenePageProxy> getGenesInCandidateGeneList(Long id, ConstEnums.GENE_FILTER filter, String searchString, int start, int size);

    Request<GeneProxy> addGeneToCandidateGeneList(CandidateGeneListProxy candidateGeneList, String geneId);

    Request<Void> removeGeneFromCandidateGeneList(CandidateGeneListProxy candidateGeneList, String geneId);

    Request<CandidateGeneListEnrichmentPageProxy> findCandidateGeneListEnrichments(SecureEntityProxy entity, ConstEnums.ENRICHMENT_FILTER currentFilter, String searchString, int start, int length);

    Request<Void> createCandidateGeneListEnrichments(SecureEntityProxy entity, boolean isAllChecked, List<CandidateGeneListEnrichmentProxy> candidateGeneListEnrichments);

    Request<List<FacetProxy>> findEnrichmentStats(SecureEntityProxy entity, String searchString);
}
