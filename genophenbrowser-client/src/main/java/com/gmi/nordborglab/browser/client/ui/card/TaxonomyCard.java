package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.AppStatProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;
import java.util.Map;

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
    @UiField
    ImageElement taxonomyImg;
    @UiField
    HeadingElement title;
    @UiField
    FocusPanel focusPanel;
    @UiField
    SpanElement statsPhenotypes;
    @UiField
    SpanElement statsGenotypes;
    @UiField
    SpanElement statsStocks;
    @UiField
    SpanElement statsPassports;

    private final Map<AppStatProxy.STAT, SpanElement> statMap;


    public TaxonomyCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
        statMap = ImmutableMap.<AppStatProxy.STAT, SpanElement>builder()
                .put(AppStatProxy.STAT.PHENOTYPE, statsPhenotypes)
                .put(AppStatProxy.STAT.PASSPORT, statsPassports)
                .put(AppStatProxy.STAT.STOCKS, statsStocks)
                .put(AppStatProxy.STAT.GENOTYPES, statsGenotypes).build();

    }


    public void setTaxonomy(TaxonomyProxy taxonomy) {
        this.taxonomy = taxonomy;
        updateView();
    }

    private void updateView() {
        if (taxonomy != null)
            title.setInnerText(taxonomy.getGenus() + " " + taxonomy.getSpecies());
        SafeUri imageUri = UriUtils.fromString("/provider/taxonomy/" + taxonomy.getId() + "/image.png");
        taxonomyImg.setSrc(imageUri.asString());
        List<AppStatProxy> stats = taxonomy.getStats();
        if (stats != null) {
            for (AppStatProxy stat : stats) {
                statMap.get(stat.getStat()).setInnerText(String.valueOf(stat.getValue()));
            }
        }
    }


    public HandlerRegistration setClickhandler(ClickHandler handler) {
        return focusPanel.addClickHandler(handler);
    }
}