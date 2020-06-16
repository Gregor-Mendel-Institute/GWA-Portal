package com.gmi.nordborglab.browser.client.gin;

import com.gmi.nordborglab.browser.client.resources.CardRendererResources;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.google.inject.Inject;

/**
 * Created by uemit.seren on 11.01.14.
 */
public class ResourceLoader {
    @Inject
    ResourceLoader(MainResources resources, CardRendererResources cardRendererResources) {
        cardRendererResources.style().ensureInjected();
        resources.style().ensureInjected();
    }
}
