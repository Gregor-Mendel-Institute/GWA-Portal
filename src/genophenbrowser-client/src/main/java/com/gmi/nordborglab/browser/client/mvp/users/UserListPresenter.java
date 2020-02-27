package com.gmi.nordborglab.browser.client.mvp.users;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.AppUserPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
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

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class UserListPresenter extends Presenter<UserListPresenter.MyView, UserListPresenter.MyProxy> implements UserListUiHandler {

    public interface MyView extends View, HasUiHandlers<UserListUiHandler> {

        HasData<AppUserProxy> getDisplay();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.userlist)
    public interface MyProxy extends ProxyPlace<UserListPresenter> {

    }

    private final CustomRequestFactory rf;


    private final PlaceManager placeManager;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    private final AsyncDataProvider<AppUserProxy> dataProvider = new AsyncDataProvider<AppUserProxy>() {
        @Override
        protected void onRangeChanged(HasData<AppUserProxy> display) {
            requestUsers();
        }
    };


    @Inject
    public UserListPresenter(EventBus eventBus, MyView view, MyProxy proxy, final PlaceManager placeManager, final CustomRequestFactory rf,
                             final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN_CONTENT);
        facetSearchPresenterWidget.setDefaultFilter("ALL");
        facetSearchPresenterWidget.initFixedFacets(FacetSearchPresenterWidget.USER_MAP);
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        getView().setUiHandlers(this);
        this.rf = rf;
        this.placeManager = placeManager;
    }


    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetSearchPresenterWidget);
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
        if (!dataProvider.getDataDisplays().contains(getView().getDisplay())) {
            dataProvider.addDataDisplay(getView().getDisplay());
        }
    }

    private void requestUsers() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<AppUserPageProxy> receiver = new Receiver<AppUserPageProxy>() {
            @Override
            public void onSuccess(AppUserPageProxy users) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) users.getTotalElements(), true);
                dataProvider.updateRowData(getView().getDisplay().getVisibleRange().getStart(), users.getContents());
                facetSearchPresenterWidget.displayFacets(users.getFacets());
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        rf.userRequest().findUsers(facetSearchPresenterWidget.getSearchString(), ConstEnums.USER_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), range.getStart(), range.getLength()).fire(receiver);

    }
}
