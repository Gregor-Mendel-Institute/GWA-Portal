package com.gmi.nordborglab.browser.server.domain.pages;

import java.util.List;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

public class TraitUomPage extends PageImpl<TraitUom> {

    private List<ESFacet> facets;

    public TraitUomPage(List<TraitUom> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public List<ESFacet> getFacets() {
        return facets;
    }
}
