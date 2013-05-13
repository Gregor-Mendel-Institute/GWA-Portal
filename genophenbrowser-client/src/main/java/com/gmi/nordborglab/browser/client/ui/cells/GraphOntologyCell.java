package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphOntologyCell extends AbstractCell<GraphTerm2TermProxy> {


    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb,String name,String count,String relationship,String relationshipClass);
        MyStyle getStyle();
    }

    private static String ICON_OK = "icon-ok";
    private final Renderer uiRenderer;
    private boolean menuOpened = false;

    @Inject
    public GraphOntologyCell(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }

    @Override
    public void render(Context context, GraphTerm2TermProxy value, SafeHtmlBuilder sb) {
        GraphTermProxy term = value.getChild();
        String relationship =  value.getType();
        String name = term.getName();
        String count = String.valueOf(term.getChildCount());
        String relationshipClass = "";
        if ("is_a".equalsIgnoreCase(relationship)) {
            relationshipClass = "badge badge-info";
        }else if ("part_of".equalsIgnoreCase(relationship)) {
            relationshipClass = "badge badge-success";
        }
        uiRenderer.render(sb,name,count,relationship,relationshipClass);
    }
}