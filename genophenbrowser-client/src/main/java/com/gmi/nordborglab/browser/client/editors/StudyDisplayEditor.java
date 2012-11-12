package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StudyDisplayEditor extends Composite implements Editor<StudyProxy> {

	private static StudyDisplayEditorUiBinder uiBinder = GWT
			.create(StudyDisplayEditorUiBinder.class);

	interface StudyDisplayEditorUiBinder extends
			UiBinder<Widget, StudyDisplayEditor> {
	}

	public StudyDisplayEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField Label name;
	@UiField Label producer;
	@Path("protocol.analysisMethod")@UiField Label protocol;
	@UiField DateLabel studyDate;
	@Path("alleleAssay.name")	@UiField Label genotype;
}
