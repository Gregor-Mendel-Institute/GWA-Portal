package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 06.12.13
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class EnrichmentProviderImpl implements EnrichmentProvider {

    private final CustomRequestFactory rf;

    private final ConstEnums.ENRICHMENT_TYPE type;
    private Long entityId;
    private static String[] candidateGeneListPath = {"contents.study.phenotype.experiment", "contents.study.transformation", "contents.study.alleleAssay"};
    private static String[] experimentPath = {"contents.candidateGeneList", "contents.study.phenotype", "contents.study.transformation", "contents.study.alleleAssay"};
    private static String[] phenotypePath = {"contents.candidateGeneList", "contents.study.transformation", "contents.study.alleleAssay"};
    private static String[] studyPath = {"contents.candidateGeneList", "contents.study.transformation", "contents.study.alleleAssay"};

    @Inject
    public EnrichmentProviderImpl(CustomRequestFactory rf, @Assisted ConstEnums.ENRICHMENT_TYPE type) {
        this.rf = rf;
        this.type = type;
    }

    @Override
    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    private String[] getPath() {
        switch (type) {
            case CANDIDATE_GENE_LIST:
                return candidateGeneListPath;
            case ANALYSIS:
                return studyPath;
            case PHENOTYPE:
                return phenotypePath;
            case STUDY:
                return experimentPath;
        }
        return null;
    }

    @Override
    public void fetchData(ConstEnums.ENRICHMENT_FILTER filter, String searchString, int start, int size, Receiver<CandidateGeneListEnrichmentPageProxy> receiver) {
        if (entityId == null)
            return;
        rf.metaAnalysisRequest().findCandidateGeneListEnrichments(entityId, type, filter, searchString, start, size).with(getPath()).fire(receiver);
    }

    @Override
    public void createEnrichments(Set<CandidateGeneListEnrichmentProxy> records, boolean isAllChecked, Receiver<Void> receiver) {
        MetaAnalysisRequest ctx = rf.metaAnalysisRequest();
        List<CandidateGeneListEnrichmentProxy> candidateGeneListEnrichments = Lists.newArrayList();
        if (!isAllChecked && records != null) {
            for (CandidateGeneListEnrichmentProxy enrichment : records) {
                CandidateGeneListEnrichmentProxy newEnrichment = ctx.create(CandidateGeneListEnrichmentProxy.class);
                newEnrichment.setStudy(enrichment.getStudy());
                newEnrichment.setCandidateGeneList(enrichment.getCandidateGeneList());
                candidateGeneListEnrichments.add(newEnrichment);
            }
        }
        ctx.createCandidateGeneListEnrichments(entityId, type, isAllChecked, candidateGeneListEnrichments).fire(receiver);
    }

    @Override
    public void findEnrichmentStats(String searchString, Receiver<List<FacetProxy>> receiver) {
        if (entityId == null)
            return;
        rf.metaAnalysisRequest().findEnrichmentStats(entityId, type, searchString).fire(receiver);
    }

    @Override
    public ConstEnums.ENRICHMENT_TYPE getViewType() {
        return type;
    }


}
