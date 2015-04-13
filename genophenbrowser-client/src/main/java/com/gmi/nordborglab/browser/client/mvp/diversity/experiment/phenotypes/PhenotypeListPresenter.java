package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PhenotypeUploadedEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.ExperimentDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.core.client.Callback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class PhenotypeListPresenter
        extends
        Presenter<PhenotypeListPresenter.MyView, PhenotypeListPresenter.MyProxy> implements PhenotypeListUiHandlers {

    public interface MyView extends View, HasUiHandlers<PhenotypeListUiHandlers> {

        HasData<PhenotypeProxy> getDisplay();

        void onShowPhenotypeUploadPanel(boolean isShow);

        void showUploadBtn(boolean showAdd);

    }

    @ProxyCodeSplit
    @TabInfo(container = ExperimentDetailTabPresenter.class, label = "Phenotypes", priority = 1)
    @NameToken(NameTokens.phenotypes)
    public interface MyProxy extends
            TabContentProxyPlace<PhenotypeListPresenter> {

    }

    protected final AsyncDataProvider<PhenotypeProxy> dataProvider;

    public static final Object TYPE_SetPhenotypeUploadContent = new Object();
    private final PhenotypeManager phenotypeManager;

    private ExperimentProxy experiment;
    private final PlaceManager placeManager;
    boolean phenotypesLoaded = false;
    protected Long experimentId = null;
    boolean fireLoadExperimentEvent = false;
    private final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizardPresenterWidget;
    private final ExperimentManager experimentManager;
    private final CurrentUser currentUser;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    @Inject
    public PhenotypeListPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy, final PhenotypeManager phenotypeManager,
                                  final PlaceManager placeManager,
                                  final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizardPresenterWidget,
                                  final CurrentUser currentUser,
                                  final FacetSearchPresenterWidget facetSearchPresenterWidget, final ExperimentManager experimentManager) {
        super(eventBus, view, proxy, ExperimentDetailTabPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.phenotypeUploadWizardPresenterWidget = phenotypeUploadWizardPresenterWidget;
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        facetSearchPresenterWidget.setDefaultFilter(ConstEnums.TABLE_FILTER.ALL.name());
        this.currentUser = currentUser;
        this.experimentManager = experimentManager;
        this.phenotypeManager = phenotypeManager;
        this.placeManager = placeManager;
        dataProvider = new AsyncDataProvider<PhenotypeProxy>() {

            @Override
            protected void onRangeChanged(HasData<PhenotypeProxy> display) {
                requestPhenotypes(null, display.getVisibleRange());

            }
        };
    }


    protected void requestPhenotypes(final Callback<Void, Void> callback, final Range range) {
        if (experimentId == null)
            return;


        Receiver<PhenotypePageProxy> receiver = new Receiver<PhenotypePageProxy>() {
            @Override
            public void onSuccess(PhenotypePageProxy phenotypes) {
                dataProvider.updateRowCount(
                        (int) phenotypes.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), phenotypes.getContents());
                phenotypesLoaded = true;
                facetSearchPresenterWidget.displayFacets(phenotypes.getFacets());
                if (callback != null)
                    callback.onSuccess(null);
            }

            public void onFailure(ServerFailure error) {
                DisplayNotificationEvent.fireError(getEventBus(), "Error", error.getMessage());
                if (callback != null) {
                    callback.onFailure(null);
                }
            }

        };
        phenotypeManager.findAll(receiver, experimentId, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength());
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetPhenotypeUploadContent, phenotypeUploadWizardPresenterWidget);
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
        registerHandler(PhenotypeUploadedEvent.register(getEventBus(), new PhenotypeUploadedEvent.Handler() {
            @Override
            public void onPhenotypeUploaded(PhenotypeUploadedEvent event) {
                getView().onShowPhenotypeUploadPanel(false);
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }));
        registerHandler(getEventBus().addHandlerToSource(FacetSearchChangeEvent.TYPE, facetSearchPresenterWidget, new FacetSearchChangeEvent.Handler() {

            @Override
            public void onChanged(FacetSearchChangeEvent event) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }));
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    @Override
    protected void onReset() {
        super.onReset();
        phenotypeUploadWizardPresenterWidget.setExperiment(experiment);
        if (fireLoadExperimentEvent) {
            fireEvent(new LoadExperimentEvent(experiment));
            fireLoadExperimentEvent = false;
        }
        LoadingIndicatorEvent.fire(this, false);
        checkPermissionAndUpdateView();
        //getProxy().getTab().setTargetHistoryToken(placeManager.buildRelativeHistoryToken(0));
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        try {
            final Long experimentIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
            if (!experimentIdToLoad.equals(experimentId)) {
                experimentId = experimentIdToLoad;
                phenotypesLoaded = false;
            }
            if (phenotypesLoaded) {
                getProxy().manualReveal(PhenotypeListPresenter.this);
                return;
            }
            if (experiment == null || !experiment.getId().equals(experimentIdToLoad)) {
                experimentManager.findOne(new Receiver<ExperimentProxy>() {

                    @Override
                    public void onFailure(ServerFailure error) {
                        getProxy().manualRevealFailed();
                        placeManager.revealPlace(new PlaceRequest.Builder()
                                .nameToken(NameTokens.experiments).build());
                    }

                    @Override
                    public void onSuccess(ExperimentProxy response) {
                        experiment = response;
                        fireLoadExperimentEvent = true;
                    }
                }, experimentId);
            }
            requestPhenotypes(new Callback<Void, Void>() {

                @Override
                public void onFailure(Void reason) {
                    getProxy().manualRevealFailed();
                    placeManager.revealPlace(new PlaceRequest.Builder()
                            .nameToken(NameTokens.experiment)
                            .with("id", experimentIdToLoad.toString()).build());
                }

                @Override
                public void onSuccess(Void result) {
                    getProxy().manualReveal(PhenotypeListPresenter.this);
                }
            }, getView().getDisplay().getVisibleRange());
        } catch (Exception e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @ProxyEvent
    public void onLoadExperiment(LoadExperimentEvent event) {
        experiment = event.getExperiment();
        if (!experiment.getId().equals(experimentId))
            phenotypesLoaded = false;
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", experiment.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic("Phenotypes (" + experiment.getNumberOfPhenotypes() + ")", tabData.getPriority(), historyToken));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onClosePhenotypeUploadPopup() {

    }

    protected void checkPermissionAndUpdateView() {
        int permission = 0;
        if (experiment != null)
            permission = currentUser.getPermissionMask(experiment.getUserPermission());
        boolean showAdd = (((permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT) ||
                ((permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION));
        getView().showUploadBtn(showAdd);
    }
}
