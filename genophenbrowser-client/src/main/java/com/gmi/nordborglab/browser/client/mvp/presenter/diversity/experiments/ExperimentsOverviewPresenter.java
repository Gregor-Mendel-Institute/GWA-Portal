package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;


public class ExperimentsOverviewPresenter
        extends
        Presenter<ExperimentsOverviewPresenter.MyView, ExperimentsOverviewPresenter.MyProxy> implements ExperimentsOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<ExperimentsOverviewUiHandlers> {
        HasData<ExperimentProxy> getDisplay();

        void setActiveNavLink(ConstEnums.TABLE_FILTER filter);

        void displayFacets(List<FacetProxy> facets, String searchString);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.experiments)
    @TabInfo(container = ExperimentsOverviewTabPresenter.class,
            label = "Overview",
            priority = 0)
    @Title("Studies")
    public interface MyProxy extends TabContentProxyPlace<ExperimentsOverviewPresenter> {
    }


    private final ExperimentManager experimentManager;
    private final PlaceManager placeManager;
    protected final AsyncDataProvider<ExperimentProxy> dataProvider;
    private ConstEnums.TABLE_FILTER currentFilter = ConstEnums.TABLE_FILTER.ALL;
    private String searchString = null;
    private List<FacetProxy> facets;
    public static final PlaceRequest place = new PlaceRequest(NameTokens.experiments);

    @Inject
    public ExperimentsOverviewPresenter(final EventBus eventBus,
                                        final MyView view, final MyProxy proxy,
                                        final ExperimentManager experimentManager,
                                        final PlaceManager placeManager) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        this.experimentManager = experimentManager;
        this.placeManager = placeManager;
        dataProvider = new AsyncDataProvider<ExperimentProxy>() {

            @Override
            protected void onRangeChanged(HasData<ExperimentProxy> display) {
                requestExperiments();
            }
        };
    }

    protected void requestExperiments() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<ExperimentPageProxy> receiver = new Receiver<ExperimentPageProxy>() {
            @Override
            public void onSuccess(ExperimentPageProxy experiments) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) experiments.getTotalElements(), true);
                dataProvider.updateRowData(getView().getDisplay().getVisibleRange().getStart(), experiments.getContents());
                facets = experiments.getFacets();
                getView().displayFacets(facets, searchString);
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        experimentManager.findAll(receiver, currentFilter, searchString, range.getStart(), range.getLength());
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, ExperimentsOverviewTabPresenter.TYPE_SetTabContent,
                this);
    }

    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    @Override
    protected void onReset() {
        super.onReset();
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        ConstEnums.TABLE_FILTER newFilter = ConstEnums.TABLE_FILTER.ALL;
        String newCategoryString = request.getParameter("filter", null);
        String newSearchString = request.getParameter("query", null);
        if (newCategoryString != null) {
            try {
                newFilter = ConstEnums.TABLE_FILTER.valueOf(newCategoryString);
            } catch (Exception e) {

            }
        }
        if (newFilter != currentFilter || newSearchString != searchString) {
            currentFilter = newFilter;
            searchString = newSearchString;
            getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
        }
        getView().setActiveNavLink(currentFilter);
    }



    @Override
    public void updateSearchString(String value) {
        PlaceRequest request = place;
        if (currentFilter != null) {
            request = request.with("filter", currentFilter.name());
        }
        if (value != null && !value.equals("")) {
            request = request.with("query", value);
        }
        placeManager.revealPlace(request);
    }
}
