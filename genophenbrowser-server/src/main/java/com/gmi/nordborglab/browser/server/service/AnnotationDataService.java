package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.google.visualization.datasource.datatable.DataTable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */


public interface AnnotationDataService {

    public List<Gene> getGenes(String chr, Long start, Long end, boolean isFeatures);

    public Isoform getGeneIsoform(String gene);

    List<SNPAnnot> getSNPAnnotations(String chr, int[] positions);

    Gene getGeneById(String gene);

    DataTable getGenomeStatData(String stats, String chr);

    SNPAlleleInfo getSNPAlleleInfo(Long alleleAssayId, Integer chromosome, Integer position, List<Long> passportIds);

}
