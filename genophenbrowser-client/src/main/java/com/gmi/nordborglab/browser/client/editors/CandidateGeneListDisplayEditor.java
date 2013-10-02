package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.ui.AccessLabel;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.09.13
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListDisplayEditor extends Composite implements Editor<CandidateGeneListProxy> {

    interface CandiateGeneListDisplayEditorUiBinder extends UiBinder<Widget, CandidateGeneListDisplayEditor> {
    }

    private static CandiateGeneListDisplayEditorUiBinder ourUiBinder = GWT.create(CandiateGeneListDisplayEditorUiBinder.class);

    @UiField
    Label name;
    @UiField
    Label description;
    @UiField
    @Path("public")
    AccessLabel access;
    @UiField
    DateLabel created;
    @UiField
    DateLabel modified;
    @UiField
    DateLabel published;

    public CandidateGeneListDisplayEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}