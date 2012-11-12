package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentPermissionPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentPermissionPresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExperimentPermissionView extends ViewImpl implements
		ExperimentPermissionPresenter.MyView {

	private final Widget widget;
	@UiField SimpleLayoutPanel container;

	public interface Binder extends UiBinder<Widget, ExperimentPermissionView> {
	}

	@Inject
	public ExperimentPermissionView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == ExperimentPermissionPresenter.TYPE_SetMainContent){
			container.clear();
			container.add(content);
		}
		else {
			super.setInSlot(slot, content);
		}
	}
}
