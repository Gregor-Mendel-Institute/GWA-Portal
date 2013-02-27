package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import java.util.ArrayList;
import java.util.List;

import com.gmi.nordborglab.browser.client.mvp.presenter.home.BasicStudyWizardPresenter;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import org.danvk.dygraphs.client.events.DataPoint;
import org.danvk.dygraphs.client.events.SelectHandler;
import org.danvk.dygraphs.client.events.SelectHandler.SelectEvent;

import at.gmi.nordborglab.widgets.geneviewer.client.datasource.DataSource;
import at.gmi.nordborglab.widgets.gwasgeneviewer.client.GWASGeneViewer;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyGWASPlotPresenter;
import com.gmi.nordborglab.browser.client.ui.ResizeableFlowPanel;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.inject.Inject;

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
    public void setInSlot(Object slot, Widget content) {
        if (slot == StudyGWASPlotPresenter.TYPE_SetGWASPlotsContent) {
            gwasPlotContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

}
