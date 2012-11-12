package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailTabPresenter;
import com.gmi.nordborglab.browser.client.ui.BaseTabContainerView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExperimentDetailTabView extends BaseTabContainerView implements
		ExperimentDetailTabPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, ExperimentDetailTabView> {
	}
	
	@Inject
	public ExperimentDetailTabView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == ExperimentDetailTabPresenter.TYPE_SetTabContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}
	
}
