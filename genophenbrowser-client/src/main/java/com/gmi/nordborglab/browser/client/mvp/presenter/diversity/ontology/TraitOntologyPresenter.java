package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.manager.GraphOntologyManager;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.OntologyUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.Collections;
import java.util.List;

public class TraitOntologyPresenter
		extends
		Presenter<TraitOntologyPresenter.MyView, TraitOntologyPresenter.MyProxy> implements OntologyUiHandlers {

    public interface MyView extends View,HasUiHandlers<OntologyUiHandlers> {

        void initNavigationTree();

        void setRootOntology(GraphTermProxy term);

        TraitOntologyView.OntologyDisplayDriver getDisplayDriver();

        HasData<PhenotypeProxy> getPhenotypeDisplay();

        void setPhenotypeCount(Integer count);

        void openNavTreeAndSelectItem(GraphTermProxy term);
    }

	@ProxyCodeSplit
	@NameToken(NameTokens.traitontology)
	public interface MyProxy extends ProxyPlace<TraitOntologyPresenter> {

    }

    public static enum ONTOLOGY_TYPE {TRAIT,ENVIRONMENT}

    private final OntologyManager ontologyManager;
    private final GraphOntologyManager graphOntologyManager;
    private final PlaceManager placeManager;
    private ONTOLOGY_TYPE ontologyType = ONTOLOGY_TYPE.TRAIT;
    private GraphTerm2TermProxy selectedTerm;
    private ListDataProvider<PhenotypeProxy> phenotypeDataProvider = new ListDataProvider<PhenotypeProxy>();
    private final PhenotypeManager phenotypeManager;


	@Inject
	public TraitOntologyPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy,
                                  final OntologyManager ontologyManager,
                                  final PlaceManager placeManager,
                                  final PhenotypeManager phenotypeManager,
                                  final GraphOntologyManager graphOntologyManager) {
		super(eventBus, view, proxy);
        this.phenotypeManager = phenotypeManager;
        this.placeManager = placeManager;
        this.graphOntologyManager = graphOntologyManager;
        this.ontologyManager = ontologyManager;
        getView().setUiHandlers(this);
        phenotypeDataProvider.addDataDisplay(getView().getPhenotypeDisplay());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        String acc = null;
        try {
            ontologyType = ONTOLOGY_TYPE.valueOf(request.getParameter("ontology",ONTOLOGY_TYPE.TRAIT.name()).toUpperCase());
            acc = request.getParameter("id",null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (acc == null) {
            getView().initNavigationTree();
            getView().openNavTreeAndSelectItem(null);
            selectedTerm = null;
        }
        else {
           graphOntologyManager.findOneByAcc(new Receiver<GraphTermProxy>() {
               @Override
               public void onSuccess(GraphTermProxy response) {
                   selectedTerm = Iterables.get(response.getParents(), 0);
                   getView().initNavigationTree();
               }
           },acc);
        }


    }

    private void resetView() {
        //getView().getDisplayDriver().display(graphOntologyManager.getContext().create(GraphTermProxy.class));
        getView().setPhenotypeCount(0);
        phenotypeDataProvider.setList(Collections.<PhenotypeProxy>emptyList());
    }

    @Override
    public void refreshWithChildTerms(final HasData<GraphTerm2TermProxy> display, GraphTerm2TermProxy term) {
        if (term != null) {
            graphOntologyManager.findOneTerm2Term(new Receiver<GraphTerm2TermProxy>() {
                @Override
                public void onSuccess(GraphTerm2TermProxy response) {
                    Range range = display.getVisibleRange();
                    display.setRowData(range.getStart(), ImmutableList.copyOf(response.getChild().getChildren()));
                    if (selectedTerm != null) {
                        getView().openNavTreeAndSelectItem(selectedTerm.getChild());
                    }
                }
            }, term.getNodeId());
        }
        else {
            graphOntologyManager.findRootTerm(new Receiver<GraphTermProxy>() {
                @Override
                public void onSuccess(GraphTermProxy response) {
                    Range range = display.getVisibleRange();
                    getView().setRootOntology(response);
                    display.setRowData(range.getStart(), ImmutableList.copyOf(response.getChildren()));
                    if (selectedTerm != null) {
                        getView().openNavTreeAndSelectItem(selectedTerm.getChild());
                    }
                }
            }, ontologyType.toString());
        }
    }

    @Override
    public void onSelectTerm(GraphTerm2TermProxy selectedTerm) {
        this.selectedTerm = null;
        if (selectedTerm != null) {
            PlaceRequest request = placeManager.getCurrentPlaceRequest();
            //TODO doesn't work when it checks if nameTokens match
            placeManager.updateHistory(request.with("id",selectedTerm.getChild().getId()),true);
            loadTermDataAndDisplay(selectedTerm);
            //OntologyLoadedEvent.fire(getEventBus(),selectedTerm.getChild());
        }
        else {
           resetView();
        }
    }

    private void loadTermDataAndDisplay(GraphTerm2TermProxy selectedTerm) {
        //getView().getDisplayDriver().display(selectedTerm.getChild());
        getView().getPhenotypeDisplay().setVisibleRangeAndClearData(getView().getPhenotypeDisplay().getVisibleRange(),false);
        phenotypeManager.findAllByOntology(new Receiver<List<PhenotypeProxy>>() {
            @Override
            public void onSuccess(List<PhenotypeProxy> response) {
                phenotypeDataProvider.setList(response);
                getView().setPhenotypeCount(response.size());
            }
        },ontologyType.name(),selectedTerm.getChild().getId(),true);
    }
}
