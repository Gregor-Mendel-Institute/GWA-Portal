package com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology;

import com.gwtplatform.mvp.client.ViewImpl;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.OntologyOverviewPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OntologyOverviewView extends ViewImpl implements
		OntologyOverviewPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, OntologyOverviewView> {
	}

	@Inject
	public OntologyOverviewView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
