package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
public class AppUserPage extends PageImpl<AppUser> {

    private List<ESFacet> facets;

    public AppUserPage() {
        super(new ArrayList<AppUser>(), new PageRequest(0, 1), 0);
    }

    public AppUserPage(List<AppUser> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public AppUserPage(List<AppUser> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    //NOTE required because there is a hasContent() getter ->  https://code.google.com/p/google-web-toolkit/issues/detail?id=6587
    public List<AppUser> getContents() {
        return getContent();
    }


    public List<ESFacet> getFacets() {
        return facets;
    }
}

