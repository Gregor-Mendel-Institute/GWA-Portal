package com.gmi.nordborglab.browser.client.mvp.view.diversity;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class DiversityView extends ViewImpl implements
		DiversityPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, DiversityView> {
	}

	@UiField
	SimpleLayoutPanel container;
	@UiField
	FlowPanel breadcrumbs;
	@UiField Label titleLabel;
	
	private final PlaceManager placeManager;

	@Inject
	public DiversityView(final Binder binder,final PlaceManager placeManager) {
		widget = binder.createAndBindUi(this);
		this.placeManager = placeManager;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == DiversityPresenter.TYPE_SetMainContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	private void setMainContent(Widget content) {
		if (content != null) {
			container.setWidget(content);
		}
	}

	@Override
	public void clearBreadcrumbs(int breadcrumbSize) {
		breadcrumbs.clear();
		if (breadcrumbSize > 0)
			breadcrumbs.add(new InlineHyperlink("Loading title...",""));
		for (int i = 0; i < breadcrumbSize; ++i) {
			breadcrumbs.add(new InlineLabel(" > "));
			breadcrumbs.add(new InlineHyperlink("Loading title...",""));
		}
	}

	@Override
	public void setBreadcrumbs(int index, String title,String historyToken) {
		InlineHyperlink hyperlink = null;
		if (index ==0)
			hyperlink = (InlineHyperlink) breadcrumbs.getWidget(0);
		else
		    hyperlink = (InlineHyperlink) breadcrumbs
			 	.getWidget((index *2));
		if (title == null) {
			hyperlink.setText("Unknown title");
		} else {
			hyperlink.setText(title);
		}
		hyperlink.setTargetHistoryToken(historyToken);
	}

	@Override
	public void setTitle(String title) {
		if (title != null)
			titleLabel.setText(title);
	}
	
}
