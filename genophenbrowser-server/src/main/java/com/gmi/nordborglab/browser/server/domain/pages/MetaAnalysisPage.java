package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysis;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uemit.seren on 1/15/16.
 */
public class MetaAnalysisPage extends PageImpl<MetaAnalysis> {

    public MetaAnalysisPage() {
        super(new ArrayList<MetaAnalysis>(), new PageRequest(0, 1), 0);
    }

    private int maxAssocCount = 0;

    public MetaAnalysisPage(List<MetaAnalysis> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public MetaAnalysisPage(List<MetaAnalysis> content, Pageable pageable, long total, int maxAssocCount) {
        super(content, pageable, total);
        this.maxAssocCount = maxAssocCount;
    }

    public List<MetaAnalysis> getContents() {
        return getContent();
    }

    public int getMaxAssocCount() {
        return maxAssocCount;
    }
}
