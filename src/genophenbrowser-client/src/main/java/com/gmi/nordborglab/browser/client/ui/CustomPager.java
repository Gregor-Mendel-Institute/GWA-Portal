package com.gmi.nordborglab.browser.client.ui;

import com.google.common.collect.ImmutableSet;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;

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
    @UiField
    Button previousPage;
    @UiField
    Button nextPage;
    protected ImmutableSet<String> pageSizes = ImmutableSet.of("15", "25", "50", "100", "250", "500");


	public CustomPager() {
		initWidget(uiBinder.createAndBindUi(this));
        for (String size : pageSizes) {
            pageSize.addItem(size);
        }
        setRangeLimited(true);
    }

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();

        this.pageSize.setSelectedIndex(pageSizes.asList().indexOf(String.valueOf(display.getVisibleRange().getLength())));
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

	public void setPageControlDisabled(boolean disabled) {
		setPreviousPageEnabled(disabled || !hasPreviousPage());
		setNextPageEnabled(disabled || !hasNextPage());
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
	}
	
	private void setNextPageEnabled(boolean disabled) {
		nextPage.setEnabled(!disabled);
	}
}
