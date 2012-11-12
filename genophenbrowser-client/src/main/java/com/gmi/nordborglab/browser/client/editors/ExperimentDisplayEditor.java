package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ExperimentDisplayEditor extends Composite implements Editor<ExperimentProxy> {

	private static ExperimentDisplayEditorUiBinder uiBinder = GWT
			.create(ExperimentDisplayEditorUiBinder.class);

	interface ExperimentDisplayEditorUiBinder extends
			UiBinder<Widget, ExperimentDisplayEditor> {
	}
	@UiField Label name;
	@UiField Label design;
	@UiField Label originator;
	@UiField Label comments;
	

	public ExperimentDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
