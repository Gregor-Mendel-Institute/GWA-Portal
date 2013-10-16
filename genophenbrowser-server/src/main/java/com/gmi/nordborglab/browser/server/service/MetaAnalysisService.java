package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage;
import com.gmi.nordborglab.browser.server.domain.pages.GenePage;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PreAuthorize;

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

    public List<ESFacet> findMetaStats(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems);

    public MetaSNPAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems, int start, int size);

    CandidateGeneListPage findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size);

    @PreAuthorize("hasRole('ROLE_USER') and (#candidateGeneList.id == null or hasPermission(#candidateGeneList,'EDIT') or hasPermission(#candidateGeneList,'ADMINISTRATION'))")
    CandidateGeneList saveCandidateGeneList(CandidateGeneList candidateGeneList);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#candidateGeneList,'EDIT') or hasPermission(#candidateGeneList,'ADMINISTRATION'))")
    void deleteCandidateGeneList(CandidateGeneList candidateGeneList);

    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList','READ')")
    CandidateGeneList findOneCandidateGeneList(Long id);

    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList','READ')")
    GenePage getGenesInCandidateGeneList(Long id, ConstEnums.GENE_FILTER filter, String searchString, int page, int size);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#candidateGeneList,'EDIT') or hasPermission(#candidateGeneList,'ADMINISTRATION'))")
    Gene addGeneToCandidateGeneList(CandidateGeneList candidateGeneList, String geneId);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#candidateGeneList,'EDIT') or hasPermission(#candidateGeneList,'ADMINISTRATION'))")
    void removeGeneFromCandidateGeneList(CandidateGeneList candidateGeneList, String geneId);
}
