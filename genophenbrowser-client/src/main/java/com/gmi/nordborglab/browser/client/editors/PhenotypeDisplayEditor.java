package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PhenotypeDisplayEditor extends Composite implements Editor<PhenotypeProxy>{

	private static PhenotypeDisplayEditorUiBinder uiBinder = GWT
			.create(PhenotypeDisplayEditorUiBinder.class);

	interface PhenotypeDisplayEditorUiBinder extends
			UiBinder<Widget, PhenotypeDisplayEditor> {
	}

	public PhenotypeDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField Label localTraitName;
	@UiField Label toAccession;
	@UiField Label eoAccession;
	@Path("unitOfMeasure.unitType")	@UiField Label unitOfMeasure;
	@UiField Label traitProtocol;

}
