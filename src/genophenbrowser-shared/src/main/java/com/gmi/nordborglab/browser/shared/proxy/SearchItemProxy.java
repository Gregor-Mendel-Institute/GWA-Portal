package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName("com.gmi.nordborglab.browser.server.domain.SearchItem")
public interface SearchItemProxy extends ValueProxy {

    public static enum CATEGORY {DIVERSITY, GERMPLASM, GENOTYPE}

    public static enum SUB_CATEGORY {STUDY, PHENOTYPE, ANALYSIS, ONTOLOGY, PASSPORT, STOCK, PUBLICATION, TAXONOMY, CANDIDATE_GENE_LIST, GENE, USER}

    public String getId();

    public String getDisplayText();

    public String getReplacementText();

    public SUB_CATEGORY getSubCategory();

    public CATEGORY getCategory();

}
