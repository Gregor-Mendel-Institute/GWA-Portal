package com.gmi.nordborglab.browser.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class WizardPanel extends ResizeComposite {

	private static WizardPanelUiBinder uiBinder = GWT
			.create(WizardPanelUiBinder.class);

	interface WizardPanelUiBinder extends UiBinder<Widget, WizardPanel> {
	}
	
	interface MyStyle extends CssResource {
		String stepContainer();
		String stepContainer_item();
		String stepContainer_item_active();
		String arrow_container();
		String arrow_border();
		String arrow();
		String step_number();
		String step_title();
	}
	
	@UiField MyStyle style;
	@UiField FlowPanel stepContainer;
	@UiField DeckLayoutPanel pageContainer;
	@UiField Button nextBtn;
	@UiField Button previousBtn;
	@UiField Button cancelBtn;
	@UiField HTMLPanel indicator;
	@UiField MainResources mainRes;
	private Integer numberOfSteps = 0;
	private List<HTMLPanel> stepWidgets = new ArrayList<HTMLPanel>();

	public WizardPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		pageContainer.setAnimationDuration(1000);
		pageContainer.setAnimationVertical(true);
	}
	
	@UiChild(tagname="page")
	public void addPage(Widget page,String title) {
		numberOfSteps = numberOfSteps +1;
		HTMLPanel item = new HTMLPanel("");
		if (numberOfSteps == 1)
			item.setStylePrimaryName(style.stepContainer_item_active());
		else
			item.setStylePrimaryName(style.stepContainer_item());
		Label stepNumber = new Label();
		stepNumber.setText(numberOfSteps.toString());
		stepNumber.setStylePrimaryName(style.step_number());
		item.add(stepNumber);
		Label stepTitle = new Label();
		stepTitle.setText(title);
		stepTitle.setStylePrimaryName(style.step_title());
		item.add(stepTitle);
	    stepContainer.add(item);
	    pageContainer.add(page);
	    pageContainer.showWidget(0);
	    stepWidgets.add(item);
	} 
	
	
	public void nextStep() {
		previousBtn.setVisible(true);
		int visibleIndex = pageContainer.getVisibleWidgetIndex(); 
		if (visibleIndex < pageContainer.getWidgetCount()-1)
			pageContainer.showWidget(visibleIndex+1);
		updateWizardControls();
	}
	
	public void previousStep() {
		int visibleIndex = pageContainer.getVisibleWidgetIndex(); 
		if ( visibleIndex> 0)
			pageContainer.showWidget(visibleIndex-1);
		updateWizardControls();
		nextBtn.setText("Next");
		nextBtn.removeStyleName(mainRes.style().button_red());
		nextBtn.addStyleName(mainRes.style().button_blue());
	}
	
	private void updateWizardControls() {
		int visibleIndex = pageContainer.getVisibleWidgetIndex(); 
		if (visibleIndex+1 == pageContainer.getWidgetCount()) { 
			nextBtn.setText("Finish");
			nextBtn.removeStyleName(mainRes.style().button_blue());
			nextBtn.addStyleName(mainRes.style().button_red());
		}
		if (visibleIndex == 0)
			previousBtn.setVisible(false);
		indicator.getElement().getStyle().setTop(40+visibleIndex*105,Style.Unit.PX);
		for (int i =0;i<stepWidgets.size();i++) {
			HTMLPanel step = stepWidgets.get(i);
			if (i == visibleIndex)
				step.setStylePrimaryName(style.stepContainer_item_active());
			else
				step.setStylePrimaryName(style.stepContainer_item());
		}
	}
	
	public HandlerRegistration addNextButtonClickHandler(ClickHandler handler) {
		return nextBtn.addClickHandler(handler);
	}
	
	public HandlerRegistration addPreviousButtonClickHandler(ClickHandler handler) {
		return previousBtn.addClickHandler(handler);
	}
	
	public HandlerRegistration addCancelButtonClickHandler(ClickHandler handler) {
		return cancelBtn.addClickHandler(handler);
	}
}
