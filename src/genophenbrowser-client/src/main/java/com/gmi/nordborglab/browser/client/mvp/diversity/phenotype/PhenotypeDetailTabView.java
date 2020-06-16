package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype;

import com.gmi.nordborglab.browser.client.ui.BaseTabContainerView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PhenotypeDetailTabView extends BaseTabContainerView implements
        PhenotypeDetailTabPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, PhenotypeDetailTabView> {
    }

    @Inject
    public PhenotypeDetailTabView(final Binder binder) {
        widget = binder.createAndBindUi(this);
        bindSlot(PhenotypeDetailTabPresenter.SLOT_CONTENT, tabContainer.getPanelContent());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


}
