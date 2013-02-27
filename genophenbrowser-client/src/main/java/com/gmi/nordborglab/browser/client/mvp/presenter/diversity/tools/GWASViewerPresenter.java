package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.manager.GWASDataManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASViewerUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASViewerView;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASViewerPresenter extends Presenter<GWASViewerPresenter.MyView,GWASViewerPresenter.MyProxy>
                                implements GWASViewerUiHandlers {

    public interface MyView extends View,HasUiHandlers<GWASViewerUiHandlers>{

        void hideListPanel(boolean hide);

        void showPanel(GWASViewerView.PANELS panel);

        HasData<GWASResultProxy> getDisplay();

    }
    @ProxyCodeSplit
    @NameToken(NameTokens.gwasViewer)
    public interface MyProxy extends ProxyPlace<GWASViewerPresenter>{

    }
    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetGWASUploadContent = new GwtEvent.Type<RevealContentHandler<?>>();

    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetGWASPLOTContent = new GwtEvent.Type<RevealContentHandler<?>>();
    private final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget;
    private final ListDataProvider<GWASResultProxy> dataProvider = new ListDataProvider<GWASResultProxy>();
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;
    private final GWASDataManager gwasDataManager;
    private Long gwasResultId = null;
    private List<GWASResultProxy> gwasResults;
    private final GWASPlotPresenterWidget gwasPlotPresenterWidget;
    @Inject
    public GWASViewerPresenter(EventBus eventBus, MyView view, GWASViewerPresenter.MyProxy proxy,
                               final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget,
                               final CurrentUser currentUser,
                               final PlaceManager placeManager,
                               final GWASDataManager gwasDataManager,
                               final GWASPlotPresenterWidget gwasPlotPresenterWidget) {
        super(eventBus, view, proxy);
        this.gwasUploadWizardPresenterWidget = gwasUploadWizardPresenterWidget;
        this.gwasPlotPresenterWidget = gwasPlotPresenterWidget;
        this.placeManager = placeManager;
        this.gwasDataManager = gwasDataManager;
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
        dataProvider.addDataDisplay(getView().getDisplay());
    }


    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }

    @Override
    public void onBind() {
        super.onBind();
        setInSlot(TYPE_SetGWASUploadContent,gwasUploadWizardPresenterWidget);
        setInSlot(TYPE_SetGWASPLOTContent,gwasPlotPresenterWidget);
        registerHandler(GWASUploadedEvent.register(getEventBus(),new GWASUploadedEvent.Handler() {
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
    }

    private void updateDataGrid() {
        if (gwasResults == null)
            return;
        dataProvider.setList(gwasResults);
    }

    @Override
    public void onReset() {
        super.onReset();
        getView().hideListPanel((gwasResults == null || gwasResults.size() == 0));
        updateDataGrid();
        if (gwasResultId != null) {
            gwasPlotPresenterWidget.loadPlots(gwasResultId, GetGWASDataAction.TYPE.GWASVIEWER);
            getView().showPanel(GWASViewerView.PANELS.PLOTS);
        }
    }


    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        try {
            gwasResultId = Long.parseLong(request.getParameter("id",null));
        }
        catch (Exception e) {}
        if (!currentUser.isLoggedIn() && gwasResultId == null) {
            getProxy().manualRevealFailed();
            Window.Location.assign("/login?url="+placeManager.buildHistoryToken(request));
            return;
        }
        gwasDataManager.findAllGWASResults(new Receiver<List<GWASResultProxy>>() {
            @Override
            public void onSuccess(List<GWASResultProxy> response) {
                gwasResults = response;
                getProxy().manualReveal(GWASViewerPresenter.this);
            }

            @Override
            public void onFailure(ServerFailure error) {
                getProxy().manualReveal(GWASViewerPresenter.this);
            }
        });
        getProxy().manualReveal(this);
    }

    @Override
    public void onShowPermissions(GWASResultProxy object) {
    }

    @Override
    public void onEdit(GWASResultProxy object) {
        //To change body of implemented methods use File | Settings | File Templates.
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
}
