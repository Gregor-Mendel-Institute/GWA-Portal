package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy;

import java.util.Set;

import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.mvp.client.HasUiHandlers;

import javax.validation.ConstraintViolation;

import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadTaxonomyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.TaxonomyManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.TaxonomyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy.TaxonomyDetailView.TaxonomyDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy.TaxonomyDetailView.TaxonomyEditDriver;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyStatsProxy;
import com.gmi.nordborglab.browser.shared.service.TaxonomyRequest;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.gwtplatform.mvp.client.Presenter;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class TaxonomyDetailPresenter
        extends
        Presenter<TaxonomyDetailPresenter.MyView, TaxonomyDetailPresenter.MyProxy> implements TaxonomyDetailUiHandlers {

    public interface MyView extends View, HasUiHandlers<TaxonomyDetailUiHandlers> {
        TaxonomyDisplayDriver getDisplayDriver();

        void setState(State editing, int permission);

        TaxonomyEditDriver getEditDriver();

        void setGeoChartData(DataTable createDataTableFromString);

        void setAlleleAssayData(DataTable createDataTableFromString);

        void setSampStatData(DataTable createDataTableFromString);

        void scheduleLayout();

        void setStockGenerationData(DataTable createDataTableFromString);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.taxonomy)
    public interface MyProxy extends ProxyPlace<TaxonomyDetailPresenter> {
    }

    private TaxonomyProxy taxonomy;
    private final PlaceManager placeManager;
    private boolean fireLoadEvent = false;
    private final TaxonomyManager taxonomyManager;
    private final CurrentUser currentUser;
    protected final Receiver<TaxonomyProxy> receiver;
    protected TaxonomyStatsProxy stats = null;

    @Inject
    public TaxonomyDetailPresenter(final EventBus eventBus, final MyView view,
                                   final MyProxy proxy, final PlaceManager placeManager,
                                   final TaxonomyManager taxonomyManager, final CurrentUser currentUser) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.taxonomyManager = taxonomyManager;
        this.currentUser = currentUser;
        receiver = new Receiver<TaxonomyProxy>() {
            public void onSuccess(TaxonomyProxy response) {
                taxonomy = response;
                fireEvent(new LoadTaxonomyEvent(taxonomy));
                getView().setState(State.DISPLAYING, getPermission());
                getView().getDisplayDriver().display(taxonomy);
            }


            public void onFailure(ServerFailure error) {
                fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                getView().getEditDriver().setConstraintViolations(violations);
                getView().setState(State.EDITING, getPermission());
            }
        };

    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, GermplasmPresenter.TYPE_SetMainContent, this);
    }

    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireLoadEvent = false;
            fireEvent(new LoadTaxonomyEvent(taxonomy));
        }
        fireEvent(new LoadingIndicatorEvent(false));
        getView().getDisplayDriver().display(taxonomy);
        getView().setState(State.DISPLAYING, getPermission());
        if (stats == null) {
            taxonomyManager.findStats(new Receiver<TaxonomyStatsProxy>() {

                @Override
                public void onSuccess(TaxonomyStatsProxy response) {
                    stats = response;
                    displayStats();
                }

            }, taxonomy.getId());
        } else {
            displayStats();
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        Receiver<TaxonomyProxy> receiver = new Receiver<TaxonomyProxy>() {
            @Override
            public void onSuccess(TaxonomyProxy tax) {
                taxonomy = tax;
                fireLoadEvent = true;
                getProxy().manualReveal(TaxonomyDetailPresenter.this);
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                getProxy().manualRevealFailed();
                placeManager.revealPlace(new PlaceRequest(NameTokens.taxonomies));
            }
        };
        try {
            Long taxonomyId = Long.valueOf(placeRequest.getParameter("id",
                    null));
            if (taxonomy == null || !taxonomy.getId().equals(taxonomyId)) {
                stats = null;
                taxonomyManager.findOne(receiver, taxonomyId);
            } else {
                getProxy().manualReveal(TaxonomyDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest(NameTokens.taxonomies));
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }


    @Override
    public void onEdit() {
        getView().setState(State.EDITING, getPermission());
        TaxonomyRequest ctx = taxonomyManager.getContext();
        getView().getEditDriver().edit(taxonomy, ctx);
        ctx.save(taxonomy).to(receiver);
    }

    @Override
    public void onSave() {
        getView().setState(State.SAVING, getPermission());
        RequestContext req = getView().getEditDriver().flush();
        req.fire();
    }

    @Override
    public void onCancel() {
        getView().setState(State.DISPLAYING, getPermission());
        getView().getDisplayDriver().display(taxonomy);
    }

    private int getPermission() {
        int permission = 0;
        if (currentUser.isAdmin()) {
            permission = AccessControlEntryProxy.EDIT;
        }
        return permission;
    }

    private void displayStats() {
        if (stats == null)
            return;
        getView().setGeoChartData(DataTableUtils.createChartDataTableFromString(stats.getGeoChartData()));
        getView().setAlleleAssayData(DataTableUtils.createChartDataTableFromString(stats.getAlleleAssayData()));
        getView().setSampStatData(DataTableUtils.createChartDataTableFromString(stats.getSampStatData()));
        getView().setStockGenerationData(DataTableUtils.createChartDataTableFromString(stats.getStockGenerationData()));
        getView().scheduleLayout();
    }
}
