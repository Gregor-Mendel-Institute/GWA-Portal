package com.gmi.nordborglab.browser.client.editors;

import java.util.List;

import com.gmi.nordborglab.browser.client.ui.ValidationValueBoxEditorDecorator;
import com.gmi.nordborglab.browser.client.ui.ValidationValueListEditorDecorator;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;

public class StudyCreateEditor extends Composite implements Editor<StudyProxy>{

	private static StudyCreateEditorUiBinder uiBinder = GWT
			.create(StudyCreateEditorUiBinder.class);

	interface StudyCreateEditorUiBinder extends
			UiBinder<Widget, StudyCreateEditor> {
	}
	
	@UiField  ValidationValueBoxEditorDecorator<String> name; 
	@UiField  ValidationValueBoxEditorDecorator<String> producer;
	@UiField ValidationValueListEditorDecorator<StudyProtocolProxy> protocol;
	@Ignore private ValueListBox<StudyProtocolProxy> protocolListBox;
	@Ignore private ValueListBox<AlleleAssayProxy> genotypeListBox;
	@Path("alleleAssay") @UiField ValidationValueListEditorDecorator<AlleleAssayProxy> genotype;
	

	public StudyCreateEditor() {
		protocolListBox = new ValueListBox<StudyProtocolProxy>(new ProxyRenderer<StudyProtocolProxy>(null) {

			@Override
			public String render(StudyProtocolProxy object) {
				return object == null ? "" : object.getAnalysisMethod();
			}
			
		}, new EntityProxyKeyProvider<StudyProtocolProxy>());
		genotypeListBox = new ValueListBox<AlleleAssayProxy>(new ProxyRenderer<AlleleAssayProxy>(null) {

			@Override
			public String render(AlleleAssayProxy object) {
				return object == null ? "": object.getName();
			}
		}, new EntityProxyKeyProvider<AlleleAssayProxy>());
		initWidget(uiBinder.createAndBindUi(this));
		protocol.setValueListBox(protocolListBox);
		genotype.setValueListBox(genotypeListBox);
	}
	
	public void setAcceptableValues(List<StudyProtocolProxy> protocolValues,List<AlleleAssayProxy> genotypeValues) {
		protocolListBox.setAcceptableValues(protocolValues);
		genotypeListBox.setAcceptableValues(genotypeValues);
	}
	
	public HandlerRegistration addGenotypeChangeHandler(ValueChangeHandler<AlleleAssayProxy> handler)  {
		return genotypeListBox.addValueChangeHandler(handler);
	}

}
