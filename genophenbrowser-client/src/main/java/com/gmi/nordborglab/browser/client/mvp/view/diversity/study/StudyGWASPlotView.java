package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyGWASPlotPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class StudyGWASPlotView extends ViewImpl implements
        StudyGWASPlotPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyGWASPlotView> {
    }

    @UiField
    SimpleLayoutPanel gwasPlotContainer;

    @Inject
    public StudyGWASPlotView(final Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == StudyGWASPlotPresenter.TYPE_SetGWASPlotsContent) {
            gwasPlotContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

}
