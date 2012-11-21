package com.gmi.nordborglab.browser.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.server.search.ExperimentSearchProcessor;
import com.gmi.nordborglab.browser.server.search.PassportSearchProcessor;
import com.gmi.nordborglab.browser.server.search.PhenotypeSearchProcessor;
import com.gmi.nordborglab.browser.server.search.StockSearchProcessor;
import com.gmi.nordborglab.browser.server.search.StudySearchProcessor;
import com.gmi.nordborglab.browser.server.search.TaxonomySearchProcessor;
import com.gmi.nordborglab.browser.server.service.SearchService;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

	@Resource
	protected Client client;

	public static String INDEX_NAME = "gdpdm";

	@Override
	public List<SearchFacetPage> searchByTerm(String term, CATEGORY category,
			SUB_CATEGORY subCategory) {
		List<SearchFacetPage> searchResults = new ArrayList<SearchFacetPage>();
		SearchFacetPage facetPage = null;
		MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch();

		if (category == CATEGORY.DIVERSITY) {
			ExperimentSearchProcessor experimentProcessor = new ExperimentSearchProcessor(
					term);
			PhenotypeSearchProcessor phenotypeProcessor = new PhenotypeSearchProcessor(
					term);
			StudySearchProcessor studyProcessor = new StudySearchProcessor(term);

			requestBuilder.add(experimentProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));

			requestBuilder.add(phenotypeProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));

			requestBuilder.add(studyProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));

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

			
		} else if (category == CATEGORY.GERMPLASM) {
			TaxonomySearchProcessor taxonomySearchProcessor = new TaxonomySearchProcessor(term);
			PassportSearchProcessor passportSearchProcessor = new PassportSearchProcessor(term);
			StockSearchProcessor stockSearchProcessor = new StockSearchProcessor(term);
			
			requestBuilder.add(taxonomySearchProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));
			
			requestBuilder.add(passportSearchProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));
			
			requestBuilder.add(stockSearchProcessor.getSearchBuilder(client
					.prepareSearch(INDEX_NAME)));
			
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
}
