package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewTabPresenter;
import com.gmi.nordborglab.browser.client.ui.BaseTabContainerView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExperimentsOverviewTabView extends BaseTabContainerView implements
		ExperimentsOverviewTabPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, ExperimentsOverviewTabView> {
	}
	
	
	@Inject
	public ExperimentsOverviewTabView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == ExperimentsOverviewTabPresenter.TYPE_SetTabContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}
}
