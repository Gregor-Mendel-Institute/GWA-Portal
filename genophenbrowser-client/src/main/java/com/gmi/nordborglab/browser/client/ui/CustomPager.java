package com.gmi.nordborglab.browser.client.ui;

import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class CustomPager extends AbstractPager {

	private static CustomPagerUiBinder uiBinder = GWT
			.create(CustomPagerUiBinder.class);

	interface CustomPagerUiBinder extends UiBinder<Widget, CustomPager> {
	}

	@UiField
	TextBox page;
	@UiField
	ListBox pageSize;
	@UiField
	Label label;
	@UiField Anchor previousPage;
	@UiField Anchor nextPage;

	public CustomPager() {
		initWidget(uiBinder.createAndBindUi(this));
		pageSize.addItem("10");
		pageSize.addItem("25");
		pageSize.addItem("50");
		pageSize.addItem("100");
		pageSize.addItem("250");
		pageSize.addItem("500");
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();
	    label.setText(createText());
	    // Update the prev and first buttons.
	    setPreviousPageEnabled(!hasPreviousPage());

	    // Update the next and last buttons.
	    if (isRangeLimited() || !display.isRowCountExact()) {
	    	setNextPageEnabled(!hasNextPage());
	    }
	    Integer currentPage = getPage()+1;
	    page.setText(currentPage.toString());
	}

	@UiHandler("nextPage")
	public void onNextPage(ClickEvent e) {
		nextPage();
	}

	@UiHandler("previousPage")
	public void onPreviousPage(ClickEvent e) {
		previousPage();
	}

	@UiHandler("page")
	public void onPageKeyPress(KeyPressEvent e) {
		boolean enterPressed = KeyCodes.KEY_ENTER == e.getNativeEvent()
				.getKeyCode();
		if (enterPressed) {
			try {
				Integer pageSize = Integer.parseInt(page.getText());
				if (pageSize > 0) 
					pageSize = pageSize -1;
				setPage(pageSize);
			} catch (Exception ex) {
			}
		}
	}

	@UiHandler("pageSize")
	public void onPageSizeChanged(ChangeEvent e) {
		setPageSize(Integer.parseInt(pageSize.getItemText(pageSize
				.getSelectedIndex())));
	}

	protected String createText() {
		NumberFormat formatter = NumberFormat.getFormat("#,###");
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		int pageStart = range.getStart() + 1;
		int pageSize = range.getLength();
		int dataSize = display.getRowCount();
		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
		endIndex = Math.max(pageStart, endIndex);
		boolean exact = display.isRowCountExact();
		return formatter.format(pageStart) + "-" + formatter.format(endIndex)
				+ (exact ? " of " : " of over ") + formatter.format(dataSize);
	}
	
	private void setPreviousPageEnabled(boolean disabled) {
		previousPage.setEnabled(!disabled);
		/*
		if (disabled)
			previousPage.addStyleName("disabled");
		else 
			previousPage.removeStyleName("disabled");
			*/
	}
	
	private void setNextPageEnabled(boolean disabled) {
		nextPage.setEnabled(!disabled);
		return;
		/*if (disabled)
			nextPage.addStyleName("disabled");
		else 
			nextPage.removeStyleName("disabled");
			*/
	}

}
