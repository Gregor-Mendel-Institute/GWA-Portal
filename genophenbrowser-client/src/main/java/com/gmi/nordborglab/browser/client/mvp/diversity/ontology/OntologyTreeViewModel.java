package com.gmi.nordborglab.browser.client.mvp.diversity.ontology;

import com.gmi.nordborglab.browser.client.ui.cells.OntologyCell;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.TreeViewModel;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyTreeViewModel implements TreeViewModel {

    private final SelectionModel<Term2TermProxy> selectionModel;
    private final TraitOntologyView.OntologyDataProvider dataProvider;
    private final DefaultSelectionEventManager<Term2TermProxy> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final OntologyCell ontologyCell;


    public OntologyTreeViewModel(final SelectionModel<Term2TermProxy> selectionModel, TraitOntologyView.OntologyDataProvider dataProvider, final OntologyCell ontologyCell) {
        this.selectionModel = selectionModel;
        this.dataProvider = dataProvider;
        this.ontologyCell = ontologyCell;

    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(final T value) {
        AsyncDataProvider<Term2TermProxy> provider = new AsyncDataProvider<Term2TermProxy>() {
            @Override
            protected void onRangeChanged(HasData<Term2TermProxy> display) {
                dataProvider.refreshWithChildTerms(display, (Term2TermProxy) value);
            }
        };
        return new DefaultNodeInfo<Term2TermProxy>(provider, ontologyCell, selectionModel, selectionManager, null);
    }

    @Override
    public boolean isLeaf(Object value) {
        Term2TermProxy term = (Term2TermProxy) value;
        return value != null && term.getChild().getChildCount() == 0;
    }
}
