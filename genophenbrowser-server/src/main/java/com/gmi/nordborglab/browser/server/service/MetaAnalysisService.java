package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListEnrichmentPage;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage;
import com.gmi.nordborglab.browser.server.domain.pages.GenePage;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
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
    public MetaSNPAnalysisPage findAllAnalysisForRegion(int startPos, int endPost, String chr, int start, int size, List<FilterItem> filterItems);

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

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList','EDIT') or hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList','ADMINISTRATION'))")
    List<Gene> addGenesToCandidateGeneList(Long id, List<String> geneIds);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#candidateGeneList,'EDIT') or hasPermission(#candidateGeneList,'ADMINISTRATION'))")
    void removeGeneFromCandidateGeneList(CandidateGeneList candidateGeneList, String geneId);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList','READ'))")
    List<Gene> getGenesInCandidateGeneListEnrichment(Long id);

    @PreAuthorize("hasPermission(#entity,'READ')")
    CandidateGeneListEnrichmentPage findCandidateGeneListEnrichments(SecureEntity entity, ConstEnums.ENRICHMENT_FILTER currentFilter, String searchString, int start, int length);


    @PreAuthorize("hasPermission(#entity,'READ')")
    List<ESFacet> findEnrichmentStats(SecureEntity entity, String searchString);

    @PreAuthorize("hasPermission(#entity,'READ')")
    void createCandidateGeneListEnrichments(SecureEntity entity, boolean isAllChecked, List<CandidateGeneListEnrichment> candidateGeneListEnrichments);

    void indexCandidateGeneListEnrichment(CandidateGeneListEnrichment enrichment);
}
