package com.gmi.nordborglab.browser.client.mvp.presenter.diversity;

import java.util.List;


import com.gmi.nordborglab.browser.client.events.GWASResultLoadedEvent;
import com.gmi.nordborglab.browser.client.events.OntologyLoadedEvent;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.manager.HelperManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.SearchPresenter;
import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.DiversityView.MENU_ITEM;


import java.util.List;

public class DiversityPresenter extends
        Presenter<DiversityPresenter.MyView, DiversityPresenter.MyProxy> {


    public interface MyView extends View {
        void clearBreadcrumbs(int size);

        void setBreadcrumbs(int index, String title, String historyToken);

        void setTitle(String title);

        void setActiveMenuItem(MENU_ITEM menuItem, PlaceRequest request);

        void checkTour();
    }


    @ProxyCodeSplit
    public interface MyProxy extends Proxy<DiversityPresenter> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();
    public static final Object TYPE_SearchPresenterContent = new Object();

    private final PlaceManager placeManager;
    private final HelperManager helperManager;
    protected String titleType = null;
    protected Long titleId = null;
    protected List<TaxonomyProxy> taxonomies = null;
    private final SearchPresenter searchPresenter;


    @Inject
    public DiversityPresenter(final EventBus eventBus, final MyView view,
                              final MyProxy proxy, final PlaceManager placeManager,
                              final HelperManager helperManager, final SearchPresenter searchPresenter) {
        super(eventBus, view, proxy, MainPagePresenter.TYPE_SetMainContent);
        this.searchPresenter = searchPresenter;
        searchPresenter.setCategory(CATEGORY.DIVERSITY);
        this.placeManager = placeManager;
        this.helperManager = helperManager;
    }


    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SearchPresenterContent, searchPresenter);
        registerHandler(GWASResultLoadedEvent.register(getEventBus(), new GWASResultLoadedEvent.Handler() {
            @Override
            public void onGWASResultLoaded(GWASResultLoadedEvent event) {
                if (placeManager.getCurrentPlaceRequest().matchesNameToken(NameTokens.gwasViewer)) {
                    getView().clearBreadcrumbs(1);
                    getView().setBreadcrumbs(0, "ALL", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.gwasViewer).build()));
                    getView().setBreadcrumbs(1, event.getGWASResult().getName(), placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest()));
                }
            }
        }));
        registerHandler(OntologyLoadedEvent.register(getEventBus(), new OntologyLoadedEvent.Handler() {
            @Override
            public void onOntologyLoaded(OntologyLoadedEvent event) {
                if (placeManager.getCurrentPlaceRequest().matchesNameToken(NameTokens.traitontology)) {
                    getView().clearBreadcrumbs(1);
                    TermProxy term = event.getTerm();
                    PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.traitontology);
                    String subTitle = "Trait";
                    if (term.getTermType().equalsIgnoreCase("plant_trait_ontology")) {
                        request = request.with("ontology", "trait");
                    }
                    getView().setBreadcrumbs(0, subTitle, placeManager.buildHistoryToken(request.build()));
                    getView().setBreadcrumbs(1, term.getName() + " (" + term.getAcc() + ")", placeManager.buildHistoryToken(request.with("id", term.getAcc()).build()));
                }
            }
        }));
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
        clearSlot(TYPE_SearchPresenterContent);
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (taxonomies == null) {

        }
        setTitle();
        getView().checkTour();
    }

    protected void setTitle() {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        String type = null;
        String title = "Studies";
        String subItem = null;
        if (request.matchesNameToken(NameTokens.experiments)) {
            getView().clearBreadcrumbs(0);
            getView().setActiveMenuItem(MENU_ITEM.EXPERIMENT, request);
        }
        if (request.matchesNameToken(NameTokens.experiment) || request.matchesNameToken(NameTokens.phenotypes) || request.matchesNameToken(NameTokens.experimentsEnrichments)) {
            type = "experiment";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.EXPERIMENT, request);
        } else if (request.matchesNameToken(NameTokens.phenotype)
                || request.matchesNameToken(NameTokens.obsunit)
                || request.matchesNameToken(NameTokens.studylist)
                || request.matchesNameToken(NameTokens.phenotypeEnrichments)
                ) {
            title = "Phenotype";
            type = "phenotype";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.PHENOTYPE, request);
        } else if (request.matchesNameToken(NameTokens.phenotypeoverview)) {
            getView().clearBreadcrumbs(0);
            type = "phenotype";
            title = "Phenotypes";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.PHENOTYPE, request);
        } else if (request.matchesNameToken(NameTokens.studyoverview)) {
            getView().clearBreadcrumbs(0);
            type = "study";
            title = "Analyses";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.STUDY, request);
        } else if (
                request.matchesNameToken(NameTokens.study) ||
                        request.matchesNameToken(NameTokens.studygwas) ||
                        request.matchesNameToken(NameTokens.studyEnrichments)) {
            type = "study";
            title = "Analysis";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.STUDY, request);
        } else if (request.matchesNameToken(NameTokens.traitontology)) {
            getView().clearBreadcrumbs(0);
            type = "ontology";
            title = "Ontologies";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.ONTOLOGY, request);
        } else if (request.matchesNameToken(NameTokens.study)) {
            getView().clearBreadcrumbs(0);
            title = "Analysis";
            type = "study";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.STUDY, request);
        } else if (request.matchesNameToken(NameTokens.studywizard)) {
            getView().clearBreadcrumbs(0);
            title = "Analysis";
            type = "studywizard";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.STUDY, request);
        } else if (request.matchesNameToken(NameTokens.gwasViewer)) {
            getView().clearBreadcrumbs(0);
            title = "GWAS Viewer";
            getView().setActiveMenuItem(MENU_ITEM.TOOLS, request);
            getView().clearBreadcrumbs(0);
        } else if (request.matchesNameToken(NameTokens.publications)) {
            getView().clearBreadcrumbs(0);
            title = "Publications";
            type = "publications";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.PUBLICATION, request);
        } else if (request.matchesNameToken((NameTokens.publication))) {
            getView().clearBreadcrumbs(0);
            title = "Publication";
            type = "publication";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.PUBLICATION, request);
        } else if (request.matchesNameToken(NameTokens.metaAnalysisGenes) || request.matchesNameToken(NameTokens.metaAnalysisTopResults)) {
            getView().clearBreadcrumbs(0);
            title = "Meta-analysis";
            type = "meta";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.META_ANALYSIS, request);
        } else if (request.matchesNameToken(NameTokens.candidateGeneList) || request.matchesNameToken(NameTokens.candidateGeneListDetail)) {
            getView().clearBreadcrumbs(0);
            title = "Candidate gene lists";
            type = "candidategenelist";
            subItem = null;
            getView().setActiveMenuItem(MENU_ITEM.META_ANALYSIS, request);
        }
        getView().setTitle(title);
        Long id = null;
        try {
            id = Long.parseLong(request.getParameter("id", null));
        } catch (Exception e) {

        }
        if (!titleUpdateRequired(type, id))
            return;
        helperManager.getBreadcrumbs(new Receiver<List<BreadcrumbItemProxy>>() {

            @Override
            public void onSuccess(List<BreadcrumbItemProxy> response) {
                getView().clearBreadcrumbs(response.size());
                //TODO publications doen't work this way.
                getView().setBreadcrumbs(0, "ALL", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build()));
                for (int i = 0; i < response.size(); i++) {
                    BreadcrumbItemProxy item = response.get(i);
                    String nameToken = null;
                    if (item.getType().equals("experiment"))
                        nameToken = NameTokens.experiment;
                    else if (item.getType().equals("phenotype"))
                        nameToken = NameTokens.phenotype;
                    else if (item.getType().equals("study"))
                        nameToken = NameTokens.study;
                    else if (item.getType().equals("studywizard"))
                        nameToken = NameTokens.studywizard;
                    else if (item.getType().equals("publication"))
                        nameToken = NameTokens.publication;
                    else if (item.getType().equals("candidategenelist"))
                        nameToken = NameTokens.candidateGeneListDetail;
                    PlaceRequest request = new PlaceRequest.Builder()
                            .nameToken(nameToken)
                            .with("id", item.getId().toString()).build();
                    getView().setBreadcrumbs(i + 1, item.getText(), placeManager.buildHistoryToken(request));
                }
            }
        }, id, type);
    }

    protected boolean titleUpdateRequired(String type, Long id) {
        boolean required = false;
        if (type != null) {
            if (!type.equals(titleType)) {
                if (id != null)
                    required = true;
            } else if (id != null && !id.equals(titleId)) {
                required = true;
            }
        }
        titleType = type;
        titleId = id;
        return required;
    }


    @Override
    public boolean useManualReveal() {
        return false;
    }
}
