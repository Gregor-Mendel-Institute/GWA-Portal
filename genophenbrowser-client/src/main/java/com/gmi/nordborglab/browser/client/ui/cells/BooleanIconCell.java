package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 8:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class BooleanIconCell extends AbstractCell<Boolean> {


    interface Template extends SafeHtmlTemplates {
        @Template("<span class=\"icon-ok\" style=\"color:green;font-size:15px;\">")
        SafeHtml okIcon();

        @Template("<span class=\"icon-remove\" style=\"color:red;font-size:15px;\">")
        SafeHtml nokIcon();
    }

    private static Template template = GWT.create(Template.class);;



    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        if (value)
            sb.append(template.okIcon());
        else
            sb.append(template.nokIcon());
    }
}
