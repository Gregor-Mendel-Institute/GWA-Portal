package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
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

import java.util.List;

public class StudyOverviewPresenter
        extends
        Presenter<StudyOverviewPresenter.MyView, StudyOverviewPresenter.MyProxy> implements StudyOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<StudyOverviewUiHandlers> {

        HasData<StudyProxy> getDisplay();

        void setActiveNavLink(ConstEnums.TABLE_FILTER filter);

        void displayFacets(List<FacetProxy> facets, String searchString);

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.studyoverview)
    public interface MyProxy extends ProxyPlace<StudyOverviewPresenter> {

    }

    protected final AsyncDataProvider<StudyProxy> dataProvider;
    protected final CdvManager cdvManager;
    private ConstEnums.TABLE_FILTER currentFilter = ConstEnums.TABLE_FILTER.ALL;
    private String searchString = null;
    private List<FacetProxy> facets;
    private final PlaceManager placeManager;
    public static final String placeToken = NameTokens.studyoverview;

    @Inject
    public StudyOverviewPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy, final CdvManager cdvManager, final PlaceManager placeManager) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.cdvManager = cdvManager;
        this.placeManager = placeManager;
        dataProvider = new AsyncDataProvider<StudyProxy>() {

            @Override
            protected void onRangeChanged(HasData<StudyProxy> display) {
                requestStudies(display.getVisibleRange());
            }
        };
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    protected void requestStudies(final Range range) {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
            @Override
            public void onSuccess(StudyPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) studies.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studies.getContents());
                facets = studies.getFacets();
                getView().displayFacets(facets, searchString);
            }
        };
        cdvManager.findAll(receiver, currentFilter, searchString, range.getStart(), range.getLength());
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
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(placeToken);
        if (currentFilter != null) {
            builder = builder.with("filter", currentFilter.name());
        }
        if (searchString != null && !searchString.equals("")) {
            builder = builder.with("query", searchString);
        }
        placeManager.revealPlace(builder.build());
    }
}
