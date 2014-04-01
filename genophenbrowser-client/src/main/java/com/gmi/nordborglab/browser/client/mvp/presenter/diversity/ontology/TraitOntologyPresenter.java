package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology;

import com.gmi.nordborglab.browser.client.events.OntologyLoadedEvent;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.OntologyUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Collections;
import java.util.List;

public class TraitOntologyPresenter
        extends
        Presenter<TraitOntologyPresenter.MyView, TraitOntologyPresenter.MyProxy> implements OntologyUiHandlers {

    public interface MyView extends View, HasUiHandlers<OntologyUiHandlers> {

        void initNavigationTree(TermProxy term);

        void setRootOntology(TermProxy term);

        TraitOntologyView.OntologyDisplayDriver getDisplayDriver();

        HasData<PhenotypeProxy> getPhenotypeDisplay();

        void setPhenotypeCount(Integer count);

        void openNavTreeAndSelectItem(TermProxy term);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.traitontology)
    public interface MyProxy extends ProxyPlace<TraitOntologyPresenter> {

    }

    private final OntologyManager ontologyManager;
    private final PlaceManager placeManager;
    private ConstEnums.ONTOLOGY_TYPE ontologyType = ConstEnums.ONTOLOGY_TYPE.TRAIT;
    private Term2TermProxy selectedTerm;
    private ListDataProvider<PhenotypeProxy> phenotypeDataProvider = new ListDataProvider<PhenotypeProxy>();
    private final PhenotypeManager phenotypeManager;


    @Inject
    public TraitOntologyPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy,
                                  final OntologyManager ontologyManager,
                                  final PlaceManager placeManager,
                                  final PhenotypeManager phenotypeManager) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.phenotypeManager = phenotypeManager;
        this.placeManager = placeManager;
        this.ontologyManager = ontologyManager;
        getView().setUiHandlers(this);
        phenotypeDataProvider.addDataDisplay(getView().getPhenotypeDisplay());
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
            ontologyType = ConstEnums.ONTOLOGY_TYPE.valueOf(request.getParameter("ontology", ConstEnums.ONTOLOGY_TYPE.TRAIT.name()).toUpperCase());
            acc = request.getParameter("id", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (acc == null) {
            getView().initNavigationTree(null);
            getView().openNavTreeAndSelectItem(null);
            selectedTerm = null;
        } else {
            ontologyManager.findOneByAcc(new Receiver<TermProxy>() {
                @Override
                public void onSuccess(TermProxy response) {
                    selectedTerm = Iterables.get(response.getParents(), 0);
                    getView().initNavigationTree(null);
                }
            }, acc);
        }


    }

    private void resetView() {
        getView().getDisplayDriver().display(ontologyManager.getContext().create(TermProxy.class));
        getView().setPhenotypeCount(0);
        phenotypeDataProvider.setList(Collections.<PhenotypeProxy>emptyList());
    }

    @Override
    public void refreshWithChildTerms(final HasData<Term2TermProxy> display, Term2TermProxy term) {
        if (term != null) {
            ontologyManager.findOneTerm2Term(new Receiver<Term2TermProxy>() {
                @Override
                public void onSuccess(Term2TermProxy response) {
                    Range range = display.getVisibleRange();
                    display.setRowData(range.getStart(), ImmutableList.copyOf(response.getChild().getChilds()));
                    if (selectedTerm != null) {
                        getView().openNavTreeAndSelectItem(selectedTerm.getChild());
                    }
                }
            }, term.getId());
        } else {
            ontologyManager.findRootTerm(new Receiver<TermProxy>() {
                @Override
                public void onSuccess(TermProxy response) {
                    Range range = display.getVisibleRange();
                    getView().setRootOntology(response);
                    display.setRowData(range.getStart(), ImmutableList.copyOf(response.getChilds()));
                    if (selectedTerm != null) {
                        getView().openNavTreeAndSelectItem(selectedTerm.getChild());
                    }
                }
            }, ontologyType.toString());
        }
    }

    @Override
    public void onSelectTerm(Term2TermProxy selectedTerm) {
        this.selectedTerm = null;
        if (selectedTerm != null) {
            PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
            //TODO doesn't work when it checks if nameTokens match
            placeManager.updateHistory(request.with("id", selectedTerm.getChild().getAcc()).build(), true);
            loadTermDataAndDisplay(selectedTerm);
            OntologyLoadedEvent.fire(getEventBus(), selectedTerm.getChild());
        } else {
            resetView();
        }
    }

    private void loadTermDataAndDisplay(Term2TermProxy selectedTerm) {
        getView().getDisplayDriver().display(selectedTerm.getChild());
        getView().getPhenotypeDisplay().setVisibleRangeAndClearData(getView().getPhenotypeDisplay().getVisibleRange(), false);
        phenotypeManager.findAllByOntology(new Receiver<List<PhenotypeProxy>>() {
            @Override
            public void onSuccess(List<PhenotypeProxy> response) {
                phenotypeDataProvider.setList(response);
                getView().setPhenotypeCount(response.size());
            }
        }, ontologyType.name(), selectedTerm.getChild().getAcc(), true);
    }
}
