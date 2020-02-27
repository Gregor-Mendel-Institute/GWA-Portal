package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

public class SimpleTab extends BaseTab {

	interface Binder extends UiBinder<Widget, SimpleTab> {
	}

	private static final Binder binder = GWT.create(Binder.class);

	private final Gatekeeper gatekeeper;

	@UiConstructor
	public SimpleTab(TabData tabData, Gatekeeper gatekeeper) {
		super(tabData);
		this.gatekeeper = gatekeeper;
		initWidget(binder.createAndBindUi(this));
		setText(tabData.getLabel());
	}

	@Override
	public boolean canUserAccess() {
		return (gatekeeper == null || gatekeeper.canReveal()) && canUserAccess;
	}

}
