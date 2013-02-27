package com.gmi.nordborglab.browser.client.ui.card;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;

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

    public interface MyStyle extends CssResource   {
        String card_selected();

        String ok();
        String empty_ok();
    }
    private static ExperimentCardUiBinder uiBinder = GWT.create(ExperimentCardUiBinder.class);
    protected ExperimentProxy experiment;

    @UiField
    SpanElement phenotypeCountLabel;

    @UiField SpanElement studyCountLabel;
    @UiField SpanElement permissionLabel;
    @UiField
    Icon selectIcon;
    @UiField
    HeadingElement title;
    @UiField
    ParagraphElement subtitle;
    @UiField
    DivElement card;
    @UiField MyStyle style;
    protected boolean isSelected;
    public ExperimentCard() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    public void setExperiment(ExperimentProxy experiment) {
        this.experiment=experiment;
        updateView();
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return ((FocusPanel)getWidget()).addClickHandler(handler);
    }

    private void updateView() {
        if (experiment == null)
            return;
        title.setInnerText(experiment.getName());
        subtitle.setInnerText(experiment.getOriginator());
        phenotypeCountLabel.setInnerText(String.valueOf(experiment.getNumberOfPhenotypes()));
        // TODO create getter for number of studies
        studyCountLabel.setInnerText(String.valueOf(experiment.getNumberOfPhenotypes()));
        updateSelected();
    }


    private void updateSelected() {
        if (isSelected) {
            card.addClassName(style.card_selected());
            selectIcon.removeStyleName(style.empty_ok());
            selectIcon.addStyleName(style.ok());
            selectIcon.setType(IconType.OK);

        }
        else {
            card.removeClassName(style.card_selected());
            selectIcon.removeStyleName(style.ok());
            selectIcon.addStyleName(style.empty_ok());
            selectIcon.setType(IconType.OK_CIRCLE);
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
        return ((FocusPanel)getWidget()).addKeyUpHandler(handler);
    }



}