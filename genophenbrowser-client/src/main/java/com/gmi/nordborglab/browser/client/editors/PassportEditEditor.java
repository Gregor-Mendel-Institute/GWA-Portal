package com.gmi.nordborglab.browser.client.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PassportEditEditor extends Composite {

	private static PassportEditEditorUiBinder uiBinder = GWT
			.create(PassportEditEditorUiBinder.class);

	interface PassportEditEditorUiBinder extends
			UiBinder<Widget, PassportEditEditor> {
	}

	public PassportEditEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
