package com.gmi.nordborglab.browser.client.mvp.diversity.tools.gwasviewer;

import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.GWASResultLoadedEvent;
import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.client.manager.GWASDataManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASPlotPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.permissions.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gmi.nordborglab.browser.shared.service.GWASDataRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASViewerPresenter extends Presenter<GWASViewerPresenter.MyView, GWASViewerPresenter.MyProxy>
        implements GWASViewerUiHandlers {

    public interface MyView extends View, HasUiHandlers<GWASViewerUiHandlers> {

        void showPanel(GWASViewerView.PANELS panel);

        HasData<GWASResultProxy> getDisplay();

        void hideUploadPanel(boolean hide);

        GWASViewerView.GWASResultEditDriver getEditDriver();

        void showEditPanel(boolean show);

        void showPermissionPanel(boolean show);

        void showDeletePopup(GWASResultProxy object);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.gwasViewer)
    public interface MyProxy extends ProxyPlace<GWASViewerPresenter> {

    }


    static final SingleSlot<GWASUploadWizardPresenterWidget> SLOT_GWAS_UPLOAD = new SingleSlot();
    static final SingleSlot<GWASPlotPresenterWidget> SLOT_GWAS_PLOT = new SingleSlot<>();
    static final SingleSlot<PermissionDetailPresenter> SLOT_PERMISSIONS = new SingleSlot<>();


    private final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget;
    private final AsyncDataProvider<GWASResultProxy> dataProvider = new AsyncDataProvider<GWASResultProxy>() {
        @Override
        protected void onRangeChanged(HasData<GWASResultProxy> display) {
            requestGWASResults(display.getVisibleRange());
        }
    };
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;
    private final GWASDataManager gwasDataManager;
    private GWASResultProxy gwasResult = null;
    private final GWASPlotPresenterWidget gwasPlotPresenterWidget;
    private boolean isFireEvent = false;
    private Receiver<GWASResultProxy> receiverOfSave = null;
    private GWASDataRequest ctx;
    private final PermissionDetailPresenter permissionDetailPresenter;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;


    @Inject
    public GWASViewerPresenter(EventBus eventBus, MyView view, GWASViewerPresenter.MyProxy proxy,
                               final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget,
                               final CurrentUser currentUser,
                               final PlaceManager placeManager,
                               final GWASDataManager gwasDataManager,
                               final GWASPlotPresenterWidget gwasPlotPresenterWidget,
                               final PermissionDetailPresenter permissionDetailPresenter,
                               final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, DiversityPresenter.SLOT_CONTENT);
        this.gwasUploadWizardPresenterWidget = gwasUploadWizardPresenterWidget;
        this.gwasPlotPresenterWidget = gwasPlotPresenterWidget;
        this.permissionDetailPresenter = permissionDetailPresenter;
        this.placeManager = placeManager;
        this.gwasDataManager = gwasDataManager;
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        facetSearchPresenterWidget.setDefaultFilter(ConstEnums.TABLE_FILTER.ALL.name());
        facetSearchPresenterWidget.initFixedFacets(FacetSearchPresenterWidget.SHARED_MAP);
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
        dataProvider.addDataDisplay(getView().getDisplay());
        receiverOfSave = new Receiver<GWASResultProxy>() {
            public void onSuccess(GWASResultProxy response) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
                getView().showEditPanel(false);
            }

            public void onFailure(ServerFailure error) {
                DisplayNotificationEvent.fireError(getEventBus(), "Error while saving", error.getMessage());
                getView().showEditPanel(false);
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                super.onConstraintViolation(violations);
                getView().showEditPanel(true);
            }
        };
    }


    @Override
    public void onBind() {
        super.onBind();
        setInSlot(SLOT_GWAS_UPLOAD, gwasUploadWizardPresenterWidget);
        setInSlot(SLOT_GWAS_PLOT, gwasPlotPresenterWidget);
        setInSlot(SLOT_PERMISSIONS, permissionDetailPresenter);
        setInSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetSearchPresenterWidget);
        registerHandler(GWASUploadedEvent.register(getEventBus(), new GWASUploadedEvent.Handler() {
            @Override
            public void onGWASUploaded(GWASUploadedEvent event) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
                ;
                getView().showPanel(GWASViewerView.PANELS.LIST);
            }
        }));
        registerHandler(getEventBus().addHandlerToSource(PermissionDoneEvent.TYPE, permissionDetailPresenter, new PermissionDoneEvent.Handler() {
            @Override
            public void onPermissionDone(PermissionDoneEvent event) {
                getView().showPermissionPanel(false);
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
    public void onReset() {
        super.onReset();
        this.gwasUploadWizardPresenterWidget.setRestURL("provider/gwas/upload");
        this.gwasUploadWizardPresenterWidget.setMultipleUpload(true);
        if (isFireEvent)
            GWASResultLoadedEvent.fire(getEventBus(), gwasResult);
        isFireEvent = false;
        getView().hideUploadPanel(!currentUser.isLoggedIn());

        if (gwasResult != null) {
            getView().showPanel(GWASViewerView.PANELS.PLOTS);
            gwasPlotPresenterWidget.loadPlots(gwasResult.getId(), GetGWASDataAction.TYPE.GWASVIEWER);
        } else {
            getView().showPanel(GWASViewerView.PANELS.LIST);
        }
    }


    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        Long gwasResultId = null;
        try {
            gwasResultId = Long.parseLong(request.getParameter("id", null));
        } catch (Exception e) {
        }
        if (gwasResult != null && gwasResult.getId().equals(gwasResultId)) {
            getProxy().manualReveal(this);
        } else if (!currentUser.isLoggedIn() && gwasResultId == null) {
            getProxy().manualRevealFailed();
            Window.Location.assign(GWT.getHostPageBaseURL() + "login?url=" + placeManager.buildHistoryToken(request));
            return;
        } else if (gwasResultId != null) {
            gwasDataManager.findOneGWASResults(new Receiver<GWASResultProxy>() {

                @Override
                public void onSuccess(GWASResultProxy response) {
                    gwasResult = response;
                    isFireEvent = true;
                    getProxy().manualReveal(GWASViewerPresenter.this);
                }

                @Override
                public void onFailure(ServerFailure error) {
                    if (currentUser.isLoggedIn()) {
                        getProxy().manualReveal(GWASViewerPresenter.this);
                    } else {
                        getProxy().manualRevealFailed();
                        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
                    }

                }
            }, gwasResultId);
        } else {
            gwasResult = null;
            //getView().getFlatDisplay().setVisibleRangeAndClearData(getView().getFlatDisplay().getVisibleRange(),true);
            getProxy().manualReveal(GWASViewerPresenter.this);
        }
    }

    @Override
    public void onShowPermissions(GWASResultProxy object) {
        getView().showPermissionPanel(true);
        permissionDetailPresenter.setDomainObject(object, placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(placeManager.getCurrentPlaceRequest().getNameToken()).with("id", object.getId().toString()).build()));
    }

    @Override
    public void onEdit(GWASResultProxy object) {
        getView().showEditPanel(true);
        ctx = gwasDataManager.getContext();
        getView().getEditDriver().edit(object, ctx);
        ctx.save(object).with("appUser").to(receiverOfSave);
    }

    @Override
    public void onDelete(GWASResultProxy object) {
        getView().showDeletePopup(object);
    }

    @Override
    public void onConfirmDelete(GWASResultProxy object) {
        gwasDataManager.delete(new Receiver<List<GWASResultProxy>>() {
            @Override
            public void onSuccess(List<GWASResultProxy> response) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }, object);
    }

    @Override
    public void cancelEdits() {
        ctx = null;
        getView().showEditPanel(false);
    }

    @Override
    public void saveEdits() {
        RequestContext req = getView().getEditDriver().flush();
        req.fire();
    }

    protected void requestGWASResults(final Range range) {
        if (!currentUser.isLoggedIn())
            return;
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<GWASResultPageProxy> receiver = new Receiver<GWASResultPageProxy>() {
            @Override
            public void onSuccess(GWASResultPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) studies.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studies.getContents());
                facetSearchPresenterWidget.displayFacets(studies.getFacets());
            }
        };
        gwasDataManager.findAllGWASResults(receiver, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength());
    }
}
