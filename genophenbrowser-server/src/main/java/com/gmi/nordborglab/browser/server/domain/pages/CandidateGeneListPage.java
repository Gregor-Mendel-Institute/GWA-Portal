package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 23.09.13
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListPage extends PageImpl<CandidateGeneList> {

    private List<ESFacet> facets;

    public CandidateGeneListPage() {
        super(new ArrayList<CandidateGeneList>(), new PageRequest(0, 1), 0);
    }

    public CandidateGeneListPage(List<CandidateGeneList> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public CandidateGeneListPage(List<CandidateGeneList> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public List<CandidateGeneList> getContents() {
        return getContent();
    }


    public List<ESFacet> getFacets() {
        return facets;
    }
}