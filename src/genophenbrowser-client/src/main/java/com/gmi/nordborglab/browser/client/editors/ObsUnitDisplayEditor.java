package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Widget;

public class ObsUnitDisplayEditor extends Composite implements Editor<ObsUnitProxy>{

	private static ObsUnitDisplayEditorUiBinder uiBinder = GWT
			.create(ObsUnitDisplayEditorUiBinder.class);

	interface ObsUnitDisplayEditorUiBinder extends
			UiBinder<Widget, ObsUnitDisplayEditor> {
	}
	
	@UiField Label name;
	@Path("stock.id") @UiField LongBox stock;
	@Path("locality.localityName") @UiField Label locality;
	@UiField IntegerBox coordX;
	@UiField IntegerBox coordY;
	@UiField Label rep;
	@UiField Label block;
	@UiField Label plot;
	@UiField Label plant;
	@UiField Label season;
	@UiField DateLabel plantingDate;
	@UiField DateLabel harvestDate;
	@UiField Label comments;

	public ObsUnitDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
