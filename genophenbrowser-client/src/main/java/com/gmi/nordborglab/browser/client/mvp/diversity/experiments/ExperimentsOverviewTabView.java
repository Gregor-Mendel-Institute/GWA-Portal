package com.gmi.nordborglab.browser.client.mvp.diversity.experiments;

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
        bindSlot(ExperimentsOverviewTabPresenter.SLOT_CONTENT, tabContainer.getPanelContent());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

}
