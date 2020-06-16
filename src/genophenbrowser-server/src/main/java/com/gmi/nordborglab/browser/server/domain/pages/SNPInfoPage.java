package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uemit.seren on 3/4/15.
 */
public class SNPInfoPage extends PageImpl<SNPInfo> {

    protected List<ESFacet> facets;

    public SNPInfoPage() {
        super(new ArrayList<SNPInfo>(), new PageRequest(0, 1), 0);
    }

    public SNPInfoPage(List<SNPInfo> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public List<SNPInfo> getContents() {
        return getContent();
    }

    public List<ESFacet> getFacets() {
        return facets;
    }
}
