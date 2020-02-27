package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.ui.AccessLabel;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StudyDisplayEditor extends Composite implements Editor<StudyProxy> {

    private static StudyDisplayEditorUiBinder uiBinder = GWT
            .create(StudyDisplayEditorUiBinder.class);

    interface StudyDisplayEditorUiBinder extends
            UiBinder<Widget, StudyDisplayEditor> {
    }

    @UiField
    Label name;

    @UiField
    Label producer;
    @Path("protocol.analysisMethod")
    @UiField
    Label protocol;
    @UiField
    DateLabel studyDate;
    @Path("alleleAssay.name")
    @UiField
    Label genotype;
    @UiField
    @Path("public")
    AccessLabel access;
    @UiField(provided = true)
    AvatarOwnerDisplayEditor ownerUser;

    @Inject
    public StudyDisplayEditor(final AvatarOwnerDisplayEditor ownerUser) {
        this.ownerUser = ownerUser;
        initWidget(uiBinder.createAndBindUi(this));
    }

}
