package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uemit.seren on 1/28/15.
 */
public class GWASResultPage extends PageImpl<GWASResult> {

    protected List<ESFacet> facets;

    public GWASResultPage() {
        super(new ArrayList<GWASResult>(), new PageRequest(0, 1), 0);
    }

    public GWASResultPage(List<GWASResult> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public List<GWASResult> getContents() {
        return getContent();
    }

    public List<ESFacet> getFacets() {
        return facets;
    }
}
