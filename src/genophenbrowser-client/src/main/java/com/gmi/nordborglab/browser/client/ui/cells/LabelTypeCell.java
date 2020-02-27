package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.gwtbootstrap3.client.ui.constants.LabelType;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.07.13
 * Time: 14:06
 * To change this template use File | Settings | File Templates.
 */
public class LabelTypeCell extends AbstractCell<String> {

    private final Map<String, LabelType> map;

    interface Template extends SafeHtmlTemplates {

        @Template("<span class=\"label {0}\">{1}</span>")
        SafeHtml cell(String className, SafeHtml label);

    }

    private static Template templates = GWT.create(Template.class);

    public LabelTypeCell(Map<String, LabelType> map) {
        this.map = map;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        LabelType type = LabelType.DEFAULT;
        if (value != null && map.containsKey(value.toUpperCase())) {
            type = map.get(value.toUpperCase());
        }
        sb.append(templates.cell(type.getCssName(), SafeHtmlUtils.fromSafeConstant(value)));
    }
}
