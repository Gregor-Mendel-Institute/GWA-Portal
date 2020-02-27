package com.gmi.nordborglab.browser.client.mvp.diversity.study;

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
        bindSlot(StudyTabPresenter.SLOT_CONTENT, tabContainer.getPanelContent());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }
}
