package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.ui.AccessLabel;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExperimentDisplayEditor extends Composite implements Editor<ExperimentProxy> {

    private static ExperimentDisplayEditorUiBinder uiBinder = GWT
            .create(ExperimentDisplayEditorUiBinder.class);

    interface ExperimentDisplayEditorUiBinder extends
            UiBinder<Widget, ExperimentDisplayEditor> {
    }

    @UiField
    Label name;
    @UiField
    Label design;
    @UiField
    Label originator;
    @UiField
    Label comments;
    @UiField
    @Path("public")
    AccessLabel access;
    @UiField(provided = true)
    AvatarOwnerDisplayEditor ownerUser;

    @Inject
    public ExperimentDisplayEditor(final AvatarOwnerDisplayEditor ownerUser) {
        this.ownerUser = ownerUser;
        initWidget(uiBinder.createAndBindUi(this));
    }

}
