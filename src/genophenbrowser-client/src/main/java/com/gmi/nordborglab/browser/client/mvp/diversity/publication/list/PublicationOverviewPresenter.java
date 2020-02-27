package com.gmi.nordborglab.browser.client.mvp.diversity.publication.list;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.PublicationPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/2/13
 * Time: 6:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationOverviewPresenter extends
        Presenter<PublicationOverviewPresenter.MyView, PublicationOverviewPresenter.MyProxy> implements PublicationOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<PublicationOverviewUiHandlers> {

        HasData<PublicationProxy> getDisplay();

        void setSearchString(String searchString);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.publications)
    public interface MyProxy extends ProxyPlace<PublicationOverviewPresenter> {

    }

    protected final AsyncDataProvider<PublicationProxy> dataProvider;
    protected final PlaceManager placeManager;

    protected final ExperimentManager experimentManager;
    protected String searchString;

    @Inject
    public PublicationOverviewPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                        final ExperimentManager experimentManager, final PlaceManager placeManager) {
        super(eventBus, view, proxy, DiversityPresenter.SLOT_CONTENT);
        this.placeManager = placeManager;
        this.experimentManager = experimentManager;
        getView().setUiHandlers(this);
        dataProvider = new AsyncDataProvider<PublicationProxy>() {

            @Override
            protected void onRangeChanged(HasData<PublicationProxy> display) {
                requestPublications(display.getVisibleRange());
            }
        };
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    private void requestPublications(final Range range) {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<PublicationPageProxy> receiver = new Receiver<PublicationPageProxy>() {
            @Override
            public void onSuccess(PublicationPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) studies.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studies.getContents());
            }
        };
        experimentManager.findAllPublications(receiver, searchString, range.getStart(), range.getLength());
    }


    @Override
    protected void onReset() {
        super.onReset();
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        String newSearchString = request.getParameter("query", null);
        if (newSearchString != searchString) {
            searchString = newSearchString;
            getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            getView().setSearchString(searchString);
        }
    }


    @Override
    public void updateSearchString(String searchString) {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.publications);
        if (searchString != null && !searchString.equals("")) {
            request.with("query", searchString);
        }
        placeManager.revealPlace(request.build());
    }

}
