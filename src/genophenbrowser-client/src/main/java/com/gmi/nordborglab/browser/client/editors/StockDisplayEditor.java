package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StockDisplayEditor extends Composite implements Editor<StockProxy> {

	private static StockDisplayEditorUiBinder uiBinder = GWT
			.create(StockDisplayEditorUiBinder.class);

	interface StockDisplayEditorUiBinder extends
			UiBinder<Widget, StockDisplayEditor> {
	}

	public StockDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Path("passport.accename")
	@UiField Label passport;
	
	@Path("generation.comments")
	@UiField Label generation;
	@UiField Label seedLot;
	@UiField Label stockSource;
	@UiField Label comments;
	

}
