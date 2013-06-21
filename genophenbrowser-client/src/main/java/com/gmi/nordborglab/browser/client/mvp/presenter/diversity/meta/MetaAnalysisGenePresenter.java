package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.MetaAnalysisGeneUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisGenePresenter extends
        Presenter<MetaAnalysisGenePresenter.MyView, MetaAnalysisGenePresenter.MyProxy> implements MetaAnalysisGeneUiHandlers {

    public interface MyView extends View, HasUiHandlers<MetaAnalysisGeneUiHandlers> {
        void setGeneViewerRegion(String chr, int start, int end, int totalLength);

        HasData<MetaSNPAnalysisProxy> getDisplay();

        void setGeneViewerSelection(long position);

        void setGene(String gene);

        void setGeneRange(Integer leftInterval, Integer rightInterval);

        void reset();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.metaAnalysisGenes)
    public interface MyProxy extends ProxyPlace<MetaAnalysisGenePresenter> {
    }

    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;
    private GeneProxy gene;
    private ListDataProvider<MetaSNPAnalysisProxy> dataProvider = new ListDataProvider<MetaSNPAnalysisProxy>();
    private final static int DEFAULT_INTERVAL = 10000;
    private int leftInterval = DEFAULT_INTERVAL;
    private int rightInterval = DEFAULT_INTERVAL;

    @Inject
    public MetaAnalysisGenePresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                     final CustomRequestFactory rf, final PlaceManager placeManager) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.placeManager = placeManager;
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }

    @Override
    public void onSearchForGene(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        rf.searchRequest().searchGeneByTerm(request.getQuery()).fire(new Receiver<SearchFacetPageProxy>() {

            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                SuggestOracle.Response searchResponse = new SuggestOracle.Response();
                Collection<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
                if (response != null) {
                    for (SearchItemProxy searchItem : response.getContents()) {
                        suggestions.add(new SearchSuggestOracle.SearchSuggestion(searchItem));
                    }
                }
                searchResponse.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, searchResponse);
            }
        });
    }

    @Override
    public void onSelectGene(SuggestOracle.Suggestion suggestion) {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        placeManager.revealPlace(request.with("id", suggestion.getReplacementString()));
    }


    @Override
    public void onReset() {
        super.onReset();
        PlaceRequest currentRequest = placeManager.getCurrentPlaceRequest();
        String id = currentRequest.getParameter("id", null);
        if (id == null) {
            reset();
        } else if (gene == null || !gene.getName().equalsIgnoreCase(id)) {
            fetchGene(id);
        }
    }

    private void fetchGene(String geneName) {
        fireEvent(new LoadingIndicatorEvent(true));
        rf.annotationDataRequest().getGeneById(geneName).fire(new Receiver<GeneProxy>() {
            @Override
            public void onSuccess(GeneProxy response) {
                if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
                    dataProvider.removeDataDisplay(getView().getDisplay());
                }
                gene = response;
                updateView();
                fetchMetaAnalysisData();
            }
        });
    }

    private void fetchMetaAnalysisData() {
        fireEvent(new LoadingIndicatorEvent(true));
        rf.metaAnalysisRequest().findAllAnalysisForRegion((int) gene.getStart() - leftInterval, (int) gene.getEnd() + rightInterval, gene.getChr()).fire(new Receiver<List<MetaSNPAnalysisProxy>>() {
            @Override
            public void onSuccess(List<MetaSNPAnalysisProxy> metaSNPAnalysisProxies) {
                fireEvent(new LoadingIndicatorEvent(false));
                if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
                    dataProvider.removeDataDisplay(getView().getDisplay());
                }
                dataProvider.setList(metaSNPAnalysisProxies);
                dataProvider.addDataDisplay(getView().getDisplay());
            }
        });
    }

    private void reset() {
        gene = null;
        resetView();
        dataProvider.setList(Lists.<MetaSNPAnalysisProxy>newArrayList());
        /*if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
            dataProvider.removeDataDisplay(getView().getDisplay());
        } */
    }

    private void resetView() {
        getView().reset();
    }

    @Override
    public void onSelectMetaAnalysis(MetaSNPAnalysisProxy metaAnalysis) {
        getView().setGeneViewerSelection(metaAnalysis.getSnpAnnotation().getPosition());
    }

    @Override
    public void onChangeRange(int lowerLimit, int upperLimit) {
        leftInterval = lowerLimit * 1000;
        rightInterval = upperLimit * 1000;
        getView().setGeneViewerRegion(gene.getChr(), (int) gene.getStart() - leftInterval, (int) gene.getEnd() + rightInterval, 30000000);
        fetchMetaAnalysisData();
    }

    private void updateView() {
        if (gene == null)
            return;
        getView().setGeneViewerRegion(gene.getChr(), (int) gene.getStart() - leftInterval, (int) gene.getEnd() + rightInterval, 30000000);
        getView().setGeneRange(Math.round(leftInterval / 1000), Math.round(rightInterval / 1000));
        getView().setGene(gene.getName());

    }
}
