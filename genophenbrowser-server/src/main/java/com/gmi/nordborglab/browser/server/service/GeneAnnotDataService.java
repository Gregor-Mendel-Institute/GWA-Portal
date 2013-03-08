package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */


public interface GeneAnnotDataService {

    public List<Gene> getGenes(String chr,Long start, Long end, boolean isFeatures);

    public Isoform getGeneIsoform(String gene);
}
