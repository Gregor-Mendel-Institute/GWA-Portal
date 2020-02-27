package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class TraitUomPage extends PageImpl<TraitUom> {

    private List<ESFacet> facets;

    public TraitUomPage(List<TraitUom> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public List<TraitUom> getContents() {
        return getContent();
    }

    public List<ESFacet> getFacets() {
        return facets;
    }
}
