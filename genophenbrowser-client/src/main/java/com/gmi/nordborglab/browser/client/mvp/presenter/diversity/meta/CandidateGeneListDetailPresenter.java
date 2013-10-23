package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadCandidateGeneListEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.CandidateGeneListDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.CandidateGeneListDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.CandidateGeneListView;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.view.client.*;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.09.13
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListDetailPresenter extends Presenter<CandidateGeneListDetailPresenter.MyView, CandidateGeneListDetailPresenter.MyProxy> implements CandidateGeneListDetailUiHandlers {

    public enum STATS {CHR, ANNOTATION, STRAND}

    public interface MyView extends View, HasUiHandlers<CandidateGeneListDetailUiHandlers> {

        HasData<GeneProxy> getGenesDisplay();

        CandidateGeneListDetailView.CandidateGeneListDisplayDriver getDisplayDriver();

        void displayFacets(List<FacetProxy> facets);

        void showEditPopup(boolean show);

        void showDeletePopup(boolean show);

        void setShareTooltip(String toopltipMsg, IconType icon);

        void showShareBtn(boolean show);

        void showActionBtns(boolean show);

        CandidateGeneListView.CandidateGeneListEditDriver getEditDriver();

        void showPermissionPanel(boolean show);

        void setActiveNavLink(ConstEnums.GENE_FILTER filter);

        void phaseInPublication(GeneProxy gene);

        HasText getSearchBox();

        void enableAddBtn(boolean enable);

        void setStatsData(DataTable dataTable, STATS stat);

        void refreshStats();

        void setUploadActionUrl(String url);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.candidateGeneListDetail)
    public interface MyProxy extends ProxyPlace<CandidateGeneListDetailPresenter> {
    }

    private CandidateGeneListProxy candidateGeneList;
    private final PermissionDetailPresenter permissionDetailPresenter;
    private final Receiver<CandidateGeneListProxy> receiver = new Receiver<CandidateGeneListProxy>() {
        public void onSuccess(CandidateGeneListProxy response) {
            fireEvent(new LoadingIndicatorEvent(false));
            candidateGeneList = response;
            getView().getDisplayDriver().display(candidateGeneList);
            getView().showEditPopup(false);
        }

        public void onFailure(ServerFailure error) {
            fireEvent(new LoadingIndicatorEvent(false));
            fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
            onEdit();
        }

        public void onConstraintViolation(
                Set<ConstraintViolation<?>> violations) {
            fireEvent(new LoadingIndicatorEvent(false));
            super.onConstraintViolation(violations);
        }
    };

    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;
    private final CurrentUser currentUser;
    private List<FacetProxy> facets;
    private List<FacetProxy> statsFacets;
    private ConstEnums.GENE_FILTER currentFilter = ConstEnums.GENE_FILTER.ALL;
    public static final Object TYPE_SetPermissionContent = new Object();
    private GenePageProxy genesPage;
    private final BiMap<ConstEnums.GENE_FILTER, List<String>> filter2Annotation;


    private final AsyncDataProvider<GeneProxy> genesDataProvider = new AsyncDataProvider<GeneProxy>() {
        @Override
        protected void onRangeChanged(HasData<GeneProxy> display) {
            requestGenes(display, null);
        }
    };


    @Inject
    public CandidateGeneListDetailPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                            final PermissionDetailPresenter permissionDetailPresenter,
                                            final CustomRequestFactory rf, final PlaceManager placeManager,
                                            final CurrentUser currentUser) {
        super(eventBus, view, proxy);
        filter2Annotation = new ImmutableBiMap.Builder<ConstEnums.GENE_FILTER, List<String>>()
                .put(ConstEnums.GENE_FILTER.PROTEIN, Lists.newArrayList("gene"))
                .put(ConstEnums.GENE_FILTER.TRANSPOSON, Lists.newArrayList("transposable_element", "transposable_element_gene"))
                .put(ConstEnums.GENE_FILTER.PSEUDO, Lists.newArrayList("pseudogene")).build();
        this.permissionDetailPresenter = permissionDetailPresenter;
        this.currentUser = currentUser;
        this.rf = rf;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
        genesDataProvider.addDataDisplay(getView().getGenesDisplay());
    }

    private String geneId = null;


    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        setInSlot(TYPE_SetPermissionContent, permissionDetailPresenter);
        registerHandler(getEventBus().addHandlerToSource(PermissionDoneEvent.TYPE, permissionDetailPresenter, new PermissionDoneEvent.Handler() {
            @Override
            public void onPermissionDone(PermissionDoneEvent event) {
                getView().showPermissionPanel(false);
                refreshView();
            }
        }));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    private void requestGenes(final HasData<GeneProxy> display, final GeneProxy newGeneProxy) {
        if (candidateGeneList == null)
            return;
        if (genesPage == null) {
            fireEvent(new LoadingIndicatorEvent(true));
            Receiver<GenePageProxy> receiver = new Receiver<GenePageProxy>() {
                @Override
                public void onSuccess(GenePageProxy response) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    genesPage = response;
                    facets = genesPage.getFacets();
                    statsFacets = genesPage.getStatsFacets();
                    getView().displayFacets(facets);
                    displayStats();
                    filterAndDisplayGenes(newGeneProxy);
                }
            };
            Range range = display.getVisibleRange();
            rf.metaAnalysisRequest().getGenesInCandidateGeneList(candidateGeneList.getId(), currentFilter, geneId, range.getStart(), range.getLength()).fire(receiver);
        } else {
            filterAndDisplayGenes(newGeneProxy);
        }
    }

    private void displayStats() {
        for (FacetProxy facet : statsFacets) {
            STATS stat = STATS.valueOf(facet.getName().toUpperCase());
            DataTable dataTable = DataTableUtils.createFroMFacets(facet);
            getView().setStatsData(dataTable, stat);
        }
        getView().refreshStats();
    }

   /*private void displayGenes(GenePageProxy genePage, HasData<GeneProxy> display) {
        fireEvent(new LoadingIndicatorEvent(false));
        genesDataProvider.updateRowCount((int) genePage.getTotalElements(), true);
        genesDataProvider.updateRowData(display.getVisibleRange().getStart(), genePage.getContent());
        facets = genePage.getFacets();
        getView().displayFacets(facets);
    }*/

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {

        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        try {
            Long id = Long.valueOf(placeRequest.getParameter("id", null));
            if (candidateGeneList == null || !candidateGeneList.getId().equals(id)) {
                MetaAnalysisRequest ctx = rf.metaAnalysisRequest();
                ctx.findOneCandidateGeneList(id).with("userPermission").to(new Receiver<CandidateGeneListProxy>() {
                    @Override
                    public void onSuccess(CandidateGeneListProxy response) {
                        candidateGeneList = response;
                    }
                });
                ctx.fire(new Receiver<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getProxy().manualReveal(CandidateGeneListDetailPresenter.this);
                    }

                    @Override
                    public void onFailure(ServerFailure error) {
                        getProxy().manualRevealFailed();
                        placeManager.revealPlace(new PlaceRequest(
                                NameTokens.candidateGeneList));
                    }
                });
            } else {
                getProxy().manualReveal(CandidateGeneListDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest(NameTokens.candidateGeneList));
        }
    }

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        fireEvent(new LoadingIndicatorEvent(false));
        refreshView();
        requestGenes(getView().getGenesDisplay(), null);
    }

    @Override
    public void onSearchForGene(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        rf.searchRequest().searchGeneByTerm(request.getQuery()).fire(new Receiver<SearchFacetPageProxy>() {

            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                SuggestOracle.Response searchResponse = new SuggestOracle.Response();
                Collection<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
                if (response != null) {
                    for (SearchItemProxy searchItem : response.getContents()) {
                        suggestions.add(new SearchSuggestOracle.SearchSuggestion(searchItem));
                    }
                }
                searchResponse.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, searchResponse);
            }
        });
    }

    @Override
    public void onSelectGene(SuggestOracle.Suggestion suggestion) {
        geneId = suggestion.getReplacementString();
    }

    @Override
    public void onAddGene() {
        rf.metaAnalysisRequest().addGeneToCandidateGeneList(candidateGeneList, geneId).fire(new Receiver<GeneProxy>() {
            @Override
            public void onSuccess(GeneProxy response) {
                genesPage = null;
                requestGenes(getView().getGenesDisplay(), response);
                getView().getSearchBox().setText("");
                getView().enableAddBtn(false);
            }
        });
    }

    @Override
    public void selectFilter(ConstEnums.GENE_FILTER geneFilter) {
        this.currentFilter = geneFilter;
        requestGenes(getView().getGenesDisplay(), null);
        getView().setActiveNavLink(currentFilter);
    }

    @Override
    public void onEdit() {
        MetaAnalysisRequest ctx = rf.metaAnalysisRequest();
        getView().getEditDriver().edit(candidateGeneList, ctx);
        ctx.saveCandidateGeneList(candidateGeneList).with("userPermission").to(receiver);
        getView().showEditPopup(true);
    }

    @Override
    public void onDelete() {
        getView().showDeletePopup(true);
    }

    @Override
    public void onConfirmDelete() {
        fireEvent(new LoadingIndicatorEvent(true, "Deleting..."));
        rf.metaAnalysisRequest().deleteCandidateGeneList(candidateGeneList).fire(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                fireEvent(new LoadingIndicatorEvent(false));
                PlaceRequest request = null;
                if (placeManager.getHierarchyDepth() <= 1) {
                    request = new ParameterizedPlaceRequest(NameTokens.candidateGeneList);
                } else {
                    request = placeManager.getCurrentPlaceHierarchy().get(placeManager.getHierarchyDepth() - 2);
                }
                getView().showDeletePopup(false);
                candidateGeneList = null;
                placeManager.revealPlace(request);
                //TODO fire event to refresh the list
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    public void onCancel() {
        getView().showEditPopup(false);
    }

    @Override
    public void onSave() {
        RequestContext req = getView().getEditDriver().flush();
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        req.fire();
    }

    @Override
    public void onShare() {
        getView().showPermissionPanel(true);
        permissionDetailPresenter.setDomainObject(candidateGeneList, placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest()));
    }

    @Override
    public void onDeleteGene(GeneProxy gene) {
        rf.metaAnalysisRequest().removeGeneFromCandidateGeneList(candidateGeneList, gene.getName()).fire(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                genesPage = null;
                requestGenes(getView().getGenesDisplay(), null);
            }
        });
    }

    @Override
    public void refresh() {
        genesPage = null;
        getView().getGenesDisplay().setVisibleRangeAndClearData(getView().getGenesDisplay().getVisibleRange(), true);
    }

    private void refreshView() {
        getView().getDisplayDriver().display(candidateGeneList);
        getView().showActionBtns(currentUser.hasEdit(candidateGeneList));
        getView().showShareBtn(currentUser.hasAdmin(candidateGeneList));

        getView().setUploadActionUrl("/provider/candidategenelist/" + candidateGeneList.getId() + "/upload");

        String toolTipText = "Public - Anyone on the Internet can find and access";
        IconType toolTipIcon = IconType.GLOBE;
        if (!candidateGeneList.isPublic()) {
            toolTipText = "Private - Only people explicitly granted permission can access";
            toolTipIcon = IconType.LOCK;

        }
        getView().setShareTooltip(toolTipText, toolTipIcon);
    }

    @ProxyEvent
    public void onNewCandidateGeneList(LoadCandidateGeneListEvent event) {
        candidateGeneList = event.getCandidateGeneList();
        genesPage = null;
        PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.candidateGeneListDetail).with("id", candidateGeneList.getId().toString());
        placeManager.revealPlace(request);
    }


    private void filterAndDisplayGenes(final GeneProxy newGeneProxy) {
        Range range = getView().getGenesDisplay().getVisibleRange();
        if (newGeneProxy != null) {
            if (Iterables.find(genesPage.getContents(), new Predicate<GeneProxy>() {
                @Override
                public boolean apply(@Nullable GeneProxy geneProxy) {
                    return newGeneProxy.equals(geneProxy);
                }
            }, null) == null) {
                genesPage.getContents().add(0, newGeneProxy);
            }
        }
        List<GeneProxy> filteredList = ImmutableList.copyOf(Iterables.filter(genesPage.getContents(), new Predicate<GeneProxy>() {
            @Override
            public boolean apply(@Nullable GeneProxy geneProxy) {
                if (!filter2Annotation.containsKey(currentFilter))
                    return true;
                boolean found = false;
                for (String key : filter2Annotation.get(currentFilter)) {
                    if (key.equalsIgnoreCase(geneProxy.getAnnotation())) {
                        found = true;
                        break;
                    }
                }
                return found;
            }
        }));
        Iterable<List<GeneProxy>> partionedList = Iterables.partition(filteredList, range.getLength());
        genesDataProvider.updateRowCount(filteredList.size(), true);
        genesDataProvider.updateRowData(range.getStart(), filteredList);
        if (newGeneProxy != null) {
            getView().phaseInPublication(newGeneProxy);
        }
    }


}
