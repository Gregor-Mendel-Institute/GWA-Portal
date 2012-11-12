package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import java.util.ArrayList;
import java.util.List;

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
	
	@UiField ResizeableFlowPanel container;
	protected final DataSource geneDataSource;
	private String[] colors = {"blue", "green", "red", "cyan", "purple"};
	private String[] gene_mark_colors = {"red", "red", "blue", "red", "green"};
	protected List<GWASGeneViewer> gwasGeneViewers = new ArrayList<GWASGeneViewer>();

	@Inject
	public StudyGWASPlotView(final Binder binder, final DataSource geneDataSource) {
		widget = binder.createAndBindUi(this);
		this.geneDataSource = geneDataSource;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void drawGWASPlots(GWASDataDTO gwasData) {
		Integer i = 1;
		java.util.Iterator<DataTable> iterator = gwasData.getGwasDataTables().iterator();
//		int minWidth = 600;
//		int width = container.getOffsetWidth() - 60;
//		if (width < minWidth)
//			width = Window.getClientWidth() - 160 - 49;
//		if (width < minWidth)
//			width = minWidth;
		
		while(iterator.hasNext())
		{
			GWASGeneViewer chart =null;
			DataTable dataTable = iterator.next();
			String[] color = new String[] {colors[i%colors.length]};
			String gene_marker_color = gene_mark_colors[i%gene_mark_colors.length];
			if (gwasGeneViewers.size() >= i)
				chart = gwasGeneViewers.get((i-1));
			if (chart == null)
			{
				chart = new GWASGeneViewer("Chr"+i.toString(), color, gene_marker_color, geneDataSource,null);
				gwasGeneViewers.add(chart);
				chart.setGeneInfoUrl("http://arabidopsis.org/servlets/TairObject?name={0}&type=gene");
				container.add((IsWidget)chart);
				chart.addSelectionHandler(new SelectHandler() {

					@Override
					public void onSelect(SelectEvent event) {
						DataPoint point = event.point;
						Event mouseEvent = event.event;
						String id = event.id;
						int chromosome;
						try
						{
							chromosome = Integer.parseInt(id);
						}
						catch (Exception e)
						{
							chromosome =Integer.parseInt(id.charAt(3)+"");
						}
						//getUiHandlers().onSelectSNP(chromosome,(int)point.getXVal(),mouseEvent.getClientX(),mouseEvent.getClientY());
					}
					
				});
			}
			chart.clearDisplayGenes();
			chart.clearSelection();
			chart.draw(dataTable,gwasData.getMaxScore(),0,gwasData.getChrLengths().get(i-1),gwasData.getBonferroniThreshold());
			i++;
		}
	}
}
