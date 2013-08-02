package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.ui.AccessLabel;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class PhenotypeDisplayEditor extends Composite implements Editor<PhenotypeProxy> {

    private static PhenotypeDisplayEditorUiBinder uiBinder = GWT
            .create(PhenotypeDisplayEditorUiBinder.class);

    interface PhenotypeDisplayEditorUiBinder extends
            UiBinder<Widget, PhenotypeDisplayEditor> {
    }

    static class OntologyRenderer extends AbstractRenderer<TermProxy> {

        @Override
        public String render(TermProxy object) {
            String ontology = "";
            if (object != null) {
                ontology = object.getName() + " (" + object.getAcc() + ")";
            }
            return ontology;
        }

    }

    public PhenotypeDisplayEditor() {
        traitOntologyTerm = new ValueLabel<TermProxy>(new OntologyRenderer());
        environOntologyTerm = new ValueLabel<TermProxy>(new OntologyRenderer());
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    Label localTraitName;
    //@UiField Label toAccession;
    @UiField(provided = true)
    ValueLabel<TermProxy> traitOntologyTerm;

    @UiField(provided = true)
    ValueLabel<TermProxy> environOntologyTerm;

    @Path("unitOfMeasure.unitType")
    @UiField
    Label unitOfMeasure;
    @UiField
    Label traitProtocol;
    @UiField
    @Path("public")
    AccessLabel access;


}
