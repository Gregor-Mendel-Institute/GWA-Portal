package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListEnrichmentPresenterWidget extends PresenterWidget<CandidateGeneListEnrichmentPresenterWidget.MyView> implements CandidateGeneListEnrichmentUiHandlers {


    public interface MyView extends View, HasUiHandlers<CandidateGeneListEnrichmentUiHandlers> {

        void displayType(ConstEnums.ENRICHMENT_FILTER type);

        HasData<CandidateGeneListEnrichmentProxy> getFinishedDisplay();

        HasData<CandidateGeneListEnrichmentProxy> getRunningDisplay();

        HasData<CandidateGeneListEnrichmentProxy> getAvailableDisplay();

        void enableRunBtn(boolean enabled);

        CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState getCheckBoxState();

        void redrawhHeader();

        void setMaxPvalue(double maxPvalue);

    }


    private boolean updateFacets = true;
    private final CustomRequestFactory rf;
    private boolean isSelected = false;
    private final EnrichmentProvider dataProvider;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;
    private static final Map<String, String> FACET_MAP = ImmutableMap.<String, String>builder()
            .put(ConstEnums.ENRICHMENT_FILTER.FINISHED.name(), "Finished")
            .put(ConstEnums.ENRICHMENT_FILTER.RUNNING.name(), "Running")
            .put(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name(), "Available")
            .build();

    private class EnrichmentDataProvider extends AsyncDataProvider<CandidateGeneListEnrichmentProxy> {

        private final ConstEnums.ENRICHMENT_FILTER filter;

        private EnrichmentDataProvider(ConstEnums.ENRICHMENT_FILTER filter) {
            this.filter = filter;
        }

        @Override
        protected void onRangeChanged(HasData<CandidateGeneListEnrichmentProxy> display) {
            if (ConstEnums.ENRICHMENT_FILTER.valueOf(facetSearchPresenterWidget.getFilter()) == filter) {
                clearSelection();
                requestEnrichments(display);
            }
        }
    }

    private final AsyncDataProvider<CandidateGeneListEnrichmentProxy> finisheDataProvider = new EnrichmentDataProvider(ConstEnums.ENRICHMENT_FILTER.FINISHED);
    private final AsyncDataProvider<CandidateGeneListEnrichmentProxy> runningDataProvider = new EnrichmentDataProvider(ConstEnums.ENRICHMENT_FILTER.RUNNING);
    private final AsyncDataProvider<CandidateGeneListEnrichmentProxy> availableDataProvider = new EnrichmentDataProvider(ConstEnums.ENRICHMENT_FILTER.AVAILABLE);

    @Inject
    public CandidateGeneListEnrichmentPresenterWidget(EventBus eventBus, final CustomRequestFactory rf,
                                                      final ClientModule.AssistedInjectionFactory factory, final FacetSearchPresenterWidget facetSearchPresenterWidget,
                                                      @Assisted final EnrichmentProvider dataProvider) {
        super(eventBus, factory.getCandidateGeneListEnrichmentView(dataProvider.getViewType()));
        this.rf = rf;
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        getView().setUiHandlers(this);
        this.dataProvider = dataProvider;
        facetSearchPresenterWidget.setDefaultFilter(ConstEnums.ENRICHMENT_FILTER.FINISHED.name());
        if (dataProvider.getViewType() == EnrichmentProvider.TYPE.CANDIDATE_GENE_LIST || dataProvider.getViewType() == EnrichmentProvider.TYPE.STUDY) {
            facetSearchPresenterWidget.initFixedFacets(FACET_MAP);
        } else {
            facetSearchPresenterWidget.initFixedFacets(Maps.filterEntries(FACET_MAP, new Predicate<Map.Entry<String, String>>() {
                @Override
                public boolean apply(@Nullable Map.Entry<String, String> input) {
                    return !ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name().equals(input.getKey());
                }
            }));
        }
    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        finisheDataProvider.addDataDisplay(getView().getFinishedDisplay());
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
        registerHandler(getView().getAvailableDisplay().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<CandidateGeneListEnrichmentProxy> selectedItems = getSelectedRecords();
                int size = selectedItems.size();
                boolean newSelected = size > 0;
                if (isSelected != newSelected) {
                    isSelected = newSelected;
                    getView().enableRunBtn(isSelected);
                }
                getView().getCheckBoxState().setCheckedCount(size);
                getView().redrawhHeader();
            }
        }));
        registerHandler(getEventBus().addHandlerToSource(FacetSearchChangeEvent.TYPE, facetSearchPresenterWidget, new FacetSearchChangeEvent.Handler() {

            @Override
            public void onChanged(FacetSearchChangeEvent event) {
                final ConstEnums.ENRICHMENT_FILTER filter = ConstEnums.ENRICHMENT_FILTER.valueOf(facetSearchPresenterWidget.getFilter());
                if (filter == ConstEnums.ENRICHMENT_FILTER.AVAILABLE && (dataProvider.getViewType() != EnrichmentProvider.TYPE.CANDIDATE_GENE_LIST && dataProvider.getViewType() != EnrichmentProvider.TYPE.STUDY)) {
                    return;
                }
                if (event.isSearchStringChanged()) {
                    updateFacets = true;
                }
                refreshView();
            }
        }));
        availableDataProvider.addDataDisplay(getView().getAvailableDisplay());
        runningDataProvider.addDataDisplay(getView().getRunningDisplay());
    }

    private void requestEnrichments(final HasData<CandidateGeneListEnrichmentProxy> display) {
        if (dataProvider.getEntity() == null) {
            return;
        }
        fireEvent(new LoadingIndicatorEvent(true));
        final ConstEnums.ENRICHMENT_FILTER currentFilter = ConstEnums.ENRICHMENT_FILTER.valueOf(facetSearchPresenterWidget.getFilter());
        Receiver<CandidateGeneListEnrichmentPageProxy> receiver = new Receiver<CandidateGeneListEnrichmentPageProxy>() {
            @Override
            public void onSuccess(CandidateGeneListEnrichmentPageProxy candidateGeneListEnrichments) {
                fireEvent(new LoadingIndicatorEvent(false));
                if (currentFilter == ConstEnums.ENRICHMENT_FILTER.FINISHED) {
                    double maxPvalue = candidateGeneListEnrichments.getFacets().get(0).getTerms().get(0).getValue();
                    getView().setMaxPvalue(maxPvalue);
                }
                display.setRowCount((int) candidateGeneListEnrichments.getTotalElements(), true);
                display.setRowData(display.getVisibleRange().getStart(), candidateGeneListEnrichments.getContents());
                getView().displayType(currentFilter);
                if (currentFilter == ConstEnums.ENRICHMENT_FILTER.AVAILABLE) {
                    getView().getCheckBoxState().setTotalCount((int) candidateGeneListEnrichments.getTotalElements());
                }
            }
        };
        Range range = display.getVisibleRange();
        dataProvider.fetchData(currentFilter, facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength(), receiver);
    }

    @Override
    public void onReset() {
        super.onReset();
    }


    public void refresh() {
        updateFacets = true;
        refreshView();
    }

    public void setCandidateGeneList(CandidateGeneListProxy candidateGeneList) {

    }


    private void refreshView() {
        if (updateFacets) {
            dataProvider.findEnrichmentStats(facetSearchPresenterWidget.getSearchString(), new Receiver<List<FacetProxy>>() {
                @Override
                public void onSuccess(List<FacetProxy> response) {
                    facetSearchPresenterWidget.displayFacets(response);
                }
            });

        }
        HasData<CandidateGeneListEnrichmentProxy> display = getDisplay();
        if (display != null) {
            display.setVisibleRangeAndClearData(display.getVisibleRange(), true);
        }
        getView().enableRunBtn(getSelectedRecords().size() > 0);
    }


    private HasData<CandidateGeneListEnrichmentProxy> getDisplay() {
        switch (ConstEnums.ENRICHMENT_FILTER.valueOf(facetSearchPresenterWidget.getFilter())) {
            case FINISHED:
                return getView().getFinishedDisplay();
            case RUNNING:
                return getView().getRunningDisplay();
            case AVAILABLE:
                return getView().getAvailableDisplay();
        }
        return null;
    }

    @Override
    public void selectVisibleRecords(Boolean select) {
        for (CandidateGeneListEnrichmentProxy enrichment : getView().getAvailableDisplay().getVisibleItems()) {
            getView().getAvailableDisplay().getSelectionModel().setSelected(enrichment, select);
        }
    }

    @Override
    public void selectAllRecords(boolean clearSelection) {
        if (clearSelection) {
            clearSelection();
        }
    }

    @Override
    public void onRunEnrichment() {
        boolean isAllChecked = isAllRecordsChecked();
        Set<CandidateGeneListEnrichmentProxy> selectedRecords = null;
        if (!isAllChecked) {
            selectedRecords = getSelectedRecords();
            clearSelection();
            if (selectedRecords.size() == 0)
                return;
        } else {
            getView().getCheckBoxState().setShowCheckAll(false);
            getView().getCheckBoxState().setCheckedCount(0);
        }
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        dataProvider.createEnrichments(selectedRecords, isAllChecked, new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                fireEvent(new LoadingIndicatorEvent(false));
                facetSearchPresenterWidget.setFilter(ConstEnums.ENRICHMENT_FILTER.RUNNING.name());
                updateFacets = true;
                refreshView();
            }
        });
    }

    private void clearSelection() {
        ((MultiSelectionModel<CandidateGeneListEnrichmentProxy>) getView().getAvailableDisplay().getSelectionModel()).clear();
        getView().enableRunBtn(false);
    }

    private boolean isAllRecordsChecked() {
        CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState state = getView().getCheckBoxState();
        return state.showCheckAll() && state.totalCount() == state.checkedCount();
    }

    private Set<CandidateGeneListEnrichmentProxy> getSelectedRecords() {
        return ((MultiSelectionModel<CandidateGeneListEnrichmentProxy>) getView().getAvailableDisplay().getSelectionModel()).getSelectedSet();
    }


}
