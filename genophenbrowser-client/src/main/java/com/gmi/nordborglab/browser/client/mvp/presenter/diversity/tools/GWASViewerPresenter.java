package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.GWASResultLoadedEvent;
import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.client.manager.GWASDataManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASViewerUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASViewerView;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gmi.nordborglab.browser.shared.service.GWASDataRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

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

        void hideListPanel(boolean hide);

        void showPanel(GWASViewerView.PANELS panel);

        HasData<GWASResultProxy> getDisplay();

        void hideUploadPanel(boolean hide);

        GWASViewerView.GWASResultEditDriver getEditDriver();

        void showEditPanel(boolean show);

        void showPermissionPanel(boolean show);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.gwasViewer)
    public interface MyProxy extends ProxyPlace<GWASViewerPresenter> {

    }

    public static final Object TYPE_SetGWASUploadContent = new Object();
    public static final Object TYPE_SetGWASPLOTContent = new Object();
    public static final Object TYPE_SetPermissionContent = new Object();

    private final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget;
    private final ListDataProvider<GWASResultProxy> dataProvider = new ListDataProvider<GWASResultProxy>();
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;
    private final GWASDataManager gwasDataManager;
    private GWASResultProxy gwasResult = null;
    private List<GWASResultProxy> gwasResults;
    private final GWASPlotPresenterWidget gwasPlotPresenterWidget;
    private boolean isFireEvent = false;
    private Receiver<GWASResultProxy> receiverOfSave = null;
    private GWASDataRequest ctx;
    private final PermissionDetailPresenter permissionDetailPresenter;

    @Inject
    public GWASViewerPresenter(EventBus eventBus, MyView view, GWASViewerPresenter.MyProxy proxy,
                               final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget,
                               final CurrentUser currentUser,
                               final PlaceManager placeManager,
                               final GWASDataManager gwasDataManager,
                               final GWASPlotPresenterWidget gwasPlotPresenterWidget,
                               final PermissionDetailPresenter permissionDetailPresenter) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.gwasUploadWizardPresenterWidget = gwasUploadWizardPresenterWidget;
        this.gwasPlotPresenterWidget = gwasPlotPresenterWidget;
        this.permissionDetailPresenter = permissionDetailPresenter;
        this.placeManager = placeManager;
        this.gwasDataManager = gwasDataManager;
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
        dataProvider.addDataDisplay(getView().getDisplay());
        receiverOfSave = new Receiver<GWASResultProxy>() {
            public void onSuccess(GWASResultProxy response) {
                replaceItem(response);
                updateDataGrid();
                getView().showEditPanel(false);
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                getView().showEditPanel(false);
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                super.onConstraintViolation(violations);
                getView().showEditPanel(true);
            }
        };
    }

    private void replaceItem(GWASResultProxy result) {
        for (int i = 0; i < gwasResults.size(); i++) {
            if (gwasResults.get(i).getId().equals(result.getId())) {
                gwasResults.set(i, result);
                return;
            }
        }
    }


    @Override
    public void onBind() {
        super.onBind();
        setInSlot(TYPE_SetGWASUploadContent, gwasUploadWizardPresenterWidget);
        setInSlot(TYPE_SetGWASPLOTContent, gwasPlotPresenterWidget);
        setInSlot(TYPE_SetPermissionContent, permissionDetailPresenter);
        registerHandler(GWASUploadedEvent.register(getEventBus(), new GWASUploadedEvent.Handler() {
            @Override
            public void onGWASUploaded(GWASUploadedEvent event) {
                gwasDataManager.findAllGWASResults(new Receiver<List<GWASResultProxy>>() {
                    @Override
                    public void onSuccess(List<GWASResultProxy> response) {
                        gwasResults = response;
                        getView().hideListPanel(false);
                        updateDataGrid();
                    }
                });
            }
        }));
        registerHandler(getEventBus().addHandlerToSource(PermissionDoneEvent.TYPE, permissionDetailPresenter, new PermissionDoneEvent.Handler() {
            @Override
            public void onPermissionDone(PermissionDoneEvent event) {
                getView().showPermissionPanel(false);
            }
        }));
    }

    private void updateDataGrid() {
        if (gwasResults == null)
            return;
        dataProvider.setList(gwasResults);
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
        getView().hideListPanel(((gwasResults == null || gwasResults.size() == 0) && gwasResult == null));
        updateDataGrid();
        if (gwasResult != null) {
            getView().showPanel(GWASViewerView.PANELS.PLOTS);
            gwasPlotPresenterWidget.loadPlots(gwasResult.getId(), GetGWASDataAction.TYPE.GWASVIEWER);
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
        } else if (gwasResults != null) {
            gwasResult = null;
            getView().showPanel(GWASViewerView.PANELS.LIST);
            getProxy().manualReveal(GWASViewerPresenter.this);
        } else {
            gwasResult = null;
            gwasDataManager.findAllGWASResults(new Receiver<List<GWASResultProxy>>() {
                @Override
                public void onSuccess(List<GWASResultProxy> response) {
                    gwasResults = response;
                    getView().showPanel(GWASViewerView.PANELS.LIST);
                    getProxy().manualReveal(GWASViewerPresenter.this);
                }

                @Override
                public void onFailure(ServerFailure error) {
                    getProxy().manualReveal(GWASViewerPresenter.this);
                }
            });
        }
    }

    @Override
    public void onShowPermissions(GWASResultProxy object) {
        getView().showPermissionPanel(true);
        permissionDetailPresenter.setDomainObject(object, placeManager.buildHistoryToken(new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest()).with("id", object.getId().toString()).build()));
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
        if (Window.confirm("Do you really want to delete the record")) {
            gwasDataManager.delete(new Receiver<List<GWASResultProxy>>() {
                @Override
                public void onSuccess(List<GWASResultProxy> response) {
                    gwasResults = response;
                    getView().hideListPanel((gwasResults == null || gwasResults.size() == 0));
                    updateDataGrid();
                }
            }, object);
        }
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
}
