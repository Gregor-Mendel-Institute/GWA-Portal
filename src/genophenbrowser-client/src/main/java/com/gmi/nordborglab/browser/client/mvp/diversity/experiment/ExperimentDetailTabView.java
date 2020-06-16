package com.gmi.nordborglab.browser.client.mvp.diversity.experiment;

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
        bindSlot(ExperimentDetailTabPresenter.SLOT_CONTENT, tabContainer.getPanelContent());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

}
