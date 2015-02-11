package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
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
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;


public class ExperimentsOverviewPresenter
        extends
        Presenter<ExperimentsOverviewPresenter.MyView, ExperimentsOverviewPresenter.MyProxy> implements ExperimentsOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<ExperimentsOverviewUiHandlers> {
        HasData<ExperimentProxy> getDisplay();
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
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    @Inject
    public ExperimentsOverviewPresenter(final EventBus eventBus,
                                        final MyView view, final MyProxy proxy,
                                        final ExperimentManager experimentManager,
                                        final PlaceManager placeManager,
                                        final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, ExperimentsOverviewTabPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
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
                facetSearchPresenterWidget.displayFacets(experiments.getFacets());
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        experimentManager.findAll(receiver, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength());
    }



    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getDisplay());
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(FacetSearchChangeEvent.TYPE, facetSearchPresenterWidget, new FacetSearchChangeEvent.Handler() {

            @Override
            public void onChanged(FacetSearchChangeEvent event) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}
