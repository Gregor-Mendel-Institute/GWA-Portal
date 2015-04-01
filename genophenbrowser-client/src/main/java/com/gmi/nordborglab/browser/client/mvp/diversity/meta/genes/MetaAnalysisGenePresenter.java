package com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes;

import com.gmi.nordborglab.browser.client.events.FilterModifiedEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.DropDownFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TypeaheadFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.FilterItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import javax.inject.Provider;
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
    private final static int DEFAULT_INTERVAL = 10000;
    private int leftInterval = DEFAULT_INTERVAL;
    private int rightInterval = DEFAULT_INTERVAL;
    private final FilterPresenterWidget filterPresenterWidget;
    private List<FilterItemProxy> filterItems;
    private MetaAnalysisRequest ctx;
    private final SearchManager searchManager;

    public static final Object TYPE_FilterContent = new Object();

    private AsyncDataProvider<MetaSNPAnalysisProxy> dataProvider = new AsyncDataProvider<MetaSNPAnalysisProxy>() {
        @Override
        protected void onRangeChanged(HasData<MetaSNPAnalysisProxy> display) {
            if (gene == null) {
                return;
            }
            final Range range = display.getVisibleRange();
            fireEvent(new LoadingIndicatorEvent(true));
            getContext().findAllAnalysisForRegion((int) gene.getStart() - leftInterval, (int) gene.getEnd() + rightInterval, gene.getChr(), range.getStart(), range.getLength(), filterItems).fire(new Receiver<MetaSNPAnalysisPageProxy>() {
                @Override
                public void onSuccess(MetaSNPAnalysisPageProxy response) {
                    updateRowCount((int) response.getTotalElements(), true);
                    updateRowData(range.getStart(), response.getContents());
                    ctx = null;
                    fireEvent(new LoadingIndicatorEvent(false));
                }

                @Override
                public void onFailure(ServerFailure error) {
                    ctx = null;
                    fireEvent(new LoadingIndicatorEvent(false));
                    super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
                }
            });
        }
    };

    @Inject
    public MetaAnalysisGenePresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                     final CustomRequestFactory rf, final PlaceManager placeManager,
                                     final FilterPresenterWidget filterPresenterWidget,
                                     final Provider<DropDownFilterItemPresenterWidget> dropDownFilterProvider,
                                     final Provider<TypeaheadFilterItemPresenterWidget> typeaheadFilterProvider,
                                     final CurrentUser currentUser,
                                     final SearchManager searchManager) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.filterPresenterWidget = filterPresenterWidget;
        this.placeManager = placeManager;
        this.searchManager = searchManager;

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
        List<FilterItemPresenterWidget> filterWidgets = Lists.newArrayList();
        filterWidgets.add(analysisFilterWidget);
        filterWidgets.add(phenotypeFilterWidget);
        filterWidgets.add(studyFilterWidget);
        filterWidgets.add(genotypeFilterWidget);
        filterWidgets.add(methodFilterWidget);
        filterPresenterWidget.setFilterItemWidgets(filterWidgets);
    }

    @Override
    public void onBind() {
        super.onBind();
        setInSlot(TYPE_FilterContent, filterPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(FilterModifiedEvent.TYPE, filterPresenterWidget, new FilterModifiedEvent.Handler() {
            @Override
            public void onFilterModified(FilterModifiedEvent event) {
                fetchMetaAnalysisData();
            }
        }));
    }


    @Override
    public void onSearchForGene(final String request, final SearchManager.SearchCallback callback) {
        searchManager.searchGeneByTerm(request, callback);
    }

    @Override
    public void onSelectGene(String gene) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        placeManager.revealPlace(request.with("id", gene).build());
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
                fireEvent(new LoadingIndicatorEvent(false));
                gene = response;
                updateView();
                fetchMetaAnalysisData();
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

    private void fetchMetaAnalysisData() {
        filterItems = getProxyFromFilter(filterPresenterWidget.getActiveFilterItems());
        if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
            Range range = getView().getDisplay().getVisibleRange();
            getView().getDisplay().setVisibleRangeAndClearData(range, true);
        } else {
            dataProvider.addDataDisplay(getView().getDisplay());
        }
    }

    private void reset() {
        gene = null;
        resetView();
        getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
        //dataProvider.setList(Lists.<MetaSNPAnalysisProxy>newArrayList());
        /*if (dataProvider.getDataDisplays().contains(getView().getDisplay())) {
            dataProvider.removeDataDisplay(getView().getDisplay());
        } */
    }

    private void resetView() {
        getView().reset();
    }

    @Override
    public void onSelectMetaAnalysis(MetaSNPAnalysisProxy metaAnalysis) {
        getView().setGeneViewerSelection(metaAnalysis.getSnpInfo().getPosition());
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

    private MetaAnalysisRequest getContext() {
        if (ctx == null) {
            ctx = rf.metaAnalysisRequest();
        }
        return ctx;
    }
}
