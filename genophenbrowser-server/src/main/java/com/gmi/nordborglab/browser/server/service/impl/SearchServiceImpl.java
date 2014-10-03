package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.server.search.CandidategenelistSearchProcessor;
import com.gmi.nordborglab.browser.server.search.ExperimentSearchProcessor;
import com.gmi.nordborglab.browser.server.search.GeneSearchProcessor;
import com.gmi.nordborglab.browser.server.search.OntologySearchProcessor;
import com.gmi.nordborglab.browser.server.search.PassportSearchProcessor;
import com.gmi.nordborglab.browser.server.search.PhenotypeSearchProcessor;
import com.gmi.nordborglab.browser.server.search.PublicationSearchProcessor;
import com.gmi.nordborglab.browser.server.search.SearchProcessor;
import com.gmi.nordborglab.browser.server.search.StockSearchProcessor;
import com.gmi.nordborglab.browser.server.search.StudySearchProcessor;
import com.gmi.nordborglab.browser.server.search.TaxonomySearchProcessor;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.SearchService;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    @Resource
    protected Client client;

    @Resource
    protected EsAclManager esAclManager;


    //public static String INDEX_NAME;

    public static String ONTOLOGY_INDEX_NAME = "ontologies";
    public static String[] GENE_INDEX_NAME = {"annot_chr1", "annot_chr2", "annot_chr3", "annot_chr4", "annot_chr5"};

    @Override
    public List<SearchFacetPage> searchByTerm(String term, CATEGORY category,
                                              SUB_CATEGORY subCategory) {
        List<SearchFacetPage> searchResults = new ArrayList<SearchFacetPage>();
        SearchFacetPage facetPage = null;
        MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch();

        if (category == CATEGORY.DIVERSITY) {
            FilterBuilder filter = esAclManager.getAclFilter(Lists.newArrayList("read"));
            ExperimentSearchProcessor experimentProcessor = new ExperimentSearchProcessor(
                    term);
            PhenotypeSearchProcessor phenotypeProcessor = new PhenotypeSearchProcessor(
                    term);
            StudySearchProcessor studyProcessor = new StudySearchProcessor(term);

            OntologySearchProcessor ontologySearchProcessor = new OntologySearchProcessor(term);

            PublicationSearchProcessor publicationSearchProcessor = new PublicationSearchProcessor(term);

            CandidategenelistSearchProcessor candidategenelistSearchProcessor = new CandidategenelistSearchProcessor(term);

            GeneSearchProcessor geneSearchprocessor = new GeneSearchProcessor(term);

            requestBuilder.add(experimentProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())).setPostFilter(filter));

            requestBuilder.add(phenotypeProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())).setPostFilter(filter));

            requestBuilder.add(studyProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())).setPostFilter(filter));

            requestBuilder.add(ontologySearchProcessor.getSearchBuilder(client
                    .prepareSearch(ONTOLOGY_INDEX_NAME)));

            requestBuilder.add(publicationSearchProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())));

            requestBuilder.add(candidategenelistSearchProcessor.getSearchBuilder(client.prepareSearch(esAclManager.getIndex())));

            requestBuilder.add(geneSearchprocessor.getSearchBuilder(client.prepareSearch(GENE_INDEX_NAME)));

            MultiSearchResponse response = requestBuilder.execute().actionGet();

            // Get results from experiment
            facetPage = experimentProcessor.extractSearchFacetPage(response
                    .getResponses()[0].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from phenotype
            facetPage = phenotypeProcessor.extractSearchFacetPage(response
                    .getResponses()[1].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from study
            facetPage = studyProcessor.extractSearchFacetPage(response
                    .getResponses()[2].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from ontologies
            facetPage = ontologySearchProcessor.extractSearchFacetPage(response.getResponses()[3].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from publications
            facetPage = publicationSearchProcessor.extractSearchFacetPage(response.getResponses()[4].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from candidate gene lists
            facetPage = candidategenelistSearchProcessor.extractSearchFacetPage(response.getResponses()[5].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get resutls from gene
            facetPage = geneSearchprocessor.extractSearchFacetPage(response.getResponses()[6].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

        } else if (category == CATEGORY.GERMPLASM) {
            TaxonomySearchProcessor taxonomySearchProcessor = new TaxonomySearchProcessor(term);
            PassportSearchProcessor passportSearchProcessor = new PassportSearchProcessor(term);
            StockSearchProcessor stockSearchProcessor = new StockSearchProcessor(term);

            requestBuilder.add(taxonomySearchProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())));

            requestBuilder.add(passportSearchProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())));

            requestBuilder.add(stockSearchProcessor.getSearchBuilder(client
                    .prepareSearch(esAclManager.getIndex())));


            MultiSearchResponse response = requestBuilder.execute().actionGet();

            // Get results from experiment
            facetPage = taxonomySearchProcessor.extractSearchFacetPage(response
                    .getResponses()[0].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from phenotype
            facetPage = passportSearchProcessor.extractSearchFacetPage(response
                    .getResponses()[1].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);

            // Get results from study
            facetPage = stockSearchProcessor.extractSearchFacetPage(response
                    .getResponses()[2].getResponse());
            if (facetPage != null)
                searchResults.add(facetPage);
        }

        return searchResults;
    }

    @Override
    public SearchFacetPage searchGeneByTerm(String term) {

        SearchFacetPage facetPage = null;
        GeneSearchProcessor processor = new GeneSearchProcessor(term);
        SearchRequestBuilder builder = client.prepareSearch("annot_chr1", "annot_chr2", "annot_chr3", "annot_chr4", "annot_chr5");
        builder = processor.getSearchBuilder(builder);
        SearchResponse response = builder.execute().actionGet();
        facetPage = processor.extractSearchFacetPage(response);
        return facetPage;
    }

    @Override
    public SearchFacetPage searchByFilter(String query, ConstEnums.FILTERS filter) {
        SearchFacetPage searchFacetPage = null;
        FilterBuilder aclFilter = esAclManager.getAclFilter(Lists.newArrayList("read"));
        SearchProcessor searchProcessor = getSearchProcessorFromFilter(query, filter);
        if (searchProcessor != null) {
            SearchRequestBuilder request = searchProcessor.getSearchBuilder(client.prepareSearch(esAclManager.getIndex()).setPostFilter(aclFilter));
            SearchResponse response = request.execute().actionGet();
            searchFacetPage = searchProcessor.extractSearchFacetPage(response);
        }
        return searchFacetPage;
    }

    private SearchProcessor getSearchProcessorFromFilter(String query, ConstEnums.FILTERS filter) {
        switch (filter) {
            case STUDY:
                return new ExperimentSearchProcessor(query);
            case PHENOTYPE:
                return new PhenotypeSearchProcessor(query);
            case ANALYSIS:
                return new StudySearchProcessor(query);
            case CANDIDATE_GENE_LIST:
                return new CandidategenelistSearchProcessor(query);
        }
        return null;
    }
}
