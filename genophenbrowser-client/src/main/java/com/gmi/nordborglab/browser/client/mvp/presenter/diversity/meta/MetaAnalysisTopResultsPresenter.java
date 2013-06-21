package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.mvp.handlers.MetaAnalysisTopResultsUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisTopResultsPresenter extends Presenter<MetaAnalysisTopResultsPresenter.MyView, MetaAnalysisTopResultsPresenter.MyProxy> implements MetaAnalysisTopResultsUiHandlers {

    public interface MyView extends View, HasUiHandlers<MetaAnalysisTopResultsUiHandlers> {

        void setStatsData(DataTable dataTable, STATS stat);

        void scheduleLayout();

        void resetSelection(List<STATS> statses);

        HasData<MetaSNPAnalysisProxy> getDisplay();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.metaAnalysisTopResults)
    public interface MyProxy extends ProxyPlace<MetaAnalysisTopResultsPresenter> {
    }

    private final CustomRequestFactory rf;

    private final PlaceManager placeManager;
    private MetaAnalysisTopResultsCriteriaProxy criteria;
    private Map<STATS, DataTable> stats2DataTable = Maps.newHashMap();
    private List<FacetProxy> stats;
    private MetaAnalysisRequest ctx;
    private List<STATS> currentFilters = Lists.newArrayList();

    private AsyncDataProvider<MetaSNPAnalysisProxy> dataProvider = new AsyncDataProvider<MetaSNPAnalysisProxy>() {
        @Override
        protected void onRangeChanged(HasData<MetaSNPAnalysisProxy> display) {
            final Range range = display.getVisibleRange();
            getContext().findTopAnalysis(criteria, range.getStart(), range.getLength()).fire(new Receiver<MetaSNPAnalysisPageProxy>() {
                @Override
                public void onSuccess(MetaSNPAnalysisPageProxy response) {
                    updateRowCount((int) response.getTotalElements(), true);
                    updateRowData(range.getStart(), response.getContent());
                    ctx = null;
                }

                @Override
                public void onFailure(ServerFailure error) {
                    ctx = null;
                    super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
                }
            });
        }
    };

    public enum STATS {CHR, INGENE, OVERFDR, ANNOTATION;}

    @Inject
    public MetaAnalysisTopResultsPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                           final CustomRequestFactory rf, final PlaceManager placeManager) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.placeManager = placeManager;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }

    @Override
    public void onReset() {
        super.onReset();
        fetchData();
    }

    private void fetchData() {
        getContext().findMetaStats(criteria).fire(new Receiver<List<FacetProxy>>() {
            @Override
            public void onSuccess(List<FacetProxy> response) {
                stats = response;
                displayStats();
                ctx = null;
                if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
                    Range range = getView().getDisplay().getVisibleRange();
                    getView().getDisplay().setVisibleRangeAndClearData(range, true);
                } else {
                    dataProvider.addDataDisplay(getView().getDisplay());
                }
            }
        });
    }

    private void displayStats() {
        for (FacetProxy facet : stats) {
            STATS stat = STATS.valueOf(facet.getName().toUpperCase());
            if (!currentFilters.contains(stat)) {
                DataTable dataTable = getDataTableFromFacet(facet);
                stats2DataTable.put(stat, dataTable);
                getView().setStatsData(dataTable, stat);
            }
        }
        getView().scheduleLayout();
    }

    private DataTable getDataTableFromFacet(FacetProxy facet) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, facet.getName());
        dataTable.addColumn(ColumnType.NUMBER, "count");
        int rowCount = dataTable.addRows(facet.getTerms().size());
        for (int i = 0; i <= rowCount; i++) {
            FacetTermProxy term = facet.getTerms().get(i);
            dataTable.setValue(i, 0, term.getTerm());
            dataTable.setValue(i, 1, term.getValue());
        }
        return dataTable;
    }

    @Override
    protected void onHide() {
        // Required because of this: https://code.google.com/p/gwt-charts/issues/detail?id=41
        getView().resetSelection(null);
        currentFilters.clear();
        criteria = null;
        stats2DataTable.clear();
        stats.clear();
        super.onHide();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onChangeSelections(STATS stat, Integer row) {
        if (criteria == null) {
            criteria = getContext().create(MetaAnalysisTopResultsCriteriaProxy.class);
        } else {
            criteria = getContext().edit(criteria);
        }
        String category = null;
        if (row != null) {
            DataTable dataTable = stats2DataTable.get(stat);
            category = dataTable.getValueString(row, 0);
            if (!currentFilters.contains(stat)) {
                currentFilters.add(stat);
            } else {
                resetOutdatedFilters(stat);
            }
        } else {
            // if not the last filter was deselected reset all filters that were set afterwards
            resetOutdatedFilters(stat);
            currentFilters.remove(stat);
        }
        setCriteriaValue(stat, category);
        fetchData();
    }

    private void resetOutdatedFilters(STATS stat) {
        List<STATS> filtersToReset = Lists.newArrayList();
        if (currentFilters.indexOf(stat) < currentFilters.size() - 1) {
            int index = currentFilters.indexOf(stat) + 1;
            filtersToReset = currentFilters.subList(index, currentFilters.size());
            for (STATS statToRemove : filtersToReset) {
                setCriteriaValue(statToRemove, null);
            }
            getView().resetSelection(filtersToReset);
        }
        currentFilters.removeAll(filtersToReset);
    }

    private void setCriteriaValue(STATS stat, String category) {
        switch (stat) {
            case CHR:
                criteria.setChr(category != null ? category.substring(-1) : category);
                break;
            case INGENE:
                Boolean inGene = null;
                if (category != null && category.equalsIgnoreCase("genic")) {
                    inGene = true;
                } else if (category != null) {
                    inGene = false;
                }
                criteria.setInGene(inGene);
                break;
            case OVERFDR:
                Boolean overFDR = null;
                if (category != null && category.equalsIgnoreCase("significant")) {
                    overFDR = true;
                } else if (category != null) {
                    overFDR = false;
                }
                criteria.setOverFDR(overFDR);
                break;
            case ANNOTATION:
                criteria.setAnnotation(category);
                break;
        }
    }

    private MetaAnalysisRequest getContext() {
        if (ctx == null) {
            ctx = rf.metaAnalysisRequest();
        }
        return ctx;
    }
}
