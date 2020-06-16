package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TaxonomyEditEditor extends Composite implements Editor<TaxonomyProxy> {

	private static TaxonomyEditEditorUiBinder uiBinder = GWT
			.create(TaxonomyEditEditorUiBinder.class);

	interface TaxonomyEditEditorUiBinder extends
			UiBinder<Widget, TaxonomyEditEditor> {
	}
	
	@UiField  ValueBoxEditorDecorator<String> genus; 
	@UiField  ValueBoxEditorDecorator<String> species;
	@UiField  ValueBoxEditorDecorator<String> subspecies; 
	@UiField  ValueBoxEditorDecorator<String> subtaxa;
	@UiField  ValueBoxEditorDecorator<String> race; 
	@UiField  ValueBoxEditorDecorator<String> population;
	@UiField  ValueBoxEditorDecorator<String> commonName;
	@UiField  ValueBoxEditorDecorator<String> termAccession;

	public TaxonomyEditEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
