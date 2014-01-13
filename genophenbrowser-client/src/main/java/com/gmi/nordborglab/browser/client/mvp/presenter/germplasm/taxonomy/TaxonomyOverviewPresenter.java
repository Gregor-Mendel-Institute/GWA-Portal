package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy;

import com.gmi.nordborglab.browser.client.events.LoadTaxonomiesEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.TaxonomyOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

public class TaxonomyOverviewPresenter
        extends
        Presenter<TaxonomyOverviewPresenter.MyView, TaxonomyOverviewPresenter.MyProxy> implements TaxonomyOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<TaxonomyOverviewUiHandlers> {

        void setTaxonomies(List<TaxonomyProxy> taxonomies);
    }

    private final PlaceManager placeManager;
    private List<TaxonomyProxy> taxonomies;

    @ProxyCodeSplit
    @NameToken(NameTokens.taxonomies)
    public interface MyProxy extends ProxyPlace<TaxonomyOverviewPresenter> {
    }


    @Inject
    public TaxonomyOverviewPresenter(final EventBus eventBus, final MyView view,
                                     final MyProxy proxy,
                                     final PlaceManager placeManager) {
        super(eventBus, view, proxy, GermplasmPresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
    }


    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
    public void onReset() {
        if (taxonomies != null) {
            getView().setTaxonomies(taxonomies);
        }
    }

    @Override
    public void onClickTaxonomy(TaxonomyProxy taxonomy) {
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(NameTokens.taxonomy)
                .with("id", taxonomy.getId().toString()).build();
        placeManager.revealPlace(request);
    }

    @ProxyEvent
    public void onLoadTaxonomies(LoadTaxonomiesEvent event) {
        taxonomies = event.getTaxonomies();
        getView().setTaxonomies(taxonomies);
    }

}
