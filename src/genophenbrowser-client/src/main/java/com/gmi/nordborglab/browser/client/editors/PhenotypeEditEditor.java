package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.ui.OntologyTypeahead;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;
import com.google.web.bindery.requestfactory.shared.Receiver;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Suggestion;
import org.gwtbootstrap3.extras.typeahead.client.base.SuggestionCallback;

import java.util.Collection;

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
    @UiField(provided = true)
    OntologyTypeahead traitOntologyTerm;
    @UiField(provided = true)
    OntologyTypeahead environOntologyTerm;

    private OntologySearchCallback searchCallback;
    private OntologyManager ontologyManager;


    public PhenotypeEditEditor() {
        unitOfMeasure = new ValueListBox(new ProxyRenderer<UnitOfMeasureProxy>(null) {

            @Override
            public String render(UnitOfMeasureProxy object) {
                return object == null ? "" : object.getUnitType();
            }

        }, new EntityProxyKeyProvider<UnitOfMeasureProxy>());
        traitOntologyTerm = new OntologyTypeahead(new Dataset<TermProxy>() {

            @Override
            public void findMatches(String query, SuggestionCallback<TermProxy> callback) {
                onSearchOntology(query, callback, ConstEnums.ONTOLOGY_TYPE.TRAIT);
            }
        });
        environOntologyTerm = new OntologyTypeahead(new Dataset<TermProxy>() {

            @Override
            public void findMatches(String query, SuggestionCallback<TermProxy> callback) {
                onSearchOntology(query, callback, ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void onSearchOntology(final String request, final SuggestionCallback<TermProxy> callback, final ConstEnums.ONTOLOGY_TYPE type) {
        if (ontologyManager == null)
            return;
        ontologyManager.findByQuery(new Receiver<TermPageProxy>() {
            @Override
            public void onSuccess(TermPageProxy termPage) {
                Collection<Suggestion<TermProxy>> suggestions = Lists.newArrayList();
                if (termPage != null) {
                    switch (type) {
                        case TRAIT:
                            if (traitOntologyTerm.getValue() != null) {
                                termPage.getContents().add(0, traitOntologyTerm.getValue());
                            }
                            break;
                        case ENVIRONMENT:
                            if (environOntologyTerm.getValue() != null) {
                                termPage.getContents().add(0, environOntologyTerm.getValue());
                            }
                            break;
                    }
                    for (TermProxy term : termPage.getContents()) {
                        suggestions.add(Suggestion.create(term.getName() + " (" + term.getAcc() + ")", term, null));
                    }
                }
                callback.execute(suggestions);
            }
        }, request, type, 10);
    }

    public void setAcceptableValuesForUnitOfMeasure(Collection<UnitOfMeasureProxy> values) {
        unitOfMeasure.setAcceptableValues(values);
    }

    public HasValueChangeHandlers<UnitOfMeasureProxy> getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public HasChangeHandlers getLocalTraitName() {
        return localTraitName;
    }

    public void addChangeHandlerToTypeAhead(ChangeHandler changeHandler) {
        traitOntologyTerm.setChangeHandler(changeHandler);
    }

    public void setOntologyManager(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }
}
