package com.gmi.nordborglab.browser.client.ui.cells;

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
 * Date: 5/7/13
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyCell extends AbstractCell<Term2TermProxy> {


    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String name, String count, String relationship, String relationshipClass);

        MyStyle getStyle();
    }

    private static String ICON_OK = "fa fa-check";
    private final Renderer uiRenderer;
    private boolean menuOpened = false;

    @Inject
    public OntologyCell(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }

    @Override
    public void render(Context context, Term2TermProxy value, SafeHtmlBuilder sb) {
        TermProxy term = value.getChild();
        TermProxy relationShipTerm = value.getRelationshipType();
        String name = term.getName();
        String count = String.valueOf(term.getChildCount());
        String relationship = "";
        String relationshipClass = "";
        if (relationShipTerm != null) {
            relationship = relationShipTerm.getName();
            if ("is_a".equalsIgnoreCase(relationship)) {
                relationshipClass = "label label-info";
            } else if ("part_of".equalsIgnoreCase(relationship)) {
                relationshipClass = "label label-success";
            }
        }
        uiRenderer.render(sb, name, count, relationship, relationshipClass);
    }
}