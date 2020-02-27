package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import org.gwtbootstrap3.client.ui.constants.IconType;


/**
 * Created by uemit.seren on 1/19/16.
 */
public class FontAwesomeIconRenderer extends AbstractSafeHtmlRenderer<IconType> {

    interface Template extends SafeHtmlTemplates {
        @Template("<span class=\"fa {0}\">")
        SafeHtml icon(String icon);
    }

    private static Template template = GWT.create(Template.class);

    private static FontAwesomeIconRenderer instance;

    public static FontAwesomeIconRenderer getInstance() {
        if (instance == null) {
            instance = new FontAwesomeIconRenderer();
        }
        return instance;
    }


    @Override
    public SafeHtml render(IconType object) {
        return template.icon(object.getCssName());
    }
}
