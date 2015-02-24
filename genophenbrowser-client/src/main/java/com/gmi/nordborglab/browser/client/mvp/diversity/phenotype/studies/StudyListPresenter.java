package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.StudyModifiedEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
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

import java.util.Arrays;

public class StudyListPresenter extends
        Presenter<StudyListPresenter.MyView, StudyListPresenter.MyProxy> implements StudyListUiHandlers {

    public interface MyView extends View, HasUiHandlers<StudyListUiHandlers> {

        HasData<StudyProxy> getDisplay();

        void showAddBtn(boolean showAdd);

    }

    protected PhenotypeProxy phenotype;

    protected Long phenotypeId;
    protected boolean studiesLoaded = false;
    protected final PlaceManager placeManager;
    protected final PhenotypeManager phenotypeManager;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;
    protected final CdvManager cdvManager;
    protected boolean fireLoadEvent = false;
    protected final AsyncDataProvider<StudyProxy> dataProvider;
    protected final CurrentUser currentUser;

    @ProxyCodeSplit
    @NameToken(NameTokens.studylist)
    @TabInfo(label = "Analyses", priority = 2, container = PhenotypeDetailTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<StudyListPresenter> {
    }

    @Inject
    public StudyListPresenter(final EventBus eventBus, final MyView view,
                              final MyProxy proxy, final PlaceManager placeManager,
                              final PhenotypeManager phenotypeManager, final CdvManager cdvManager,
                              final CurrentUser currentUser, final
                              FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, PhenotypeDetailTabPresenter.TYPE_SetTabContent);
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        facetSearchPresenterWidget.setDefaultFilter(ConstEnums.TABLE_FILTER.ALL.name());
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.placeManager = placeManager;
        this.phenotypeManager = phenotypeManager;
        this.cdvManager = cdvManager;
        dataProvider = new AsyncDataProvider<StudyProxy>() {

            @Override
            protected void onRangeChanged(HasData<StudyProxy> display) {
                requestStudies(null, display.getVisibleRange());
            }
        };
    }


    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireEvent(new LoadPhenotypeEvent(phenotype));
            fireLoadEvent = false;
        }
        LoadingIndicatorEvent.fire(this, false);
        checkPermissionAndUpdateView();
    }

    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getDisplay());
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
        registerHandler(StudyModifiedEvent.register(getEventBus(), new StudyModifiedEvent.Handler() {
            @Override
            public void onStudyModified(StudyModifiedEvent event) {
                for (int i = 0; i < getView().getDisplay().getVisibleItemCount(); i++) {
                    if (getView().getDisplay().getVisibleItem(i).getId().equals(event.getStudy().getId())) {
                        dataProvider.updateRowData(i, Arrays.asList(event.getStudy()));
                        break;
                    }
                }
            }
        }));
        registerHandler(getEventBus().addHandlerToSource(FacetSearchChangeEvent.TYPE, facetSearchPresenterWidget, new FacetSearchChangeEvent.Handler() {

            @Override
            public void onChanged(FacetSearchChangeEvent event) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }));
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        try {
            final Long phenotypeIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
            if (!phenotypeIdToLoad.equals(phenotypeId)) {
                phenotypeId = phenotypeIdToLoad;
                studiesLoaded = false;
            }
            if (studiesLoaded) {
                getProxy().manualReveal(StudyListPresenter.this);
                return;
            }
            if (phenotype == null || !phenotype.getId().equals(phenotypeIdToLoad)) {
                phenotypeManager.findOne(new Receiver<PhenotypeProxy>() {

                    @Override
                    public void onSuccess(PhenotypeProxy response) {
                        phenotype = response;
                        fireLoadEvent = true;
                    }
                }, phenotypeIdToLoad);
            }
            requestStudies(new Callback<Void, Void>() {

                @Override
                public void onFailure(Void reason) {
                    getProxy().manualRevealFailed();
                    placeManager.revealPlace(new PlaceRequest.Builder()
                            .nameToken(NameTokens.phenotype)
                            .with("id", phenotypeIdToLoad.toString())
                            .build());
                }

                @Override
                public void onSuccess(Void result) {
                    getProxy().manualReveal(StudyListPresenter.this);
                }
            }, getView().getDisplay().getVisibleRange());
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @ProxyEvent
    void onLoadPhenotype(LoadPhenotypeEvent event) {
        phenotype = event.getPhenotype();
        if (!phenotype.getId().equals(phenotypeId))
            studiesLoaded = false;
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", phenotype.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic("Analyses (" + phenotype.getNumberOfStudies() + ")", tabData.getPriority(), historyToken));
    }

    protected void requestStudies(final Callback<Void, Void> callback, final Range range) {
        if (phenotypeId == null)
            return;
        Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
            @Override
            public void onSuccess(StudyPageProxy studyPage) {
                dataProvider.updateRowCount(
                        (int) studyPage.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studyPage.getContents());
                studiesLoaded = true;
                facetSearchPresenterWidget.displayFacets(studyPage.getFacets());
                if (callback != null)
                    callback.onSuccess(null);
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new DisplayNotificationEvent("Error", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                if (callback != null) {
                    callback.onFailure(null);
                }
            }

        };
        cdvManager.findStudiesByPhenotypeId(receiver, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), phenotypeId, range.getStart(), range.getLength());
    }

    @Override
    public void onNewStudy() {
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(NameTokens.basicstudywizard)
                .with("phenotype", phenotypeId.toString()).build();
        placeManager.revealPlace(request);
    }

    protected void checkPermissionAndUpdateView() {
        int permission = currentUser.getPermissionMask(phenotype.getUserPermission());
        boolean showAdd = (((permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT) ||
                ((permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION));
        getView().showAddBtn(showAdd);
    }
}
