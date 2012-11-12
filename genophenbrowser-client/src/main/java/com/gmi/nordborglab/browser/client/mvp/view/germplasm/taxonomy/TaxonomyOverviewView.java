package com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy.TaxonomyOverviewPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TaxonomyOverviewView extends ViewImpl implements
		TaxonomyOverviewPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, TaxonomyOverviewView> {
	}

	@Inject
	public TaxonomyOverviewView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
