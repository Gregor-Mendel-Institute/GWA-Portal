package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.client.ui.BaseTabContainerView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StudyTabView extends BaseTabContainerView implements StudyTabPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, StudyTabView> {
	}

	@Inject
	public StudyTabView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot ==  StudyTabPresenter.TYPE_SetTabContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}
}
