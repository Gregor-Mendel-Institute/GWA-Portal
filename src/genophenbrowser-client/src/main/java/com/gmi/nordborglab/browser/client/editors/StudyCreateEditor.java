package com.gmi.nordborglab.browser.client.editors;


import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ValueListBox;

import java.util.List;

public class StudyCreateEditor extends Composite implements Editor<StudyProxy> {

    private static StudyCreateEditorUiBinder uiBinder = GWT
            .create(StudyCreateEditorUiBinder.class);

    interface StudyCreateEditorUiBinder extends
            UiBinder<Widget, StudyCreateEditor> {
    }

    @UiField
    TextBox name;
    @UiField
    TextBox producer;
    @UiField(provided = true)
    ValueListBox<StudyProtocolProxy> protocol;
    @Path("alleleAssay")
    @UiField(provided = true)
    ValueListBox<AlleleAssayProxy> genotype;


    public StudyCreateEditor() {
        protocol = new ValueListBox<>(new ProxyRenderer<StudyProtocolProxy>(null) {

            @Override
            public String render(StudyProtocolProxy object) {
                return object == null ? "" : object.getAnalysisMethod();
            }

        }, new EntityProxyKeyProvider<StudyProtocolProxy>());
        genotype = new ValueListBox<>(new ProxyRenderer<AlleleAssayProxy>(null) {

            @Override
            public String render(AlleleAssayProxy object) {
                return object == null ? "" : object.getName();
            }
        }, new EntityProxyKeyProvider<AlleleAssayProxy>());
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setAcceptableValues(List<StudyProtocolProxy> protocolValues, List<AlleleAssayProxy> genotypeValues) {
        protocol.setAcceptableValues(protocolValues);
        genotype.setAcceptableValues(genotypeValues);
    }

    public HandlerRegistration addGenotypeChangeHandler(ValueChangeHandler<AlleleAssayProxy> handler) {
        return genotype.addValueChangeHandler(handler);
    }

}
