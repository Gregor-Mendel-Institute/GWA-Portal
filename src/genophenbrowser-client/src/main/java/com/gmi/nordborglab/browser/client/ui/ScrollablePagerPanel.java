package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;

public class ScrollablePagerPanel extends AbstractPager {

	private static ScrollablePagerPanelUiBinder uiBinder = GWT
			.create(ScrollablePagerPanelUiBinder.class);

	interface ScrollablePagerPanelUiBinder extends
			UiBinder<Widget, ScrollablePagerPanel> {
	}

	@UiField
	ScrollPanel scrollPanel;

	private static final int DEFAULT_INCREMENT = 20;

	/**
	 * The increment size.
	 */
	private int incrementSize = DEFAULT_INCREMENT;

	/**
	 * The last scroll position.
	 */
	private int lastScrollPos = 0;

	public ScrollablePagerPanel() {
		initWidget(uiBinder.createAndBindUi(this));

		scrollPanel.getElement().setTabIndex(-1);

		// Handle scroll events.
		scrollPanel.addScrollHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				// If scrolling up, ignore the event.
				int oldScrollPos = lastScrollPos;
				lastScrollPos = scrollPanel.getVerticalScrollPosition();
				if (oldScrollPos >= lastScrollPos) {
					return;
				}

				HasRows display = getDisplay();
				if (display == null) {
					return;
				}
				int maxScrollTop = scrollPanel.getWidget().getOffsetHeight()
						- scrollPanel.getOffsetHeight();
				if (lastScrollPos >= maxScrollTop) {
					// We are near the end, so increase the page size.
					int newPageSize = Math.min(display.getVisibleRange()
							.getLength() + incrementSize, display.getRowCount());
					display.setVisibleRange(0, newPageSize);
				}
			}
		});
	}

	/**
	 * Get the number of rows by which the range is increased when the scrollbar
	 * reaches the bottom.
	 * 
	 * @return the increment size
	 */
	public int getIncrementSize() {
		return incrementSize;
	}

	@Override
	public void setDisplay(HasRows display) {
		assert display instanceof Widget : "display must extend Widget";
		scrollPanel.setWidget((Widget) display);
		super.setDisplay(display);
	}

	/**
	 * Set the number of rows by which the range is increased when the scrollbar
	 * reaches the bottom.
	 * 
	 * @param incrementSize
	 *            the incremental number of rows
	 */
	public void setIncrementSize(int incrementSize) {
		this.incrementSize = incrementSize;
	}

	@Override
	protected void onRangeOrRowCountChanged() {

	}
	
	@UiChild(limit=1,tagname="scrollContent")
	public void addScrollContent(Widget content) {
		scrollPanel.add(content);
	}

}
