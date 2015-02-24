package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.list;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadCandidateGeneListEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.09.13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListPresenter extends Presenter<CandidateGeneListPresenter.MyView, CandidateGeneListPresenter.MyProxy> implements CanidateGeneListUiHandlers {

    public interface MyView extends View, HasUiHandlers<CanidateGeneListUiHandlers> {
        HasData<CandidateGeneListProxy> getDisplay();

        CandidateGeneListView.CandidateGeneListEditDriver getCandidateGeneListEditDriver();

        void showEditPopup(boolean show);

        void showCreateBtn(boolean loggedIn);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.candidateGeneList)
    public interface MyProxy extends ProxyPlace<CandidateGeneListPresenter> {

    }

    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;
    protected final AsyncDataProvider<CandidateGeneListProxy> dataProvider;
    private Receiver<CandidateGeneListProxy> receiver = null;
    private final CurrentUser currentUser;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    @Inject
    public CandidateGeneListPresenter(EventBus eventBus, CandidateGeneListPresenter.MyView view,
                                      CandidateGeneListPresenter.MyProxy proxy, final CustomRequestFactory rf,
                                      final PlaceManager placeManager, final CurrentUser currentUser,
                                      final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.currentUser = currentUser;
        this.rf = rf;
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        this.facetSearchPresenterWidget.setDefaultFilter(ConstEnums.TABLE_FILTER.ALL.name());
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
        dataProvider = new AsyncDataProvider<CandidateGeneListProxy>() {

            @Override
            protected void onRangeChanged(HasData<CandidateGeneListProxy> display) {
                requestCandidateGeneLists();
            }
        };
        receiver = new Receiver<CandidateGeneListProxy>() {

            public void onSuccess(CandidateGeneListProxy response) {
                addCandidateGeneList(response);
                fireEvent(new LoadingIndicatorEvent(false));
                fireEvent(new LoadCandidateGeneListEvent(response));
                getView().showEditPopup(false);
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                onCreate();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onConstraintViolation(violations);
            }
        };
    }

    private void addCandidateGeneList(CandidateGeneListProxy response) {
        requestCandidateGeneLists();
    }

    private void requestCandidateGeneLists() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<CandidateGeneListPageProxy> receiver = new Receiver<CandidateGeneListPageProxy>() {
            @Override
            public void onSuccess(CandidateGeneListPageProxy candidateGeneLists) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) candidateGeneLists.getTotalElements(), true);
                dataProvider.updateRowData(getView().getDisplay().getVisibleRange().getStart(), candidateGeneLists.getContents());
                facetSearchPresenterWidget.displayFacets(candidateGeneLists.getFacets());
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        rf.metaAnalysisRequest().findCandidateGeneLists(ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength()).with("contents.acl", "contents.ownerUser").fire(receiver);
    }


    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
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
        getView().showCreateBtn(currentUser.isLoggedIn());
    }


    @Override
    public void onSave() {
        RequestContext req = getView().getCandidateGeneListEditDriver().flush();
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        req.fire();
    }

    @Override
    public void onCancel() {
        getView().showEditPopup(false);
    }

    @Override
    public void onCreate() {
        MetaAnalysisRequest ctx = rf.metaAnalysisRequest();
        CandidateGeneListProxy candidateGeneListProxy = ctx.create(CandidateGeneListProxy.class);
        getView().getCandidateGeneListEditDriver().edit(candidateGeneListProxy, ctx);
        ctx.saveCandidateGeneList(candidateGeneListProxy).with("userPermission", "ownerUser").to(receiver);
        getView().showEditPopup(true);
    }
}
