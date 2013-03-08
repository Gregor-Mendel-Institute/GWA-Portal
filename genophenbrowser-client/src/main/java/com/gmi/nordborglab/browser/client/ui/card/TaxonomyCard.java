package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaxonomyCard extends Composite {

    interface TaxonomyCardUiBinder extends UiBinder<FocusPanel, TaxonomyCard> {
    }

    private static TaxonomyCardUiBinder ourUiBinder = GWT.create(TaxonomyCardUiBinder.class);

    private TaxonomyProxy taxonomy;
    private ImageResource imageRes;
    @UiField
    Image taxonomyImg;
    @UiField
    HeadingElement title;
    @UiField
    FocusPanel focusPanel;


    public TaxonomyCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }


    public void setTaxonomy(TaxonomyProxy taxonomy,ImageResource imageRes) {
        this.taxonomy = taxonomy;
        this.imageRes = imageRes;
        updateView();
    }

    private void updateView() {
        if (taxonomy != null)
            title.setInnerText(taxonomy.getGenus()+" "+taxonomy.getSpecies());
        taxonomyImg.setUrl(imageRes.getSafeUri());
    }


    public HandlerRegistration setClickhandler(ClickHandler handler) {
        return focusPanel.addClickHandler(handler);
    }
}