package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TaxonomyDisplayEditor extends Composite implements Editor<TaxonomyProxy>{

	private static TaxonomyDisplayEditorUiBinder uiBinder = GWT
			.create(TaxonomyDisplayEditorUiBinder.class);

	interface TaxonomyDisplayEditorUiBinder extends
			UiBinder<Widget, TaxonomyDisplayEditor> {
	}

	public TaxonomyDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField Label genus;
	@UiField Label species;
	@UiField Label subspecies;
	@UiField Label subtaxa;
	@UiField Label race;
	@UiField Label population;
	@UiField Label commonName;
	@UiField Label termAccession;
	

}
