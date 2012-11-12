package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

public class ResizeableColumnChart extends ColumnChart implements
		RequiresResize {

	protected Options options;
	protected AbstractDataTable data;
	
	public ResizeableColumnChart(AbstractDataTable data, Options options) {
		super(data, options);
		this.data = data;
		this.options = options;
	}
	
	
	@Override
	public void onResize() {
		options.setWidth(getParent().getOffsetWidth());
		options.setHeight(getParent().getOffsetHeight());
		draw(data, options);
	}


	public void draw2(AbstractDataTable data2, Options options) {
		this.data = data2;
		this.options = options;
		draw(data2,options);
	}

}
