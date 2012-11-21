package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.SearchUiHandlers;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle.SearchSuggestion;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;


public class SearchPresenter extends PresenterWidget<SearchPresenter.MyView> implements SearchUiHandlers{

	public interface MyView extends View,HasUiHandlers<SearchUiHandlers> {

		void setMinCharSize(int minCharSize);
	}
	
	private CATEGORY category;
	private final CustomRequestFactory rf;
	private final PlaceManager placeManager;

	@Inject
	public SearchPresenter(final EventBus eventBus, final MyView view, 
			final CustomRequestFactory rf, final PlaceManager placeManager) {
		super(eventBus, view);
		this.rf = rf;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
	}
	
	public void setCategory(CATEGORY category) {
		this.category = category;
	}
	
	public void setMinCharSize(int minCharSize) {
		getView().setMinCharSize(minCharSize);
	}

	@Override
	public void onRequestSearch(final Request request, final Callback callback) {
		rf.searchRequest().searchByTerm(request.getQuery(), category, null).fire(new Receiver<List<SearchFacetPageProxy>>() {

			@Override
			public void onSuccess(List<SearchFacetPageProxy> response) {
				Response searchResponse = new Response();
				Collection<Suggestion> suggestions = new ArrayList<Suggestion>();
				for (SearchFacetPageProxy facetPage: response) {
					for (SearchItemProxy searchItem:facetPage.getContents()) {
						suggestions.add(new SearchSuggestOracle.SearchSuggestion(searchItem));
					}
				}
				searchResponse.setSuggestions(suggestions);
				callback.onSuggestionsReady(request, searchResponse);
			}
		});
	}

	@Override
	public void onNavigateToSuggestion(SearchSuggestion searchSuggestion) {
		String nameToken = null;
		switch (searchSuggestion.getCategory()) {
			case EXPERIMENT:
				nameToken = NameTokens.experiment;
				break;
			case PHENOTYPE:
				nameToken = NameTokens.phenotype;
				break;
			case STOCK:
				nameToken = NameTokens.stock;
				break;
			case TAXONOMY:
				nameToken = NameTokens.taxonomy;
				break;
			case PASSPORT:
				nameToken = NameTokens.passport;
				break;
			case STUDY:
				nameToken = NameTokens.study;
				break;
			case ONTOLOGY:
				nameToken = null;
				break;
		}
		if (nameToken == null)
			return;
		
		PlaceRequest request = new ParameterizedPlaceRequest(nameToken).with("id", searchSuggestion.getId());
		placeManager.revealPlace(request);
	}
}
