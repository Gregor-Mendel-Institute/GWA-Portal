package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 19.06.13
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
// required until Requestfactory supports generic types
public class MetaSNPAnalysisPage extends PageImpl<MetaSNPAnalysis> {

    public MetaSNPAnalysisPage() {
        super(new ArrayList<MetaSNPAnalysis>(), new PageRequest(0, 1), 0);
    }

    public MetaSNPAnalysisPage(List<MetaSNPAnalysis> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
}
