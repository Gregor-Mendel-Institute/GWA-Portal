package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PhenotypeOverviewPresenter
        extends
        Presenter<PhenotypeOverviewPresenter.MyView, PhenotypeOverviewPresenter.MyProxy> implements PhenotypeOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<PhenotypeOverviewUiHandlers> {

        HasData<PhenotypeProxy> getDisplay();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.phenotypeoverview)
    public interface MyProxy extends ProxyPlace<PhenotypeOverviewPresenter> {

    }

    private final PhenotypeManager phenotypeManager;
    protected final AsyncDataProvider<PhenotypeProxy> dataProvider;
    protected final PlaceManager placeManager;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    @Inject
    public PhenotypeOverviewPresenter(final EventBus eventBus, final MyView view,
                                      final MyProxy proxy, final PhenotypeManager phenotypeManager, final PlaceManager placeManager,
                                      final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.placeManager = placeManager;
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        getView().setUiHandlers(this);
        this.phenotypeManager = phenotypeManager;
        dataProvider = new AsyncDataProvider<PhenotypeProxy>() {

            @Override
            protected void onRangeChanged(HasData<PhenotypeProxy> display) {
                requestPhenotypes(display.getVisibleRange());
            }
        };
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    protected void requestPhenotypes(final Range range) {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<PhenotypePageProxy> receiver = new Receiver<PhenotypePageProxy>() {
            @Override
            public void onSuccess(PhenotypePageProxy phenotypes) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) phenotypes.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), phenotypes.getContents());
                facetSearchPresenterWidget.displayFacets(phenotypes.getFacets());
            }
        };
        phenotypeManager.findAll(receiver, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength());
    }

    @Override
    protected void onBind() {
        super.onBind();
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
