package com.gmi.nordborglab.browser.client.editors;

import java.util.List;

import com.gmi.nordborglab.browser.client.ui.ValidationValueBoxEditorDecorator;
import com.gmi.nordborglab.browser.client.ui.ValidationValueListEditorDecorator;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;

public class StudyEditEditor extends Composite implements Editor<StudyProxy>{

	private static StudyEditEditorUiBinder uiBinder = GWT
			.create(StudyEditEditorUiBinder.class);

	interface StudyEditEditorUiBinder extends UiBinder<Widget, StudyEditEditor> {
	}


	@UiField  ValidationValueBoxEditorDecorator<String> name; 
	@UiField  ValidationValueBoxEditorDecorator<String> producer;
	@UiField ValidationValueListEditorDecorator<StudyProtocolProxy> protocol;
	@Ignore private ValueListBox<StudyProtocolProxy> protocolListBox;
	@Path("alleleAssay.name")	@UiField Label genotype;

	public StudyEditEditor() {
		protocolListBox = new ValueListBox<StudyProtocolProxy>(new ProxyRenderer<StudyProtocolProxy>(null) {

			@Override
			public String render(StudyProtocolProxy object) {
				return object == null ? "" : object.getAnalysisMethod();
			}
			
		}, new EntityProxyKeyProvider<StudyProtocolProxy>());
				initWidget(uiBinder.createAndBindUi(this));
		protocol.setValueListBox(protocolListBox);
	}
	
	public void setAcceptableValues(List<StudyProtocolProxy> protocolValues,List<AlleleAssayProxy> genotypeValues) {
		protocolListBox.setAcceptableValues(protocolValues);
	}

}
