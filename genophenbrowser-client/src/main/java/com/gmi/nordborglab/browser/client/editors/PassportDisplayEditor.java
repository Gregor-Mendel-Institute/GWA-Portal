package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PassportDisplayEditor extends Composite implements Editor<PassportProxy>{

	private static PassportDisplayEditorUiBinder uiBinder = GWT
			.create(PassportDisplayEditorUiBinder.class);

	interface PassportDisplayEditorUiBinder extends
			UiBinder<Widget, PassportDisplayEditor> {
	}

	public PassportDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField Label accename;
	@UiField Label accenumb;
	
	@Path("sampstat.germplasmType")
	@UiField Label sampstatName;
	
	@Path("source.source")
	@UiField Label source;
	
	@Path("collection.collector")
	@UiField Label collectionName;
	
	@UiField Label comments;

}
