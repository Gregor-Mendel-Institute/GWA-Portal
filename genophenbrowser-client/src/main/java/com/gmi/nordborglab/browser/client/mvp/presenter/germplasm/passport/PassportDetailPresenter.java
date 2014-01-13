package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport;

import java.util.List;

import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PassportManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDetailView.PassportDisplayDriver;
import com.gmi.nordborglab.browser.client.util.CustomDataTable;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class PassportDetailPresenter
        extends
        Presenter<PassportDetailPresenter.MyView, PassportDetailPresenter.MyProxy> {

    public interface MyView extends View {

        PassportDisplayDriver getDisplayDriver();

        void setState(State displaying, int permission);

        void initMap();

        void showPassportOnMap(PassportProxy passport);

        HasData<StockProxy> getStockDataDisplay();

        HasData<StudyProxy> getStudyDataDisplay();

        HasData<PhenotypeProxy> getPhenotypeDataDisplay();

        HasText getStockStatsLabel();

        HasText getPhenotypeStatsLabel();

        HasText getStudyStatsLabel();

        HasText getGenotypeStatsLabel();

        void setStatsDataTable(CustomDataTable createDataTableFromString);

        void scheduleLayout();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.passport)
    public interface MyProxy extends ProxyPlace<PassportDetailPresenter> {
    }

    private final PlaceManager placeManager;
    private final CurrentUser currentUser;
    protected PassportProxy passport;
    private final PassportManager passportManager;
    protected PassportStatsProxy stats = null;
    private final ListDataProvider<StockProxy> stockDataProvider = new ListDataProvider<StockProxy>(new EntityProxyKeyProvider<StockProxy>());
    private final ListDataProvider<PhenotypeProxy> phenotypeDataProvider = new ListDataProvider<PhenotypeProxy>(new EntityProxyKeyProvider<PhenotypeProxy>());
    private final ListDataProvider<StudyProxy> studyDataProvider = new ListDataProvider<StudyProxy>(new EntityProxyKeyProvider<StudyProxy>());

    @Inject
    public PassportDetailPresenter(final EventBus eventBus, final MyView view,
                                   final MyProxy proxy, final PlaceManager placeManager,
                                   final CurrentUser currentUser, final PassportManager passportManager
    ) {
        super(eventBus, view, proxy, GermplasmPresenter.TYPE_SetMainContent);
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.passportManager = passportManager;
    }


    @Override
    protected void onBind() {
        super.onBind();
        stockDataProvider.addDataDisplay(getView().getStockDataDisplay());
        phenotypeDataProvider.addDataDisplay(getView().getPhenotypeDataDisplay());
        studyDataProvider.addDataDisplay(getView().getStudyDataDisplay());
    }

    @Override
    protected void onReset() {
        super.onReset();
        fireEvent(new LoadingIndicatorEvent(false));
        getView().initMap();
        getView().showPassportOnMap(passport);
        getView().getDisplayDriver().display(passport);
        getView().setState(State.DISPLAYING, getPermission());
        getView().scheduleLayout();
    }


    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        Receiver<PassportProxy> receiver = new Receiver<PassportProxy>() {
            @Override
            public void onSuccess(PassportProxy pass) {
                passport = pass;
                //fireLoadEvent = true;
                getProxy().manualReveal(PassportDetailPresenter.this);
                getView().getGenotypeStatsLabel().setText(Integer.toString(pass.getAlleleAssays().size()));
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                getProxy().manualRevealFailed();
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.taxonomies).build());
            }
        };
        try {
            Long passportId = Long.valueOf(placeRequest.getParameter("id",
                    null));
            if (passport == null || !passport.getId().equals(passportId)) {
                resetAndLoadData(passportId);
                passportManager.findOne(receiver, passportId);
            } else {
                getProxy().manualReveal(PassportDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.taxonomies).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    private int getPermission() {
        int permission = 0;
        if (currentUser.isAdmin()) {
            permission = AccessControlEntryProxy.EDIT;
        }
        return permission;
    }

    private void resetAndLoadData(Long passportId) {
        stats = null;
        phenotypeDataProvider.removeDataDisplay(getView().getPhenotypeDataDisplay());
        stockDataProvider.removeDataDisplay(getView().getStockDataDisplay());
        studyDataProvider.removeDataDisplay(getView().getStudyDataDisplay());
        phenotypeDataProvider.getList().clear();
        studyDataProvider.getList().clear();
        stockDataProvider.getList().clear();
        passportManager.findAllStocks(passportId, new Receiver<List<StockProxy>>() {
            @Override
            public void onSuccess(List<StockProxy> response) {
                getView().getStockStatsLabel().setText(Integer.toString(response.size()));
                stockDataProvider.setList(response);
                stockDataProvider.addDataDisplay(getView().getStockDataDisplay());
            }
        });

        passportManager.findAllPhenotypes(passportId, new Receiver<List<PhenotypeProxy>>() {

            @Override
            public void onSuccess(List<PhenotypeProxy> response) {
                getView().getPhenotypeStatsLabel().setText(Integer.toString(response.size()));
                phenotypeDataProvider.setList(response);
                phenotypeDataProvider.addDataDisplay(getView().getPhenotypeDataDisplay());

            }
        });

        passportManager.findAllStudies(passportId, new Receiver<List<StudyProxy>>() {

            @Override
            public void onSuccess(List<StudyProxy> response) {
                getView().getStudyStatsLabel().setText(Integer.toString(response.size()));
                studyDataProvider.setList(response);
                studyDataProvider.addDataDisplay(getView().getStudyDataDisplay());
            }
        });
        passportManager.findStats(new Receiver<PassportStatsProxy>() {

            @Override
            public void onSuccess(PassportStatsProxy response) {
                stats = response;
                getView().setStatsDataTable(DataTableUtils.createDataTableFromString(stats.getData()));
            }

        }, passportId);
    }
}
