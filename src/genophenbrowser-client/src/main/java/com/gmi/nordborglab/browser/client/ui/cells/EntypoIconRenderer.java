package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;

/**
 * Created by uemit.seren on 1/18/16.
 */
public class EntypoIconRenderer extends AbstractSafeHtmlRenderer<String> {

    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<i class=\"{0}\" ></i>")
        SafeHtml cell(String iconClass);
    }

    private static EntypoIconRenderer instance;

    public static EntypoIconRenderer getInstance() {
        if (instance == null) {
            instance = new EntypoIconRenderer();
        }
        return instance;
    }


    private static Template template = GWT.create(Template.class);


    @Override
    public SafeHtml render(String object) {
        return template.cell(object);
    }
}
