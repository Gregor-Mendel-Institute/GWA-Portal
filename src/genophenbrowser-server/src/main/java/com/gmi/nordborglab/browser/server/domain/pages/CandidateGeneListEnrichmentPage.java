package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListEnrichmentPage extends PageImpl<CandidateGeneListEnrichment> {

    private List<ESFacet> facets;

    public CandidateGeneListEnrichmentPage() {
        super(new ArrayList<CandidateGeneListEnrichment>(), new PageRequest(0, 1), 0);
    }

    public CandidateGeneListEnrichmentPage(List<CandidateGeneListEnrichment> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public CandidateGeneListEnrichmentPage(List<CandidateGeneListEnrichment> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public List<CandidateGeneListEnrichment> getContents() {
        return getContent();
    }


    public List<ESFacet> getFacets() {
        return facets;
    }
}