package com.gmi.nordborglab.browser.client.mvp.view.home.dashboard;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.dashboard.DashboardPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DashboardView extends ViewImpl implements
		DashboardPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, DashboardView> {
	}

	@Inject
	public DashboardView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
