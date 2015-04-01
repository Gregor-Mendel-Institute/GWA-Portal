package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.gwtbootstrap3.client.ui.constants.ProgressBarType;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/11/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressBarCell extends AbstractCell<Number> {

    interface Template extends SafeHtmlTemplates {
        @Template(
                "<div style=\"margin-bottom:0px;margin-top:2px;width:100px\"class=\"progress\" role=\"progressbar\" aria-valuemin=\"0\" aria-valuemax=\"100\" aria-valuenow=\"{1}\">" +
                        "<div class=\"progress-bar {0}\" style=\"min-width:2em;width:{1}%\" >" +
                        "{1}%" +
                        "</div>" +
                        "</div>")
        SafeHtml progressbar(String cssClass, Number percentage);
    }

    private static Template template = GWT.create(Template.class);

    private boolean isStriped = false;
    private boolean isAnimated = false;
    private ProgressBarType color;

    public ProgressBarCell(boolean striped, boolean animated, ProgressBarType color) {
        isStriped = striped;
        isAnimated = animated;
        this.color = color;
    }

    @Override
    public void render(Context context, Number value, SafeHtmlBuilder sb) {
        if (value != null) {
            String className = "";
            if (color != null) {
                className = color.getCssName();
            } else {
                if (value.intValue() <= 1) {
                    className = ProgressBarType.DANGER.getCssName();
                } else if (value.intValue() < 100) {
                    className = ProgressBarType.WARNING.getCssName();
                } else {
                    className = ProgressBarType.SUCCESS.getCssName();
                }

            }
            if (isStriped && value.intValue() < 100) {
                className += " progress-striped";
            }
            if (isAnimated && value.intValue() < 100) {
                className += " active";
            }
            sb.append(template.progressbar(className, value));
        }
    }
}
