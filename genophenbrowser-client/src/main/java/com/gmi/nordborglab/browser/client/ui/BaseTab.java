package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;

public abstract class BaseTab extends Composite implements Tab {

	protected interface Style extends CssResource {
		String active();

		String inactive();
	}

	@UiField
	Hyperlink hyperlink;

	@UiField
	Style style;

	private final float priority;
	protected boolean canUserAccess = true;

	public BaseTab(TabData tabData) {
		super();
		if (tabData instanceof TabDataDynamic) {
			canUserAccess = ((TabDataDynamic)tabData).hasAccess();
		}
		this.priority = tabData.getPriority();
	}

	@Override
	public void activate() {
		hyperlink.removeStyleName(style.inactive());
		hyperlink.addStyleName(style.active());
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void deactivate() {
		hyperlink.removeStyleName(style.active());
		hyperlink.addStyleName(style.inactive());
	}

	@Override
	public float getPriority() {
		return priority;
	}

	@Override
	public String getText() {
		return hyperlink.getText();
	}

	@Override
	public void setTargetHistoryToken(String historyToken) {
		 hyperlink.setTargetHistoryToken(historyToken);
	}

	@Override
	public void setText(String text) {
		hyperlink.setText(text);
	}
	
	public boolean canUserAccess() {
	    return canUserAccess;
	}
	
	public void setCanUserAccess(boolean canUserAccess) {
		this.canUserAccess = canUserAccess;
	}

}
