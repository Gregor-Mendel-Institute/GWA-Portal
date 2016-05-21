package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Created by uemit.seren on 10/21/14.
 */
public class SNPDetailPopup extends PopupPanel {

    interface Binder extends UiBinder<Widget, SNPDetailPopup> {

    }
    interface MyStyle extends CssResource {

        String popup();
    }
    private static Binder ourUiBinder = GWT.create(Binder.class);

    protected Integer chromosome;
    protected Integer position;
    protected final PlaceManager placeManager;
    protected final PlaceRequest snpDetailRequest = new PlaceRequest.Builder().nameToken(NameTokens.snps).build();
    @UiField
    InlineHyperlink snpInfoLink;
    @UiField
    HeadingElement snpInfo;
    @UiField
    MyStyle style;

    @UiField
    Anchor snpLdLink;
    @UiField
    Anchor snpExactLdLink;

    @UiField
    Anchor snpGlobalLdLink;

    @Inject
    public SNPDetailPopup(PlaceManager placeManager) {
        super();
        this.placeManager = placeManager;
        setWidget(ourUiBinder.createAndBindUi(this));
        setStylePrimaryName(style.popup());
        setAnimationEnabled(false);
        setAutoHideEnabled(true);
        setAutoHideOnHistoryEventsEnabled(true);

    }


    public void setDataPoint(Long analysisId, Integer chromosome, Integer position) {
        this.chromosome = chromosome;
        this.position = position;
        final PlaceRequest request = new PlaceRequest.Builder(snpDetailRequest).with("id", String.valueOf(analysisId)).with("chr", String.valueOf(chromosome)).with("position", String.valueOf(position)).build();
        snpInfo.setInnerText(String.valueOf(chromosome) + " : " + String.valueOf(position));
        snpInfoLink.setTargetHistoryToken(placeManager.buildHistoryToken(request));
    }

    public HasClickHandlers getSNPLdLink() {
        return snpLdLink;
    }

    public HasClickHandlers getSnpExactLdLink() {
        return snpExactLdLink;
    }

    public HasClickHandlers getSnpGlobalLdLink() {
        return snpGlobalLdLink;
    }

    public Integer getChromosome() {
        return chromosome;
    }

    public Integer getPosition() {
        return position;
    }

    public void setHasLdData(boolean hasLdData) {
        snpLdLink.setVisible(hasLdData);
        snpGlobalLdLink.setVisible(hasLdData);
    }
}