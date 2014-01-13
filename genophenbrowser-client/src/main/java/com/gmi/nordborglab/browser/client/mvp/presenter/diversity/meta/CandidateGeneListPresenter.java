package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadCandidateGeneListEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.CanidateGeneListUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.CandidateGeneListView;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import javax.validation.ConstraintViolation;
import java.util.List;
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

        void setActiveNavLink(ConstEnums.TABLE_FILTER filter);

        void displayFacets(List<FacetProxy> facets, String searchString);

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
    private ConstEnums.TABLE_FILTER currentFilter = ConstEnums.TABLE_FILTER.ALL;
    private String searchString = null;
    private List<FacetProxy> facets;
    private Receiver<CandidateGeneListProxy> receiver = null;
    private final CurrentUser currentUser;
    public static final String placeToken = NameTokens.candidateGeneList;

    @Inject
    public CandidateGeneListPresenter(EventBus eventBus, CandidateGeneListPresenter.MyView view,
                                      CandidateGeneListPresenter.MyProxy proxy, final CustomRequestFactory rf,
                                      final PlaceManager placeManager, final CurrentUser currentUser) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.currentUser = currentUser;
        this.rf = rf;
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
                facets = candidateGeneLists.getFacets();
                getView().displayFacets(facets, searchString);
            }
        };
        Range range = getView().getDisplay().getVisibleRange();
        rf.metaAnalysisRequest().findCandidateGeneLists(currentFilter, searchString, range.getStart(), range.getLength()).with("contents.acl", "contents.ownerUser").fire(receiver);
    }


    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    @Override
    protected void onReset() {
        super.onReset();
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        ConstEnums.TABLE_FILTER newFilter = ConstEnums.TABLE_FILTER.ALL;
        String newCategoryString = request.getParameter("filter", null);
        String newSearchString = request.getParameter("query", null);
        if (newCategoryString != null) {
            try {
                newFilter = ConstEnums.TABLE_FILTER.valueOf(newCategoryString);
            } catch (Exception e) {

            }
        }
        if (newFilter != currentFilter || newSearchString != searchString) {
            currentFilter = newFilter;
            searchString = newSearchString;
            getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
        }
        getView().setActiveNavLink(currentFilter);
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

    @Override
    public void updateSearchString(String value) {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(placeToken);
        if (currentFilter != null) {
            request = request.with("filter", currentFilter.name());
        }
        if (value != null && !value.equals("")) {
            request = request.with("query", value);
        }
        placeManager.revealPlace(request.build());
    }
}
