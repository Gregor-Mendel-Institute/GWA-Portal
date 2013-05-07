package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology;

import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.client.ui.cells.OntologyCell;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyTreeViewModel implements TreeViewModel{

    private final SelectionModel<Term2TermProxy> selectionModel;
    private final TraitOntologyView.OntologyDataProvider dataProvider;
    private final DefaultSelectionEventManager<Term2TermProxy> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final OntologyCell ontologyCell;



    public OntologyTreeViewModel(final SelectionModel<Term2TermProxy> selectionModel, TraitOntologyView.OntologyDataProvider dataProvider,final OntologyCell ontologyCell) {
        this.selectionModel = selectionModel;
        this.dataProvider = dataProvider;
        this.ontologyCell = ontologyCell;

    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(final T value) {
        AsyncDataProvider<Term2TermProxy> provider = new AsyncDataProvider<Term2TermProxy>() {
            @Override
            protected void onRangeChanged(HasData<Term2TermProxy> display) {
                 dataProvider.refreshWithChildTerms(display,(Term2TermProxy)value);
            }
        };
        return new DefaultNodeInfo<Term2TermProxy>(provider,ontologyCell,selectionModel, selectionManager,null);
    }

    @Override
    public boolean isLeaf(Object value) {
        Term2TermProxy term = (Term2TermProxy)value;
        return value != null && term.getChild().getChildCount() == 0;
    }
}
