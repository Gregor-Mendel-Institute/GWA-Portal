package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology;

import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.client.ui.cells.GraphOntologyCell;
import com.gmi.nordborglab.browser.client.ui.cells.OntologyCell;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.google.gwt.view.client.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphOntologyTreeViewModel implements TreeViewModel {

    private final SelectionModel<GraphTerm2TermProxy> selectionModel;
    private final TraitOntologyView.GraphOntologyDataProvider dataProvider;
    private final DefaultSelectionEventManager<GraphTerm2TermProxy> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final GraphOntologyCell ontologyCell;



    public GraphOntologyTreeViewModel(final SelectionModel<GraphTerm2TermProxy> selectionModel, TraitOntologyView.GraphOntologyDataProvider dataProvider,final GraphOntologyCell ontologyCell) {
        this.selectionModel = selectionModel;
        this.dataProvider = dataProvider;
        this.ontologyCell = ontologyCell;

    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(final T value) {
        AsyncDataProvider<GraphTerm2TermProxy> provider = new AsyncDataProvider<GraphTerm2TermProxy>() {
            @Override
            protected void onRangeChanged(HasData<GraphTerm2TermProxy> display) {
                dataProvider.refreshWithChildTerms(display,(GraphTerm2TermProxy)value);
            }
        };
        return new DefaultNodeInfo<GraphTerm2TermProxy>(provider,ontologyCell,selectionModel, selectionManager,null);
    }

    @Override
    public boolean isLeaf(Object value) {
        GraphTerm2TermProxy term = (GraphTerm2TermProxy)value;
        return value != null && term.getChild().getChildCount() == 0;
    }
}
