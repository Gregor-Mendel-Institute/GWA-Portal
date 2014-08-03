package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.visualizations.MotionChart;

public class ResizeableMotionChart extends MotionChart implements RequiresResize {

    public ResizeableMotionChart(AbstractDataTable data, MotionChart.Options options) {
        super(data, options);
    }


    @Override
    public void onResize() {
        if (options == null) {
            options = Options.create();
        }
        options.setWidth(getParent().getOffsetWidth());
        options.setHeight(getParent().getOffsetHeight());
        try {
            draw(dataTable, options);
        } catch (Exception e) {

        }
    }


    @Override
    protected void onLoad() {

    }
}
