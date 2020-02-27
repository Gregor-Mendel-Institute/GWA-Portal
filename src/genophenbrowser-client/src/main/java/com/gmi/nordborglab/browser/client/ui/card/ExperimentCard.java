package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.resources.CardRendererResources;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/4/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentCard extends Composite {

    interface ExperimentCardUiBinder extends UiBinder<FocusPanel, ExperimentCard> {

    }


    private static ExperimentCardUiBinder uiBinder = GWT.create(ExperimentCardUiBinder.class);
    protected ExperimentProxy experiment;

    @UiField
    SpanElement phenotypeCountLabel;

    //@UiField SpanElement studyCountLabel;
    @UiField
    SpanElement permissionLabel;
    @UiField
    Icon selectIcon;
    @UiField
    HeadingElement title;
    @UiField
    ParagraphElement subtitle;
    @UiField
    DivElement card;

    @UiField
    HTMLPanel cardContent;
    @UiField
    HTMLPanel newCardContent;

    @UiField
    CardRendererResources cardRen;

    protected boolean isSelected;

    public ExperimentCard() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    public void setExperiment(ExperimentProxy experiment) {
        this.experiment = experiment;
        updateView();
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return ((FocusPanel) getWidget()).addClickHandler(handler);
    }

    private void updateView() {

        cardContent.setVisible(experiment != null);
        newCardContent.setVisible(experiment == null);

        if (experiment != null) {
            cardContent.setVisible(true);
            title.setInnerText(experiment.getName());
            subtitle.setInnerText(experiment.getOriginator());
            phenotypeCountLabel.setInnerText(String.valueOf(experiment.getNumberOfPhenotypes()));
            updatePermission();
            updateSelected();
        }
    }

    private void updatePermission() {
        if (experiment.isPublic()) {
            permissionLabel.setInnerText("PUBLIC");
            permissionLabel.removeClassName("label-warning");
            permissionLabel.addClassName("label-success");
        } else {
            permissionLabel.setInnerText("PRIVATE");
            permissionLabel.removeClassName("label-success");
            permissionLabel.addClassName("label-warning");
        }
    }


    private void updateSelected() {
        if (isSelected) {
            card.addClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().empty_ok());
            selectIcon.addStyleName(cardRen.style().ok());
            selectIcon.setType(IconType.CHECK);

        } else {
            card.removeClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().ok());
            selectIcon.addStyleName(cardRen.style().empty_ok());
            selectIcon.setType(IconType.CHECK_CIRCLE);
        }

    }


    public void setSelected(boolean isSelected) {
        if (isSelected != this.isSelected) {
            this.isSelected = isSelected;
            updateSelected();
        }
    }

    public ExperimentProxy getExperiment() {
        return experiment;
    }

    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return ((FocusPanel) getWidget()).addKeyUpHandler(handler);
    }


}