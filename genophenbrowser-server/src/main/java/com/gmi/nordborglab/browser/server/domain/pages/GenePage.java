package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.09.13
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public class GenePage extends PageImpl<Gene> {

    private List<ESFacet> facets;
    private List<ESFacet> statsFacets;

    public GenePage() {
        super(new ArrayList<Gene>(), new PageRequest(0, 1), 0);
    }

    public GenePage(List<Gene> content, Pageable pageable, long total, List<ESFacet> facets, List<ESFacet> statsFacets) {
        super(content, pageable, total);
        this.facets = facets;
        this.statsFacets = statsFacets;
    }

    public GenePage(List<Gene> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }


    public List<ESFacet> getFacets() {
        return facets;
    }

    public List<ESFacet> getStatsFacets() {
        return statsFacets;
    }
}
