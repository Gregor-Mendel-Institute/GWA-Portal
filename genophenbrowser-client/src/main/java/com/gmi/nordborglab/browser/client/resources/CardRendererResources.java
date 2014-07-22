package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Created by uemit.seren on 6/30/14.
 */
public interface CardRendererResources extends ClientBundle {


    public interface Style extends CssResource {

        String icon();

        String title();

        String icon_new();

        String card_selected();

        String icon_text();

        String card();

        String ok();

        String newCardContainer();

        String stats_icon();

        String empty_ok();

        String new_icon_icon();

        String sub_title();

        String card_container();
    }

    @Source("cardstyles.css")
    Style style();
}
