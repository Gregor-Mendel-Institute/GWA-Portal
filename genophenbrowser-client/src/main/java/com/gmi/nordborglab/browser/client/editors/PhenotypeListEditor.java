package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.common.collect.Lists;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.List;

/**
 * Created by uemit.seren on 7/1/14.
 */
public class PhenotypeListEditor extends Composite implements IsEditor<ListEditor<PhenotypeProxy, PhenotypeEditEditor>> {


    interface Binder extends UiBinder<Widget, PhenotypeListEditor> {
    }

    private class PhenotypeEditorSource extends EditorSource<PhenotypeEditEditor> {

        @Override
        public PhenotypeEditEditor create(int index) {
            PhenotypeEditEditor subEditor = new PhenotypeEditEditor();
            editors.add(subEditor);
            return subEditor;
        }

        @Override
        public void dispose(PhenotypeEditEditor subEditor) {
            editors.remove(subEditor);
        }

        @Override
        public void setIndex(PhenotypeEditEditor editor, int index) {
            editors.add(index, editor);
        }
    }

    @UiField
    SimpleLayoutPanel container;
    private final List<PhenotypeEditEditor> editors = Lists.newArrayList();
    private ListEditor<PhenotypeProxy, PhenotypeEditEditor> editor = ListEditor.of(new PhenotypeEditorSource());

    @Inject
    public PhenotypeListEditor(Binder binder) {
        initWidget(binder.createAndBindUi(this));
    }

    @Override
    public ListEditor<PhenotypeProxy, PhenotypeEditEditor> asEditor() {
        return editor;
    }

}