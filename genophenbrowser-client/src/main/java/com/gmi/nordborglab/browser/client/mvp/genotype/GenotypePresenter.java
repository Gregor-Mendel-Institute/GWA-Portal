package com.gmi.nordborglab.browser.client.mvp.genotype;

import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.search.SearchPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class GenotypePresenter extends
        Presenter<GenotypePresenter.MyView, GenotypePresenter.MyProxy> {


    public interface MyView extends com.gwtplatform.mvp.client.View {

        void setTitle(String title);

        void setActiveMenuItem(GenotypeView.MENU_ITEM menuItem);
    }

    @ProxyCodeSplit
    public interface MyProxy extends Proxy<GenotypePresenter> {
    }

    public static final NestedSlot SLOT_CONTENT = new NestedSlot();
    static final PermanentSlot<SearchPresenter> SLOT_SEARCH = new PermanentSlot<>();

    private final PlaceManager placeManager;
    private final SearchPresenter searchPresenter;

    @Inject
    public GenotypePresenter(final EventBus eventBus, final MyView view,
                             final MyProxy proxy, final PlaceManager placeManager,
                             final SearchPresenter searchPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN_CONTENT);
        this.placeManager = placeManager;
        this.searchPresenter = searchPresenter;
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(SLOT_SEARCH, searchPresenter);
    }

    @Override
    protected void onReset() {
        super.onReset();
        setTitleAndMenuItem();
    }

    private String getTitleFromPlace(PlaceRequest request) {
        String title = null;
        if (request.matchesNameToken(NameTokens.genomebrowser)) {
            title = "Genome Browser";
        } else if (request.matchesNameToken(NameTokens.snpviewer)) {
            title = "SNP Viewer";
        }
        return title;
    }

    private void setTitleAndMenuItem() {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        String title = getTitleFromPlace(request);
        GenotypeView.MENU_ITEM menuItem = getMenuItemFromRequest(request);
        getView().setTitle(title);
        getView().setActiveMenuItem(menuItem);
    }

    private GenotypeView.MENU_ITEM getMenuItemFromRequest(PlaceRequest request) {
        GenotypeView.MENU_ITEM menuItem = null;
        if (request.matchesNameToken(NameTokens.genomebrowser)) {
            menuItem = GenotypeView.MENU_ITEM.GENOMEBROWSER;
        } else if (request.matchesNameToken(NameTokens.snpviewer)) {
            menuItem = GenotypeView.MENU_ITEM.SNPVIEWER;
        }
        return menuItem;
    }
}
