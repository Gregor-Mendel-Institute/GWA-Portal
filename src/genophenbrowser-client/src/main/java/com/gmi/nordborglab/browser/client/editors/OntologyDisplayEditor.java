package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 8:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyDisplayEditor extends Composite implements Editor<TermProxy> {

    private static OntologyDisplayEditorUiBinder uiBinder = GWT
            .create(OntologyDisplayEditorUiBinder.class);

    interface OntologyDisplayEditorUiBinder extends
            UiBinder<Widget, OntologyDisplayEditor> {
    }

    @UiField
    Label name;
    @Path("termDefinition.termComment")
    @UiField
    Label comment;
    @Path("termDefinition.termDefinition")
    @UiField
    Label definition;

    @UiField
    Label acc;
    @UiField
    Label termType;


    public OntologyDisplayEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
