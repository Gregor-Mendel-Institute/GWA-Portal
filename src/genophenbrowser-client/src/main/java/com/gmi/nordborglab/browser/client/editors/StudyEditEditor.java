package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ValueListBox;

import java.util.List;

public class StudyEditEditor extends Composite implements Editor<StudyProxy> {

    private static StudyEditEditorUiBinder uiBinder = GWT
            .create(StudyEditEditorUiBinder.class);

    interface StudyEditEditorUiBinder extends UiBinder<Widget, StudyEditEditor> {
    }


    @UiField
    TextBox name;
    @UiField
    TextBox producer;
    @UiField(provided = true)
    ValueListBox<StudyProtocolProxy> protocol;

    @Path("alleleAssay.name")
    @UiField
    Label genotype;

    public StudyEditEditor() {
        protocol = new ValueListBox<>(new ProxyRenderer<StudyProtocolProxy>(null) {

            @Override
            public String render(StudyProtocolProxy object) {
                return object == null ? "" : object.getAnalysisMethod();
            }

        }, new EntityProxyKeyProvider<StudyProtocolProxy>());
        protocol.getElement().setId("protocol");
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setAcceptableValues(List<StudyProtocolProxy> protocolValues, List<AlleleAssayProxy> genotypeValues) {
        protocol.setAcceptableValues(protocolValues);
    }

}
