package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.collect.Lists;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;

/**
 * Created by uemit.seren on 7/1/14.
 */
public class PhenotypeUploadDataListEditor extends Composite implements IsEditor<ListEditor<PhenotypeUploadDataProxy, PhenotypeUploadDataListEditor.PhenotypeUploadDataEditEditor>> {

    public static class PhenotypeUploadDataEditEditor extends Composite implements Editor<PhenotypeUploadDataProxy> {

        protected PhenotypeEditEditor traitUom;
        protected List<HandlerRegistration> handlerRegistrations = Lists.newArrayList();

        private PhenotypeUploadDataEditEditor(OntologyManager ontologyManager, List<UnitOfMeasureProxy> unitOfMeasures, ChangeHandler changeHandler) {
            this.traitUom = new PhenotypeEditEditor();
            this.traitUom.setAcceptableValuesForUnitOfMeasure(unitOfMeasures);
            this.traitUom.setOntologyManager(ontologyManager);
            initWidget(traitUom);
            initHandlers(changeHandler);
        }

        private void initHandlers(ChangeHandler changeHandler) {
            handlerRegistrations.add(this.traitUom.getLocalTraitName().addChangeHandler(changeHandler));
            this.traitUom.addChangeHandlerToTypeAhead(changeHandler);
        }

        public void dispose() {
            for (HandlerRegistration handlerRegistration : this.handlerRegistrations) {
                handlerRegistration.removeHandler();
            }
            handlerRegistrations.clear();
        }
    }

    private class PhenotypeUploadDataEditorSource extends EditorSource<PhenotypeUploadDataEditEditor> {

        @Override
        public PhenotypeUploadDataEditEditor create(int index) {
            PhenotypeUploadDataEditEditor subEditor = new PhenotypeUploadDataEditEditor(ontologyManager, unitOfMeasures, changeHandler);
            editors.add(subEditor);
            return subEditor;
        }

        @Override
        public void dispose(PhenotypeUploadDataEditEditor subEditor) {
            subEditor.dispose();
            editors.remove(subEditor);
        }

        @Override
        public void setIndex(PhenotypeUploadDataEditEditor editor, int index) {
            editors.add(index, editor);
        }

    }

    private SimpleLayoutPanel container = new SimpleLayoutPanel();
    private List<PhenotypeUploadDataEditEditor> editors = Lists.newArrayList();
    private ListEditor<PhenotypeUploadDataProxy, PhenotypeUploadDataEditEditor> editor = ListEditor.of(new PhenotypeUploadDataEditorSource());
    protected final OntologyManager ontologyManager;
    protected final List<UnitOfMeasureProxy> unitOfMeasures;
    protected ChangeHandler changeHandler;

    public PhenotypeUploadDataListEditor(OntologyManager ontologyManager, List<UnitOfMeasureProxy> unitOfMeasures, ChangeHandler changeHandler) {
        this.ontologyManager = ontologyManager;
        this.unitOfMeasures = unitOfMeasures;
        this.changeHandler = changeHandler;
        initWidget(container);
    }

    public void showSubEditor(int index) {
        container.setWidget(editors.get(index));
    }

    @Override
    public ListEditor<PhenotypeUploadDataProxy, PhenotypeUploadDataEditEditor> asEditor() {
        return editor;
    }
}
