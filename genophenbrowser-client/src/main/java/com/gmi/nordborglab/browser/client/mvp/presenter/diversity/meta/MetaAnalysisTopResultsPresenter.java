package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.FilterModifiedEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.MetaAnalysisTopResultsUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.*;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import javax.annotation.Nullable;
import javax.inject.Provider;
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

    public static final Object TYPE_FilterContent = new Object();

    private final CustomRequestFactory rf;

    private final PlaceManager placeManager;
    private MetaAnalysisTopResultsCriteriaProxy criteria;
    private List<FilterItemProxy> filterItems;
    private Map<STATS, DataTable> stats2DataTable = Maps.newHashMap();
    private List<FacetProxy> stats;
    private MetaAnalysisRequest ctx;
    private List<STATS> currentFilters = Lists.newArrayList();
    private final FilterPresenterWidget filterPresenterWidget;

    private AsyncDataProvider<MetaSNPAnalysisProxy> dataProvider = new AsyncDataProvider<MetaSNPAnalysisProxy>() {
        @Override
        protected void onRangeChanged(HasData<MetaSNPAnalysisProxy> display) {
            final Range range = display.getVisibleRange();
            getContext().findTopAnalysis(criteria, filterItems, range.getStart(), range.getLength()).fire(new Receiver<MetaSNPAnalysisPageProxy>() {
                @Override
                public void onSuccess(MetaSNPAnalysisPageProxy response) {
                    updateRowCount((int) response.getTotalElements(), true);
                    updateRowData(range.getStart(), response.getContents());
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

    public enum STATS {CHR, INGENE, OVERFDR, ANNOTATION, MAF}

    @Inject
    public MetaAnalysisTopResultsPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                           final CustomRequestFactory rf, final PlaceManager placeManager,
                                           final FilterPresenterWidget filterPresenterWidget,
                                           final Provider<TextBoxFilterItemPresenterWidget> textBoxFilterProvider,
                                           final Provider<DropDownFilterItemPresenterWidget> dropDownFilterProvider,
                                           final Provider<TypeaheadFilterItemPresenterWidget> typeaheadFilterProvider,
                                           final CurrentUser currentUser
    ) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        this.filterPresenterWidget = filterPresenterWidget;
        List<FilterItemPresenterWidget> filterWidgets = Lists.newArrayList();
        DropDownFilterItemPresenterWidget methodFilterWidget = dropDownFilterProvider.get();
        methodFilterWidget.setFilterType(ConstEnums.FILTERS.METHOD);
        methodFilterWidget.setAvailableOptions(Lists.newArrayList(Iterables.filter(Iterables.transform(currentUser.getAppData().getStudyProtocolList(), new Function<StudyProtocolProxy, String[]>() {
            @Nullable
            @Override
            public String[] apply(@Nullable StudyProtocolProxy studyProtocolProxy) {
                if (studyProtocolProxy == null)
                    return null;
                String[] retvalue = {studyProtocolProxy.getAnalysisMethod(), studyProtocolProxy.getId().toString()};
                return retvalue;
            }
        }), Predicates.notNull())));


        DropDownFilterItemPresenterWidget genotypeFilterWidget = dropDownFilterProvider.get();
        genotypeFilterWidget.setFilterType(ConstEnums.FILTERS.GENOTYPE);
        genotypeFilterWidget.setAvailableOptions(Lists.newArrayList(Iterables.filter(Iterables.transform(currentUser.getAppData().getAlleleAssayList(), new Function<AlleleAssayProxy, String[]>() {
            @Nullable
            @Override
            public String[] apply(@Nullable AlleleAssayProxy alleleAssay) {
                if (alleleAssay == null)
                    return null;
                String[] retvalue = {alleleAssay.getName(), alleleAssay.getId().toString()};
                return retvalue;
            }
        }), Predicates.notNull())));


        TypeaheadFilterItemPresenterWidget studyFilterWidget = typeaheadFilterProvider.get();
        studyFilterWidget.setFilterType(ConstEnums.FILTERS.STUDY);
        TypeaheadFilterItemPresenterWidget analysisFilterWidget = typeaheadFilterProvider.get();
        analysisFilterWidget.setFilterType(ConstEnums.FILTERS.ANALYSIS);

        TypeaheadFilterItemPresenterWidget phenotypeFilterWidget = typeaheadFilterProvider.get();
        phenotypeFilterWidget.setFilterType(ConstEnums.FILTERS.PHENOTYPE);
        TypeaheadFilterItemPresenterWidget candidateGeneListFilterWidget = typeaheadFilterProvider.get();
        candidateGeneListFilterWidget.setFilterType(ConstEnums.FILTERS.CANDIDATE_GENE_LIST);


        filterWidgets.add(analysisFilterWidget);
        filterWidgets.add(phenotypeFilterWidget);
        filterWidgets.add(studyFilterWidget);
        filterWidgets.add(genotypeFilterWidget);
        filterWidgets.add(methodFilterWidget);
        filterWidgets.add(candidateGeneListFilterWidget);

        filterPresenterWidget.setFilterItemWidgets(filterWidgets);
        this.rf = rf;
        this.placeManager = placeManager;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }


    @Override
    public void onBind() {
        super.onBind();
        setInSlot(TYPE_FilterContent, filterPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(FilterModifiedEvent.TYPE, filterPresenterWidget, new FilterModifiedEvent.Handler() {
            @Override
            public void onFilterModified(FilterModifiedEvent event) {
                fetchData();
            }
        }));
    }

    @Override
    public void onReset() {
        super.onReset();
        fetchData();
    }

    private void fetchData() {
        filterItems = getProxyFromFilter(filterPresenterWidget.getActiveFilterItems());
        getContext().findMetaStats(criteria, filterItems).fire(new Receiver<List<FacetProxy>>() {
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

    private List<FilterItemProxy> getProxyFromFilter(List<FilterItem> filterItems) {
        if (filterItems == null) {
            return null;
        }
        List<FilterItemProxy> filterItemProxies = Lists.newArrayList();
        for (FilterItem filterItem : filterItems) {
            FilterItemProxy filterItemProxy = filterItem.getProxy(getContext());
            filterItemProxies.add(filterItemProxy);
        }
        return filterItemProxies;
    }

    private void displayStats() {
        for (FacetProxy facet : stats) {
            STATS stat = STATS.valueOf(facet.getName().toUpperCase());
            if (!currentFilters.contains(stat)) {
                DataTable dataTable = DataTableUtils.createFroMFacets(facet);
                stats2DataTable.put(stat, dataTable);
                getView().setStatsData(dataTable, stat);
            }
        }
        getView().scheduleLayout();
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
            case MAF:
                setMafFromCategory(category);
                break;
        }
    }

    private void setMafFromCategory(String category) {
        Double mafFrom = null;
        Double mafTo = null;
        CharMatcher numberMatcher = CharMatcher.DIGIT.or(CharMatcher.is('.'));
        try {
            if (CharMatcher.is('-').matchesAnyOf(category)) {
                Iterable<String> range = Splitter.on("-").trimResults().split(category);
                mafFrom = Double.valueOf(numberMatcher.retainFrom(Iterables.getFirst(range, null)));
                mafTo = Double.valueOf(numberMatcher.retainFrom(Iterables.getLast(range, null)));
            } else if (CharMatcher.is('<').matchesAnyOf(category)) {
                mafTo = Double.valueOf(numberMatcher.retainFrom(category));
            } else {
                mafFrom = Double.valueOf(numberMatcher.retainFrom(category));
            }
        } catch (Exception e) {

        }
        if (mafFrom != null) {
            mafFrom = mafFrom / 100;
        }
        if (mafTo != null) {
            mafTo = mafTo / 100;
        }
        criteria.setMafFrom(mafFrom);
        criteria.setMafTo(mafTo);
    }

    private MetaAnalysisRequest getContext() {
        if (ctx == null) {
            ctx = rf.metaAnalysisRequest();
        }
        return ctx;
    }
}
