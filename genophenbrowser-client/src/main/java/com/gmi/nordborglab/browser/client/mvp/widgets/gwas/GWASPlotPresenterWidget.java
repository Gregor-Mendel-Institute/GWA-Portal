package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.github.timeu.gwtlibs.gwasviewer.client.DisplayFeature;
import com.github.timeu.gwtlibs.gwasviewer.client.Track;
import com.gmi.nordborglab.browser.client.dispatch.CustomCallback;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionResult;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.shared.proxy.GenePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/26/13
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASPlotPresenterWidget extends PresenterWidget<GWASPlotPresenterWidget.MyView> implements GWASPlotUiHandlers {

    public interface MyView extends View, HasUiHandlers<GWASPlotUiHandlers> {

        void drawGWASPlots(GWASDataDTO gwasData);

        void setTracks(Track[] tracks);

        void setTrackData(String id, DataTable data, boolean isStacked, String chr);

        void setPlotSettings(Long id, GetGWASDataAction.TYPE type);

        void removeDisplayFeaturesFromGWAS(int chr, Collection<DisplayFeature> features);

        void addDisplayFeaturesToGWAS(int chr, Collection<DisplayFeature> features);

    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public interface Tracks {
        @JsProperty
        Track[] getTracks();
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public interface TracksData {
        @JsProperty
        String getData();
    }

    private Long id;
    private GetGWASDataAction.TYPE type;
    private final DispatchAsync dispatch;
    private final GoogleAnalyticsManager analyticsManager;
    private Track[] tracks;
    private final CustomRequestFactory rf;
    private final Map<String, List<DisplayFeature>> selectedItems = new HashMap<>();
    private final Promise getGWASStats = new PromiseFunction() {
        @Override
        public void f(Deferred deferred) {
            if (tracks == null) {
                Ajax.get(GWT.getHostPageBaseURL() + "/provider/tracks").done(new Function() {
                    @Override
                    public void f() {
                        String response = arguments(0);
                        Tracks t = JsonUtils.safeEval(response).cast();
                        tracks = t.getTracks();
                        deferred.resolve(tracks);
                    }
                });
            } else {
                deferred.resolve(tracks);
            }
        }
    };

    @Inject
    public GWASPlotPresenterWidget(EventBus eventBus, MyView view, final CustomRequestFactory rf, final DispatchAsync dispatch, final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view);
        this.dispatch = dispatch;
        this.rf = rf;
        this.analyticsManager = analyticsManager;
        getView().setUiHandlers(this);
    }


    public void loadPlots(Long id, GetGWASDataAction.TYPE type) {
        this.id = id;
        this.type = type;
        getView().setPlotSettings(id, type);
        getGWASStats.done(new Function() {
            @Override
            public void f() {
                getView().setTracks(tracks);
            }
        });
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        analyticsManager.startTimingEvent("GWAS", "View");
        dispatch.execute(new GetGWASDataAction(id, type), new CustomCallback<GetGWASDataActionResult>(getEventBus()) {

            @Override
            public void onSuccess(GetGWASDataActionResult result) {
                getView().drawGWASPlots(result.getResultData());
                LoadingIndicatorEvent.fire(this, false);
                analyticsManager.endTimingEvent("GWAS", "View", "SUCCESS");
                analyticsManager.sendEvent("GWAS", "Display", "Type:" + type + ",ID:" + id);
            }

            @Override
            public void onFailure(Throwable caught) {
                analyticsManager.endTimingEvent("GWAS", "View", "ERROR");
                analyticsManager.sendError("GWAS", "Type:" + type + ",ID:" + id + ",Error:" + caught.getMessage(), true);
                //FIXME fix backend to not show HTML error page
                //super.onFailure(caught);
            }
        });
    }

    @Override
    public void onSelectSNP(int chromosome, int xVal, int clientX, int clientY) {
        getEventBus().fireEventFromSource(new SelectSNPEvent(chromosome, xVal, clientX, clientY), this);
    }

    @Override
    public void onLoadTrackData(String id, boolean isStacked, String chr) {
        Ajax.get(GWT.getHostPageBaseURL() + "/provider/tracks/" + id + "/" + chr).done(new Function() {
            @Override
            public void f() {
                String response = arguments(0);
                TracksData tracksData = JsonUtils.safeEval(response);
                DataTable data = DataTable.create(tracksData.getData());
                getView().setTrackData(id, data, isStacked, chr);
            }
        });
    }

    @Override
    public void onSearchGenes(String searchString, GWASPlotView.SearchGeneCallback callback) {
        rf.searchRequest().searchByTerm(searchString, SearchItemProxy.CATEGORY.DIVERSITY, null).fire(new Receiver<List<SearchFacetPageProxy>>() {

            @Override
            public void onSuccess(List<SearchFacetPageProxy> response) {
                callback.onDisplayResults(filterResponse(response));
            }
        });
    }

    @Override
    public void onHighlightGene(String value, boolean isSelection) {
        if (isSelection) {
            if (selectedItems.containsKey(value))
                return;
            String[] splitValue = value.split("__");
            if (SearchItemProxy.SUB_CATEGORY.valueOf(splitValue[0]) == SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST) {
                rf.metaAnalysisRequest().getGenesInCandidateGeneList(Long.valueOf(splitValue[1]), ConstEnums.GENE_FILTER.ALL, null, 0, -1).fire(new Receiver<GenePageProxy>() {
                    @Override
                    public void onSuccess(GenePageProxy response) {
                        List<DisplayFeature> features = getFeaturesFromGene(response.getContents());
                        selectedItems.put(value, features);
                        Map<Integer, Collection<DisplayFeature>> featureMap = groupByChr(features);
                        for (Map.Entry<Integer, Collection<DisplayFeature>> entry : featureMap.entrySet()) {
                            getView().addDisplayFeaturesToGWAS(entry.getKey(), entry.getValue());
                        }
                    }
                });
            } else {
                rf.annotationDataRequest().getGeneById(splitValue[1]).fire(new Receiver<GeneProxy>() {
                    @Override
                    public void onSuccess(GeneProxy response) {
                        List<DisplayFeature> features = getFeaturesFromGene(response);
                        selectedItems.put(value, features);
                        getView().addDisplayFeaturesToGWAS(Integer.valueOf(response.getChr()), features);
                    }
                });

            }
        } else {
            List<DisplayFeature> features = selectedItems.get(value);
            Map<Integer, Collection<DisplayFeature>> featureMap = groupByChr(features);
            for (Map.Entry<Integer, Collection<DisplayFeature>> entry : featureMap.entrySet()) {
                getView().removeDisplayFeaturesFromGWAS(entry.getKey(), entry.getValue());
            }

            selectedItems.remove(value);
        }
    }

    private Map<Integer, Collection<DisplayFeature>> groupByChr(Collection<DisplayFeature> features) {
        //TODO remove features
        return Multimaps.index(features, new com.google.common.base.Function<DisplayFeature, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable DisplayFeature input) {
                //TODO make this more robust
                return Integer.valueOf(input.name.substring(2, 3));
            }
        }).asMap();
    }

    private List<DisplayFeature> getFeaturesFromGene(List<GeneProxy> contents) {
        List<DisplayFeature> result = new ArrayList<>();
        for (GeneProxy gene : contents) {
            result.addAll(getFeaturesFromGene(gene));
        }
        return result;
    }


    private List<DisplayFeature> getFeaturesFromGene(GeneProxy response) {
        List<DisplayFeature> result = new ArrayList<>(1);
        result.add(new DisplayFeature(response.getName(), (int) response.getStart(), (int) response.getEnd(), "red"));
        return result;
    }


    private List<SearchFacetPageProxy> filterResponse(List<SearchFacetPageProxy> response) {
        return Lists.newArrayList(Iterables.filter(response, input -> input.getCategory() == SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST || input.getCategory() == SearchItemProxy.SUB_CATEGORY.GENE));
    }
}
