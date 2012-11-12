package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;


public class ExperimentsOverviewPresenter
		extends
		Presenter<ExperimentsOverviewPresenter.MyView, ExperimentsOverviewPresenter.MyProxy>  implements ExperimentsOverviewUiHandlers{

	public interface MyView extends View,HasUiHandlers<ExperimentsOverviewUiHandlers> {
		HasData<ExperimentProxy> getDisplay();
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.experiments)
    @TabInfo(container = ExperimentsOverviewTabPresenter.class,
	      label = "Overview",
	      priority = 0) 
	@Title("Experiments")
	public interface MyProxy extends TabContentProxyPlace<ExperimentsOverviewPresenter> {
	}
	

	private final ExperimentManager experimentManager;
	private final PlaceManager placeManager;
	protected final AsyncDataProvider<ExperimentProxy> dataProvider;

	@Inject
	public ExperimentsOverviewPresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy,
			final ExperimentManager experimentManager, 
			final PlaceManager placeManager) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		this.experimentManager = experimentManager;
		this.placeManager = placeManager;
		dataProvider = new AsyncDataProvider<ExperimentProxy>() {

			@Override
			protected void onRangeChanged(HasData<ExperimentProxy> display) {
				requestExperiments();
			}
		};
	}

	protected void requestExperiments() {
		LoadingIndicatorEvent.fire(this, true);
		Receiver<ExperimentPageProxy> receiver = new Receiver<ExperimentPageProxy>() {
			@Override
			public void onSuccess(ExperimentPageProxy experiments) {
				LoadingIndicatorEvent.fire(ExperimentsOverviewPresenter.this, false);
				dataProvider.updateRowCount((int)experiments.getTotalElements(), true);
				dataProvider.updateRowData(0, experiments.getContent());
			}
		};
		Range range = getView().getDisplay().getVisibleRange();
		experimentManager.findAll(receiver,range.getStart(),range.getLength());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, ExperimentsOverviewTabPresenter.TYPE_SetTabContent,
				this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		dataProvider.addDataDisplay(getView().getDisplay());
	}

	@Override
	protected void onReset() {
		super.onReset();
		
	}

	@Override
	public void loadExperiment(ExperimentProxy experiment) {
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.experiment).with("id", experiment.getId().toString());
		placeManager.revealPlace(request);
	}
}
