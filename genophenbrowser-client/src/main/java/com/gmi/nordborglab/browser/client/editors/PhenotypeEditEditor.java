package com.gmi.nordborglab.browser.client.editors;

import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.ui.OntologyTermSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.OntologyTypeahead;
import com.gmi.nordborglab.browser.client.ui.ValidationValueListBox;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
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

import javax.annotation.Nullable;
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
    ValidationValueListBox<UnitOfMeasureProxy> unitOfMeasure;
    @UiField(provided = true)
    OntologyTypeahead traitOntologyTerm;
    @UiField(provided = true)
    OntologyTypeahead environOntologyTerm;

    private OntologySearchCallback searchCallback;
    private OntologyManager ontologyManager;


    public PhenotypeEditEditor() {
        unitOfMeasure = new ValidationValueListBox<UnitOfMeasureProxy>(new ProxyRenderer<UnitOfMeasureProxy>(null) {

            @Override
            public String render(UnitOfMeasureProxy object) {
                return object == null ? "" : object.getUnitType();
            }

        }, new EntityProxyKeyProvider<UnitOfMeasureProxy>());
        traitOntologyTerm = new OntologyTypeahead(new OntologyTermSuggestOracle() {

            @Override
            public void requestDefaultSuggestions(SuggestOracle.Request request, Callback callback) {
                onSearchOntology(new OntologyTermSuggestOracle.Request(), callback, ConstEnums.ONTOLOGY_TYPE.TRAIT);
            }

            @Override
            public void requestSuggestions(SuggestOracle.Request request, Callback callback) {
                onSearchOntology(request, callback, ConstEnums.ONTOLOGY_TYPE.TRAIT);
            }
        });
        environOntologyTerm = new OntologyTypeahead(new OntologyTermSuggestOracle() {

            @Override
            public void requestDefaultSuggestions(SuggestOracle.Request request, Callback callback) {
                onSearchOntology(new OntologyTermSuggestOracle.Request(), callback, ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT);
            }

            @Override
            public void requestSuggestions(SuggestOracle.Request request, Callback callback) {
                onSearchOntology(request, callback, ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void onSearchOntology(final SuggestOracle.Request request, final SuggestOracle.Callback callback, final ConstEnums.ONTOLOGY_TYPE type) {
        if (ontologyManager == null)
            return;
        ontologyManager.findByQuery(new Receiver<TermPageProxy>() {
            @Override
            public void onSuccess(TermPageProxy termPage) {
                SuggestOracle.Response response = new SuggestOracle.Response();
                response.setMoreSuggestionsCount((int) termPage.getTotalElements() - request.getLimit());
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
                response.setSuggestions(Lists.transform(termPage.getContents(), new Function<TermProxy, SuggestOracle.Suggestion>() {
                    @Nullable
                    @Override
                    public SuggestOracle.Suggestion apply(@Nullable TermProxy termProxy) {
                        return new OntologyTermSuggestOracle.OntologySuggestion(termProxy);
                    }
                }));
                callback.onSuggestionsReady(request, response);
            }
        }, request.getQuery(), type, request.getLimit());
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
