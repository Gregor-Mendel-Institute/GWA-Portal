package com.gmi.nordborglab.browser.client.editors;

import java.util.Collection;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;

public class PhenotypeEditEditor extends Composite implements Editor<PhenotypeProxy>{

	private static PhenotypeEditEditorUiBinder uiBinder = GWT
			.create(PhenotypeEditEditorUiBinder.class);

	interface PhenotypeEditEditorUiBinder extends
			UiBinder<Widget, PhenotypeEditEditor> {
	}
	
	@UiField  ValueBoxEditorDecorator<String> localTraitName; 
	@UiField  ValueBoxEditorDecorator<String> traitProtocol;
	@UiField(provided=true) ValueListBox<UnitOfMeasureProxy> unitOfMeasure;
	@UiField ValueBoxEditorDecorator<String> toAccession;
	@UiField ValueBoxEditorDecorator<String> eoAccession;

	public PhenotypeEditEditor() {
		unitOfMeasure = new ValueListBox<UnitOfMeasureProxy>(new ProxyRenderer<UnitOfMeasureProxy>(null) {

			@Override
			public String render(UnitOfMeasureProxy object) {
				return object == null ? "" : object.getUnitType();
			}
			
		}, new EntityProxyKeyProvider<UnitOfMeasureProxy>());
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void setAcceptableValuesForUnitOfMeasure(Collection<UnitOfMeasureProxy> values) {
		unitOfMeasure.setAcceptableValues(values);
	}
	

}
