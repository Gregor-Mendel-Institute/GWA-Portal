package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyGWASPlotPresenter;
import com.gmi.nordborglab.browser.client.ui.SNPDetailPopup;
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
    private final SNPDetailPopup snpPopOver;

    @Inject
    public StudyGWASPlotView(final Binder binder, SNPDetailPopup snpPopOver) {
        this.snpPopOver = snpPopOver;
        widget = binder.createAndBindUi(this);
        snpPopOver.setAnimationEnabled(true);
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

    @Override
    public void showSNPPopUp(Long analysisId, SelectSNPEvent event) {
        snpPopOver.setDataPoint(analysisId, event.getChromosome(), event.getxVal());
        snpPopOver.setPopupPosition(event.getClientX(), event.getClientY() - 84 / 2);
        snpPopOver.show();
    }
}
