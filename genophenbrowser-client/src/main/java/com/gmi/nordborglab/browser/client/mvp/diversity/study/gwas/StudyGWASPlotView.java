package com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas;

import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.ui.PlotDownloadPopup;
import com.gmi.nordborglab.browser.client.ui.SNPDetailPopup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;

public class StudyGWASPlotView extends ViewImpl implements
        StudyGWASPlotPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyGWASPlotView> {

    }

    @UiField
    SimpleLayoutPanel gwasPlotContainer;
    private final SNPDetailPopup snpPopOver;
    private final Modal popUpPanel = new Modal();
    private final PlotDownloadPopup plotPanel = new PlotDownloadPopup(PlotDownloadPopup.PLOT_TYPE.STUDY);

    @Inject
    public StudyGWASPlotView(final Binder binder, SNPDetailPopup snpPopOver) {
        this.snpPopOver = snpPopOver;
        widget = binder.createAndBindUi(this);
        ModalBody modalBody = new ModalBody();
        modalBody.add(plotPanel);
        popUpPanel.add(modalBody);
        popUpPanel.setTitle("Download GWAS Plots");
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

    @Override
    public void setAnalysisId(Long id) {
        plotPanel.setId(id);
    }

    @UiHandler("downloadBtn")
    public void onClickDownloadBtn(ClickEvent e) {
        popUpPanel.show();

    }
}
