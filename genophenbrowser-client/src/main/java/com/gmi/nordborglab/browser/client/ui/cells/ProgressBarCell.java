package com.gmi.nordborglab.browser.client.ui.cells;

import com.github.gwtbootstrap.client.ui.Bar;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/11/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressBarCell extends AbstractCell<Number> {

    interface Template extends SafeHtmlTemplates {
        @Template("<div style=\"margin-bottom:0px;width:100px\"class=\"progress {0}\"><div class=\"bar\" style=\"width:{1}\"></div></div>")
        SafeHtml progressbar(String cssClass,String percentage);
    }

    private static Template template = GWT.create(Template.class);

    private boolean isStriped = false;
    private boolean isAnimated = false;
    private ProgressBarBase.Color color;

    public ProgressBarCell(boolean striped, boolean animated, ProgressBarBase.Color color) {
        isStriped = striped;
        isAnimated = animated;
        this.color = color;
    }

    @Override
    public void render(Context context, Number value, SafeHtmlBuilder sb) {
        if (value != null) {
            String percentage = value + "%";
            String percentageText = percentage;
            String className = "";
            if (color != null) {
                className = color.get();
            }
            else {
                if (value.intValue() <= 1) {
                    className= ProgressBarBase.Color.DANGER.get();
                }
                else if (value.intValue() <100) {
                    className = ProgressBarBase.Color.WARNING.get();
                }
                else {
                    className = ProgressBarBase.Color.SUCCESS.get();
                }

            }
            if (isStriped && value.intValue() < 100) {
                className += " progress-striped";
            }
            if (isAnimated && value.intValue() < 100) {
                className += " active";
            }
            sb.append(template.progressbar(className,percentage));
        }
    }
}
