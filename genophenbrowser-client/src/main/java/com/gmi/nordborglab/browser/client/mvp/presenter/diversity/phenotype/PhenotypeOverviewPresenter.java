package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class PhenotypeOverviewPresenter
		extends
		Presenter<PhenotypeOverviewPresenter.MyView, PhenotypeOverviewPresenter.MyProxy> {

	public interface MyView extends View {

		HasData<PhenotypeProxy> getDisplay();
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.phenotypeoverview)
	public interface MyProxy extends ProxyPlace<PhenotypeOverviewPresenter> {
	}
	
	private final PhenotypeManager phenotypeManager;
	protected final AsyncDataProvider<PhenotypeProxy> dataProvider;

	@Inject
	public PhenotypeOverviewPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy,final PhenotypeManager phenotypeManager) {
		super(eventBus, view, proxy);
		this.phenotypeManager = phenotypeManager;
		dataProvider = new AsyncDataProvider<PhenotypeProxy>() {

			@Override
			protected void onRangeChanged(HasData<PhenotypeProxy> display) {
				requestPhenotypes(display.getVisibleRange());
			}
		};
		dataProvider.addDataDisplay(getView().getDisplay());
	}
	
	protected void requestPhenotypes(final Range range) {
		fireEvent(new LoadingIndicatorEvent(true));
		Receiver<PhenotypePageProxy> receiver = new Receiver<PhenotypePageProxy>() {
			@Override
			public void onSuccess(PhenotypePageProxy experiments) {
				fireEvent(new LoadingIndicatorEvent(false));
				dataProvider.updateRowCount((int)experiments.getTotalElements(), true);
				dataProvider.updateRowData(range.getStart(), experiments.getContent());
			}
		};
		phenotypeManager.findAll(receiver,null,null,null,null,range.getStart(),range.getLength());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onReset() {
		super.onReset();
	}
}
