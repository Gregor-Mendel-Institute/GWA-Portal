package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.UserListUiHandler;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.AppUserPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
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

import java.util.List;

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

        void setActiveNavLink(ConstEnums.USER_FILTER currentFilter);

        void displayFacets(List<FacetProxy> facets);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.userlist)
    public interface MyProxy extends ProxyPlace<UserListPresenter> {
    }


    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;
    private ConstEnums.USER_FILTER currentFilter = ConstEnums.USER_FILTER.ALL;
    private String searchString = null;
    private List<FacetProxy> facets;

    private final AsyncDataProvider<AppUserProxy> dataProvider = new AsyncDataProvider<AppUserProxy>() {
        @Override
        protected void onRangeChanged(HasData<AppUserProxy> display) {
            requestUsers();
        }
    };


    @Inject
    public UserListPresenter(EventBus eventBus, MyView view, MyProxy proxy, final PlaceManager placeManager, final CustomRequestFactory rf) {
        super(eventBus, view, proxy, MainPagePresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.placeManager = placeManager;
    }


    @Override
    protected void onReset() {
        super.onReset();
        if (!dataProvider.getDataDisplays().contains(getView().getDisplay())) {
            dataProvider.addDataDisplay(getView().getDisplay());
        }
    }

    @Override
    public void selectFilter(ConstEnums.USER_FILTER filter) {
        if (filter != currentFilter) {
            currentFilter = filter;
            getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            getView().setActiveNavLink(currentFilter);

        }
    }

    @Override
    public void updateSearchString(String value) {
        searchString = value;
        getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
    }

    private void requestUsers() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<AppUserPageProxy> receiver = new Receiver<AppUserPageProxy>() {
            @Override
            public void onSuccess(AppUserPageProxy users) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) users.getTotalElements(), true);
                dataProvider.updateRowData(getView().getDisplay().getVisibleRange().getStart(), users.getContents());
                facets = users.getFacets();
                getView().displayFacets(facets);
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        rf.userRequest().findUsers(searchString, currentFilter, range.getStart(), range.getLength()).fire(receiver);

    }
}
