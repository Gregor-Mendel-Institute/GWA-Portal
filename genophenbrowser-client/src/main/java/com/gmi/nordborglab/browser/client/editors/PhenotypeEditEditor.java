package com.gmi.nordborglab.browser.client.editors;

import java.util.Collection;

import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.ValueListBox;
import com.gmi.nordborglab.browser.client.ui.OntologyTermSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.OntologyTypeahead;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;

public class PhenotypeEditEditor extends Composite implements Editor<PhenotypeProxy> {

    private static PhenotypeEditEditorUiBinder uiBinder = GWT
            .create(PhenotypeEditEditorUiBinder.class);

    interface PhenotypeEditEditorUiBinder extends
            UiBinder<Widget, PhenotypeEditEditor> {
    }

    public interface OntologySearchCallback {
        void onRequestSearch(SuggestOracle.Request request, SuggestOracle.Callback callback, ConstEnums.ONTOLOGY_TYPE type);
    }

    @UiField
    TextBox localTraitName;
    @UiField
    TextArea traitProtocol;
    @UiField(provided = true)
    ValueListBox<UnitOfMeasureProxy> unitOfMeasure;
    //@UiField TextBox toAccession;
    @UiField(provided = true)
    OntologyTypeahead traitOntologyTerm;
    //@UiField TextBox eoAccession;
    @UiField(provided = true)
    OntologyTypeahead environOntologyTerm;

    private OntologySearchCallback searchCallback;

    public PhenotypeEditEditor() {
        unitOfMeasure = new ValueListBox<UnitOfMeasureProxy>(new ProxyRenderer<UnitOfMeasureProxy>(null) {

            @Override
            public String render(UnitOfMeasureProxy object) {
                return object == null ? "" : object.getUnitType();
            }

        }, new EntityProxyKeyProvider<UnitOfMeasureProxy>());
        traitOntologyTerm = new OntologyTypeahead(new OntologyTermSuggestOracle() {

            @Override
            public void requestDefaultSuggestions(SuggestOracle.Request request, Callback callback) {
                if (searchCallback != null) {
                    searchCallback.onRequestSearch(new Request(), callback, ConstEnums.ONTOLOGY_TYPE.TRAIT);
                }
            }

            @Override
            public void requestSuggestions(SuggestOracle.Request request, Callback callback) {
                if (searchCallback != null) {
                    searchCallback.onRequestSearch(request, callback, ConstEnums.ONTOLOGY_TYPE.TRAIT);
                }
            }
        });
        environOntologyTerm = new OntologyTypeahead(new OntologyTermSuggestOracle() {

            @Override
            public void requestDefaultSuggestions(SuggestOracle.Request request, Callback callback) {
                if (searchCallback != null) {
                    searchCallback.onRequestSearch(new Request(), callback, ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT);
                }
            }

            @Override
            public void requestSuggestions(SuggestOracle.Request request, Callback callback) {
                if (searchCallback != null) {
                    searchCallback.onRequestSearch(request, callback, ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT);
                }
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setAcceptableValuesForUnitOfMeasure(Collection<UnitOfMeasureProxy> values) {
        unitOfMeasure.setAcceptableValues(values);
    }


    public void setOntologySearchCallback(OntologySearchCallback searchCallback) {
        this.searchCallback = searchCallback;
    }
}
