package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.server.search.ExperimentSearchProcessor;
import com.gmi.nordborglab.browser.server.search.PhenotypeSearchProcessor;
import com.gmi.nordborglab.browser.server.search.StudySearchProcessor;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.common.collect.Lists;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.action.get.RestMultiGetAction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/2/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */

@Service("ES")
public class ESAnnotationDataServiceImpl implements AnnotationDataService{


    @Resource
    protected Client client;
    private static String INDEX_PREFIX="annot_%s";

    @Override
    public List<Gene> getGenes(String chr, Long start, Long end, boolean isFeatures) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Isoform getGeneIsoform(String gene) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SNPAnnot> getSNPAnnotations(String chr,int[] positions) {
        List<SNPAnnot> annotations = new ArrayList<SNPAnnot>();

        MultiGetRequestBuilder requestBuilder = client.prepareMultiGet();
        for (int i =0;i<positions.length;i++) {
            requestBuilder.add(new MultiGetRequest.Item(String.format(INDEX_PREFIX,chr),"snps",String.valueOf(positions[i])).fields("annotation","inGene"));
        }
        MultiGetResponse response = requestBuilder.execute().actionGet();
        Iterator<MultiGetItemResponse> iterator = response.iterator();
        MultiGetItemResponse itemResponse =null;
        int failed =0;
        while (iterator.hasNext()) {
            SNPAnnot snpAnnot = new SNPAnnot();
            try {
                itemResponse = iterator.next();
                snpAnnot.setAnnotation(itemResponse.getResponse().getFields().get("annotation").getValue().toString());
                snpAnnot.setInGene((Boolean)itemResponse.getResponse().getFields().get("inGene").getValue());
            }
            catch (Exception e) {
                String test="test";
                failed = failed +1 ;
            }
            annotations.add(snpAnnot);
        }
        return annotations;
    }
}
