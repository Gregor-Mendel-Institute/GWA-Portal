package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.09.13
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListEditEditor extends Composite implements Editor<CandidateGeneListProxy> {

    interface CandidateGeneListEditEditorUiBinder extends UiBinder<Widget, CandidateGeneListEditEditor> {
    }

    private static CandidateGeneListEditEditorUiBinder ourUiBinder = GWT.create(CandidateGeneListEditEditorUiBinder.class);


    @UiField
    TextBox name;
    @UiField
    TextArea description;

    public CandidateGeneListEditEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}