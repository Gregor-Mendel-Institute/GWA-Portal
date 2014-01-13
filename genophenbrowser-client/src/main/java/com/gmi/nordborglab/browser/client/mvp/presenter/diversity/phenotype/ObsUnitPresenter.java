package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ObsUnitManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.ObsUnitUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.ObsUnitView.ObsUnitDisplayDriver;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.core.client.Callback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class ObsUnitPresenter extends
        Presenter<ObsUnitPresenter.MyView, ObsUnitPresenter.MyProxy> implements ObsUnitUiHandlers {

    public interface MyView extends View, HasUiHandlers<ObsUnitUiHandlers> {

        HasData<ObsUnitProxy> getDisplay();

        void setSelected(ObsUnitProxy obsUnit);

        ObsUnitDisplayDriver getDisplayDriver();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.obsunit)
    @TabInfo(label = "Plants", priority = 1, container = PhenotypeDetailTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<ObsUnitPresenter> {
    }

    protected PhenotypeProxy phenotype;
    protected Long phenotypeId;
    protected boolean obsUnitsLoaded = false;
    protected final PlaceManager placeManager;
    protected final PhenotypeManager phenotypeManager;
    protected final ObsUnitManager obsUnitManager;
    protected boolean fireLoadEvent = false;
    protected final AsyncDataProvider<ObsUnitProxy> dataProvider;


    @Inject
    public ObsUnitPresenter(final EventBus eventBus, final MyView view,
                            final MyProxy proxy, final PlaceManager placeManager,
                            final PhenotypeManager phenotypeManager, final ObsUnitManager obsUnitManager) {
        super(eventBus, view, proxy, PhenotypeDetailTabPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.phenotypeManager = phenotypeManager;
        this.obsUnitManager = obsUnitManager;
        dataProvider = new AsyncDataProvider<ObsUnitProxy>() {

            @Override
            protected void onRangeChanged(HasData<ObsUnitProxy> display) {
                requestObsUnits(null, display.getVisibleRange());
            }
        };
    }


    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getDisplay());

    }

    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireEvent(new LoadPhenotypeEvent(phenotype));
            fireLoadEvent = false;
        }
        LoadingIndicatorEvent.fire(this, false);
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        try {
            final Long phenotypeIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
            if (!phenotypeIdToLoad.equals(phenotypeId)) {
                phenotypeId = phenotypeIdToLoad;
                obsUnitsLoaded = false;
            }
            if (obsUnitsLoaded) {
                getProxy().manualReveal(ObsUnitPresenter.this);
                return;
            }
            if (phenotype == null || !phenotype.getId().equals(phenotypeIdToLoad)) {
                phenotypeManager.getContext().findPhenotype(phenotypeIdToLoad).fire(new Receiver<PhenotypeProxy>() {

                    @Override
                    public void onSuccess(PhenotypeProxy response) {
                        phenotype = response;
                        fireLoadEvent = true;
                    }
                });
            }
            requestObsUnits(new Callback<Void, Void>() {

                @Override
                public void onFailure(Void reason) {
                    getProxy().manualRevealFailed();
                    placeManager.revealPlace(new PlaceRequest.Builder()
                            .nameToken(NameTokens.phenotype)
                            .with("id", phenotypeIdToLoad.toString()).build());
                }

                @Override
                public void onSuccess(Void result) {
                    getProxy().manualReveal(ObsUnitPresenter.this);
                }
            }, getView().getDisplay().getVisibleRange());
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    protected void requestObsUnits(final Callback<Void, Void> callback, final Range range) {
        if (phenotypeId == null)
            return;
        LoadingIndicatorEvent.fire(this, true);
        Receiver<ObsUnitPageProxy> receiver = new Receiver<ObsUnitPageProxy>() {
            @Override
            public void onSuccess(ObsUnitPageProxy obsUnits) {
                dataProvider.updateRowCount(
                        (int) obsUnits.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), obsUnits.getContents());
                obsUnitsLoaded = true;
                if (callback != null)
                    callback.onSuccess(null);
                LoadingIndicatorEvent.fire(ObsUnitPresenter.this, false);
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new DisplayNotificationEvent("Error", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                if (callback != null) {
                    callback.onFailure(null);
                }
                LoadingIndicatorEvent.fire(ObsUnitPresenter.this, false);
            }

        };
        obsUnitManager.findObsUnitsByPhenotypeId(receiver, phenotypeId, range.getStart(), range.getLength());
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @ProxyEvent
    void onLoad(LoadPhenotypeEvent event) {
        phenotype = event.getPhenotype();
        if (!phenotype.getId().equals(phenotypeId))
            obsUnitsLoaded = false;
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", phenotype.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic("Plants (" + phenotype.getNumberOfObsUnits() + ")", tabData.getPriority(), historyToken));
    }

    @Override
    public void onShowObsUnit(ObsUnitProxy obsUnit) {
        if (obsUnit != null) {
            PlaceRequest request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest()).with("obsUnitId", obsUnit.getId().toString()).build();
            placeManager.updateHistory(request, true);
        }
        getView().getDisplayDriver().display(obsUnit);
    }


}
