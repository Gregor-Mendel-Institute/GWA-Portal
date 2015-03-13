package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;

/**
 * Created by uemit.seren on 11/17/14.
 */
public class BarCell extends AbstractSafeHtmlCell<Number> {

    public BarCell() {
        super(BarChartCellRenderer.getInstance());
    }

    public BarCell(double height, String color) {
        super(new BarChartCellRenderer(height, color));
    }

    @Override
    protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        if (data != null)
            sb.append(data);
    }

    public static class BarChartCellRenderer extends AbstractSafeHtmlRenderer<Number> {

        private double height = 1.17;
        private String color = "#058dc7";

        public BarChartCellRenderer() {
        }

        public BarChartCellRenderer(double height, String color) {
            this.height = height;
            this.color = color;
        }

        static interface Template extends SafeHtmlTemplates {
            @Template("<div style=\"width:{0}%;background-color: {1};display: inline;float: left;height: {2}em;margin: 0 10px 0 0;min-width: 1px;\"></div>")
            SafeHtml bar(Double percentage, String color, Double height);
        }

        private static Template template = GWT.create(Template.class);
        private static BarChartCellRenderer instance = null;

        public static BarChartCellRenderer getInstance() {
            if (instance == null) {
                instance = new BarChartCellRenderer();
            }
            return instance;
        }

        @Override
        public SafeHtml render(Number object) {
            SafeHtml output = null;
            double value = 0D;
            if (object != null)
                value = object.doubleValue();
            output = template.bar(value, color, height);
            return output;
        }
    }
}
