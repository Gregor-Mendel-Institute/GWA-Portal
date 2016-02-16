package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail.PhenotypeDetailView.PhenotypeDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail.PhenotypeDetailView.PhenotypeEditDriver;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.Statistics;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.gmi.nordborglab.browser.shared.util.PhenotypeHistogram;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class PhenotypeDetailPresenter
        extends
        Presenter<PhenotypeDetailPresenter.MyView, PhenotypeDetailPresenter.MyProxy> implements PhenotypeDetailUiHandlers {

    public interface MyView extends com.gwtplatform.mvp.client.View, com.gwtplatform.mvp.client.HasUiHandlers<PhenotypeDetailUiHandlers> {

        PhenotypeDisplayDriver getDisplayDriver();


        PhenotypeEditDriver getEditDriver();

        void setPhenotypeId(Long id);

        void setAcceptableValuesForUnitOfMeasure(
                Collection<UnitOfMeasureProxy> values);

        void setGeoChartData(Multiset<String> geochartData);

        void setHistogramChartData(
                ImmutableSortedMap<Double, Integer> histogramData);

        void scheduledLayout();

        void setPhenotypExplorerData(ImmutableList<TraitStatsProxy> traits);

        void drawCharts();

        void showEditPopup(boolean show);

        void showActionBtns(boolean show);

        void showDeletePopup();

        void setStatisticTypes(List<StatisticTypeProxy> statisticTypes);

        void setAvailableStatisticTypes(List<StatisticTypeProxy> statisticTypes);
    }


    protected PhenotypeProxy phenotype;
    protected PhenotypeProxy editedPhenotype;
    protected boolean fireLoadEvent;
    protected final PlaceManager placeManager;
    protected final OntologyManager ontologyManager;
    protected final PhenotypeManager phenotypeManager;
    protected final CurrentUser currentUser;
    protected final Receiver<PhenotypeProxy> receiver;
    protected boolean isRefresh = false;
    private ImmutableSortedMap<Double, Integer> histogramData;
    private List<StatisticTypeProxy> statisticTypes;
    protected HashMap<StatisticTypeProxy, List<TraitStatsProxy>> cache = new HashMap<>();
    private final Validator validator;

    private Multiset<String> geochartData;
    private static int BIN_COUNT = 20;

    @ProxyCodeSplit
    @NameToken(NameTokens.phenotype)
    @TabInfo(label = "Overview", priority = 0, container = PhenotypeDetailTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<PhenotypeDetailPresenter> {
    }

    @Inject
    public PhenotypeDetailPresenter(final EventBus eventBus, final MyView view,
                                    final MyProxy proxy, final PlaceManager placeManager,
                                    final PhenotypeManager phenotypeManager,
                                    final CurrentUser currentUser, final OntologyManager ontologyManager, Validator validator) {
        super(eventBus, view, proxy, PhenotypeDetailTabPresenter.SLOT_CONTENT);
        this.validator = validator;
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.ontologyManager = ontologyManager;
        this.phenotypeManager = phenotypeManager;
        this.currentUser = currentUser;
        getView().setAcceptableValuesForUnitOfMeasure(currentUser.getAppData().getUnitOfMeasureList());
        getView().setAvailableStatisticTypes(currentUser.getAppData().getStatisticTypeList());
        receiver = new Receiver<PhenotypeProxy>() {
            public void onSuccess(PhenotypeProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                phenotype = response;
                fireEvent(new LoadPhenotypeEvent(phenotype));
                getView().showEditPopup(false);
                getView().getDisplayDriver().display(phenotype);
            }


            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                DisplayNotificationEvent.fireError(getEventBus(), "Error while saving", error.getMessage());
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onConstraintViolation(violations);
            }
        };
    }


    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
    protected void onReset() {
        super.onReset();
        LoadingIndicatorEvent.fire(this, false);
        if (fireLoadEvent) {
            fireLoadEvent = false;
            fireEvent(new LoadPhenotypeEvent(phenotype));
        }
        getView().getDisplayDriver().display(phenotype);
        getView().showActionBtns(currentUser.hasEdit(phenotype));
        getProxy().getTab().setTargetHistoryToken(placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest()));
        if (isRefresh) {
            getView().setStatisticTypes(statisticTypes);
        }
        getView().setPhenotypeId(phenotype.getId());
        getView().scheduledLayout();
    }

    private void resetCharts() {
        getView().setStatisticTypes(null);
        getView().setGeoChartData(null);
        getView().setHistogramChartData(null);
        getView().setPhenotypExplorerData(null);
    }


    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        Receiver<PhenotypeProxy> receiver = new Receiver<PhenotypeProxy>() {
            @Override
            public void onSuccess(PhenotypeProxy phen) {
                phenotype = phen;
                statisticTypes = phen.getStatisticTypes();
                fireLoadEvent = true;
                getProxy().manualReveal(PhenotypeDetailPresenter.this);
            }

            @Override
            public void onFailure(ServerFailure error) {
                statisticTypes = null;
                fireEvent(new LoadingIndicatorEvent(false));
                getProxy().manualRevealFailed();
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
            }
        };
        try {
            Long phenotypeId = Long.valueOf(placeRequest.getParameter("id",
                    null));
            if (phenotype == null || !phenotype.getId().equals(phenotypeId)) {
                isRefresh = true;
                statisticTypes = null;
                cache.clear();
                phenotypeManager.findOne(receiver, phenotypeId);
                resetCharts();
            } else {
                isRefresh = false;
                getProxy().manualReveal(PhenotypeDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }


    @Override
    public void onEdit() {
        PhenotypeRequest ctx = phenotypeManager.getContext();
        editedPhenotype = ctx.edit(phenotype);
        getView().getEditDriver().edit(editedPhenotype, ctx);
        ///TODO Fix this better.
        List<String> paths = ImmutableList.<String>builder().addAll(Arrays.asList(getView().getEditDriver().getPaths())).add("userPermission").add("statisticTypes").build();
        ctx.save(editedPhenotype).with(paths.toArray(new String[0])).to(receiver);
        getView().showEditPopup(true);
    }

    @Override
    public void onSave() {
        RequestContext req = getView().getEditDriver().flush();
        if (!checkValidation())
            return;
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        req.fire();
    }

    private boolean checkValidation() {
        boolean isOk;
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(editedPhenotype, Default.class);
        if (!violations.isEmpty() || getView().getEditDriver().hasErrors()) {
            isOk = false;
        } else {
            isOk = true;
        }
        getView().getEditDriver().setConstraintViolations(violations);
        return isOk;
    }

    @Override
    public void onCancel() {
        getView().showEditPopup(false);
    }

    @Override
    public void onDelete() {
        getView().showDeletePopup();

    }

    @Override
    public void onConfirmDelete() {
        fireEvent(new LoadingIndicatorEvent(true, "Removing..."));
        phenotypeManager.delete(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                fireEvent(new LoadingIndicatorEvent(false));
                PlaceRequest request = null;
                if (placeManager.getHierarchyDepth() <= 1) {
                    request = new PlaceRequest.Builder().nameToken(NameTokens.phenotypeoverview).build();
                } else {
                    request = placeManager.getCurrentPlaceHierarchy().get(placeManager.getHierarchyDepth() - 2);
                }
                phenotype = null;
                placeManager.revealPlace(request);
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
            }
        }, phenotype);
    }

    @Override
    public void onSelectStatisticType(final StatisticTypeProxy type) {
        if (type == null) {
            getView().setGeoChartData(null);
            getView().setHistogramChartData(null);
            getView().setPhenotypExplorerData(null);
            getView().drawCharts();
        } else {
            List<TraitStatsProxy> cachedTraits = cache.get(type);
            if (cachedTraits != null) {
                calculateChartDataAndDisplay(cachedTraits);
            } else {
                fireEvent(new LoadingIndicatorEvent(true));
                phenotypeManager.findTraitStatsByStatisticType(phenotype.getId(), type.getId(), new Receiver<List<TraitStatsProxy>>() {

                    @Override
                    public void onSuccess(List<TraitStatsProxy> response) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        cache.put(type, response);
                        calculateChartDataAndDisplay(response);
                    }
                });
            }
        }
    }


    @Override
    public boolean useManualReveal() {
        return true;
    }

    @ProxyEvent
    public void onLoadPhenotype(LoadPhenotypeEvent event) {
        if (phenotype != event.getPhenotype()) {
            cache.clear();
            resetCharts();
            phenotype = event.getPhenotype();
            statisticTypes = event.getPhenotype().getStatisticTypes();
            getView().setStatisticTypes(statisticTypes);
            isRefresh = true;
        }
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", phenotype.getId().toString())
                .build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic(tabData.getLabel(), tabData.getPriority(), historyToken));
    }

    private int getPermission() {
        int permission = 0;
        if (phenotype != null)
            permission = currentUser.getPermissionMask(phenotype.getUserPermission());
        return permission;
    }

    private void calculateHistogramData(List<TraitStatsProxy> traits) {
        histogramData = PhenotypeHistogram.getHistogram(Lists.transform(traits, Statistics.statsToValue), BIN_COUNT);
    }

    private void calculateGeoChartData(List<TraitStatsProxy> traits) {
        geochartData = Statistics.getGeoChartData(traits);
    }

    private void calculateChartDataAndDisplay(List<TraitStatsProxy> traits) {
        calculateGeoChartData(traits);
        calculateHistogramData(traits);
        getView().setGeoChartData(geochartData);
        getView().setHistogramChartData(histogramData);
        getView().setPhenotypExplorerData(ImmutableList.copyOf(traits));
        getView().drawCharts();
    }

}
