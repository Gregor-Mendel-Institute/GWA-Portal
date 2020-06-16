package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 6:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FetchGeneResult {

    private final String status = "OK";
    private List<Gene> genes;

    public FetchGeneResult(List<Gene> genes) {
        this.genes = genes;
    }

    public String getStatus() {
        return status;
    }

    public List<Gene> getGenes() {
        return genes;
    }
}
