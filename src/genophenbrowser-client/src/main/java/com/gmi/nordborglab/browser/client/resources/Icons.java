package com.gmi.nordborglab.browser.client.resources;

import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.common.collect.ImmutableMap;

/**
 * Created by uemit.seren on 10.01.14.
 */
public class Icons {

    public static final String STUDY = "e_icon-thermometer";
    public static final String PHENOTYPE = "e_icon-feather";
    public static final String ANALYSIS = "e_icon-monitor";
    public static final String ONTOLOGY = "e_icon-flow-tree";
    public static final String META_ANALYSIS = "e_icon-tools";
    public static final String PUBLICATION = "e_icon-book-open";
    public static final String USER = "e_icon-user";
    public static final String TOOLS = "e_icon-tools";
    public static final String STOCK = "e_icon-tag";
    public static final String PASSPORT = "e_icon-leaf";
    public static final String TAXONOMY = "e_icon-archive";
    public static final String GENE = "e_icon-tape";

    public static final ImmutableMap<SearchItemProxy.SUB_CATEGORY, String> subCategory2Icon = ImmutableMap.<SearchItemProxy.SUB_CATEGORY, String>builder()
            .put(SearchItemProxy.SUB_CATEGORY.STUDY, STUDY)
            .put(SearchItemProxy.SUB_CATEGORY.PHENOTYPE, PHENOTYPE)
            .put(SearchItemProxy.SUB_CATEGORY.ANALYSIS, ANALYSIS)
            .put(SearchItemProxy.SUB_CATEGORY.PUBLICATION, PUBLICATION)
            .put(SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST, META_ANALYSIS)
            .put(SearchItemProxy.SUB_CATEGORY.ONTOLOGY, ONTOLOGY)
            .put(SearchItemProxy.SUB_CATEGORY.USER, USER)
            .put(SearchItemProxy.SUB_CATEGORY.TAXONOMY, TAXONOMY)
            .put(SearchItemProxy.SUB_CATEGORY.GENE, GENE)
            .put(SearchItemProxy.SUB_CATEGORY.PASSPORT, PASSPORT)
            .put(SearchItemProxy.SUB_CATEGORY.STOCK, STOCK)
            .build();
}
