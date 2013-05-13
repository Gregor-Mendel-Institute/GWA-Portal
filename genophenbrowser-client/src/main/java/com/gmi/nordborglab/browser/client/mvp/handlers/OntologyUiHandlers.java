package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.view.client.HasData;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OntologyUiHandlers extends UiHandlers{
    public void refreshWithChildTerms(HasData<GraphTerm2TermProxy> display, GraphTerm2TermProxy term);

    void onSelectTerm(GraphTerm2TermProxy selectedTerm);
}
