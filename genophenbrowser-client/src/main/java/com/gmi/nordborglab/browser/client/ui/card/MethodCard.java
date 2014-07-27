package com.gmi.nordborglab.browser.client.ui.card;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.events.SelectMethodEvent;
import com.gmi.nordborglab.browser.client.resources.CardRendererResources;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class MethodCard extends Composite {
    interface MethodCardUiBinder extends UiBinder<FocusPanel, MethodCard> {
    }

    private static MethodCardUiBinder ourUiBinder = GWT.create(MethodCardUiBinder.class);

    @UiField
    Icon selectIcon;
    @UiField
    HeadingElement title;
    @UiField
    ParagraphElement subtitle;
    @UiField
    DivElement card;

    @UiField
    SpanElement typeLabel;
    @UiField
    SpanElement runTimeDuration;
    @UiField
    CardRendererResources cardRen;

    protected EventBus eventBus;

    protected boolean isSelected;

    protected StudyProtocolProxy studyProtocol;

    public MethodCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public StudyProtocolProxy getStudyProtocol() {
        return studyProtocol;
    }

    public void setStudyProtocol(StudyProtocolProxy studyProtocol) {
        this.studyProtocol = studyProtocol;
        updateView();
    }

    private void updateView() {
        if (studyProtocol == null)
            return;
        title.setInnerText(studyProtocol.getAnalysisMethod());
        subtitle.setInnerText(studyProtocol.getFullname());
        typeLabel.setInnerText(studyProtocol.getType());
        typeLabel.removeClassName("label-success");
        typeLabel.removeClassName("label-info");
        if (studyProtocol.getType().equals("MIXED")) {
            runTimeDuration.setInnerText("10 minutes");
            typeLabel.addClassName("label-success");
        } else if (studyProtocol.getType().equals("LINEAR")) {
            runTimeDuration.setInnerText("2 minutes");
            typeLabel.addClassName("label-info");
        } else {
            runTimeDuration.setInnerText("0.5 minutes");
        }
    }

    private void updateSelected() {
        if (isSelected) {
            card.addClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().empty_ok());
            selectIcon.addStyleName(cardRen.style().ok());
            selectIcon.setType(IconType.OK);

        } else {
            card.removeClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().ok());
            selectIcon.addStyleName(cardRen.style().empty_ok());
            selectIcon.setType(IconType.OK_CIRCLE);
        }
    }


    public void setSelected(boolean isSelected) {
        if (isSelected != this.isSelected) {
            this.isSelected = isSelected;
            updateSelected();
        }
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @UiHandler("focusPanel")
    public void onClick(ClickEvent e) {
        SelectMethodEvent.fire(eventBus, this);
    }

    @UiHandler("focusPanel")
    public void onKeyUp(KeyUpEvent event) {
        SelectMethodEvent.fire(eventBus, this);
    }

}