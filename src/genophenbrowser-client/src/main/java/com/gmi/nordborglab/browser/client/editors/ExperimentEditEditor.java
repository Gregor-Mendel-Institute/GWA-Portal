package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

public class ExperimentEditEditor  extends Composite implements Editor<ExperimentProxy> {

	private static ExperimentEditorUiBinder uiBinder = GWT
			.create(ExperimentEditorUiBinder.class);

	interface ExperimentEditorUiBinder extends
			UiBinder<Widget, ExperimentEditEditor> {
	}

	@UiField
    TextBox name;
    @UiField
    TextBox originator;
    @UiField
    TextArea design;
    @UiField
    TextArea comments;

	public ExperimentEditEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	

}
