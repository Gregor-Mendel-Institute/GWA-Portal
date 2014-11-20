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

    @Override
    protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        sb.append(data);
    }

    public static class BarChartCellRenderer extends AbstractSafeHtmlRenderer<Number> {

        static interface Template extends SafeHtmlTemplates {
            @Template("<div style=\"width:{0}%;background-color: #058dc7;display: inline;float: left;height: 1.17em;margin: 0 10px 0 0;min-width: 1px;\"></div>")
            SafeHtml bar(Double percentage);
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
            if (object != null) {
                output = template.bar(object.doubleValue());
            } else {
                output = template.bar(0D);
            }
            return output;
        }
    }
}
