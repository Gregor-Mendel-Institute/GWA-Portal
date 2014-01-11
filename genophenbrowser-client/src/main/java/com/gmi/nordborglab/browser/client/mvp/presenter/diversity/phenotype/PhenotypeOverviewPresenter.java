package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;

public class PhenotypeOverviewPresenter
        extends
        Presenter<PhenotypeOverviewPresenter.MyView, PhenotypeOverviewPresenter.MyProxy> implements PhenotypeOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<PhenotypeOverviewUiHandlers> {

        HasData<PhenotypeProxy> getDisplay();

        void setActiveNavLink(ConstEnums.TABLE_FILTER filter);

        void displayFacets(List<FacetProxy> facets, String searchString);

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.phenotypeoverview)
    public interface MyProxy extends ProxyPlace<PhenotypeOverviewPresenter> {

    }

    private final PhenotypeManager phenotypeManager;
    protected final AsyncDataProvider<PhenotypeProxy> dataProvider;
    protected final PlaceManager placeManager;
    private ConstEnums.TABLE_FILTER currentFilter = ConstEnums.TABLE_FILTER.ALL;
    private String searchString = null;
    private List<FacetProxy> facets;
    public static final PlaceRequest place = new PlaceRequest(NameTokens.phenotypeoverview);

    @Inject
    public PhenotypeOverviewPresenter(final EventBus eventBus, final MyView view,
                                      final MyProxy proxy, final PhenotypeManager phenotypeManager, final PlaceManager placeManager) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
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
                facets = phenotypes.getFacets();
                getView().displayFacets(facets, searchString);
            }
        };
        phenotypeManager.findAll(receiver, currentFilter, searchString, range.getStart(), range.getLength());
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
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
    public void updateSearchString(String searchString) {
        PlaceRequest request = place;
        if (currentFilter != null) {
            request = request.with("filter", currentFilter.name());
        }
        if (searchString != null && !searchString.equals("")) {
            request = request.with("query", searchString);
        }
        placeManager.revealPlace(request);
    }
}
