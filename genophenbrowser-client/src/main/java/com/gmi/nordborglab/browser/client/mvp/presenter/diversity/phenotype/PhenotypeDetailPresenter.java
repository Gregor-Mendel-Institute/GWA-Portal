package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeDetailView.PhenotypeDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeDetailView.PhenotypeEditDriver;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class PhenotypeDetailPresenter
		extends
		Presenter<PhenotypeDetailPresenter.MyView, PhenotypeDetailPresenter.MyProxy> implements PhenotypeDetailUiHandlers{

	public interface MyView extends View,HasUiHandlers<PhenotypeDetailUiHandlers> {

		PhenotypeDisplayDriver getDisplayDriver();

		void setState(State state, int permission);

		State getState();

		PhenotypeEditDriver getEditDriver();

		void setAcceptableValuesForUnitOfMeasure(
				Collection<UnitOfMeasureProxy> values);

		void setGeoChartData(Multiset<String> geochartData);

		void setHistogramChartData(
				ImmutableSortedMap<Double, Integer> histogramData);

		void scheduledLayout();

		void setPhenotypExplorerData(ImmutableList<TraitProxy> traits);

		void setPhenotypePieChartData(List<StatisticTypeProxy> statisticTypes);

		void drawCharts();
	}
	
	
	protected PhenotypeProxy phenotype;
	protected boolean fireLoadEvent;
	protected final PlaceManager placeManager; 
	protected final PhenotypeManager phenotypeManager;
	protected final CurrentUser currentUser;
	protected final Receiver<PhenotypeProxy> receiver;
	private ImmutableSortedMap<Double, Integer> histogramData;
	private List<StatisticTypeProxy> statisticTypes;
	protected HashMap<StatisticTypeProxy, List<TraitProxy>> cache = new HashMap<StatisticTypeProxy, List<TraitProxy>>();
			
	private Multiset<String> geochartData;
	private static int BIN_COUNT = 20;
	
	@ProxyCodeSplit
	@NameToken(NameTokens.phenotype)
	@TabInfo(label="Overview",priority=0, container = PhenotypeDetailTabPresenter.class)
	public interface MyProxy extends TabContentProxyPlace<PhenotypeDetailPresenter> {
	}

	@Inject
	public PhenotypeDetailPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PlaceManager placeManager,
			final PhenotypeManager phenotypeManager, 
			final CurrentUser currentUser) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		this.placeManager = placeManager;
		this.phenotypeManager = phenotypeManager;
		this.currentUser = currentUser;
		getView().setAcceptableValuesForUnitOfMeasure(currentUser.getAppData().getUnitOfMeasureList());
		receiver = new Receiver<PhenotypeProxy>() {
			public void onSuccess(PhenotypeProxy response) {
				phenotype = response;
				fireEvent(new LoadPhenotypeEvent(phenotype));
				getView().setState(State.DISPLAYING,getPermission());
				getView().getDisplayDriver().display(phenotype);
			}


			public void onFailure(ServerFailure error) {
				fireEvent(new DisplayNotificationEvent("Error while saving",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,0));
				onEdit();
			}

			public void onConstraintViolation(
					Set<ConstraintViolation<?>> violations) {
				super.onConstraintViolation(violations);
				getView().setState(State.EDITING,getPermission());
			}
		};
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				PhenotypeDetailTabPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}
	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadEvent) {
			fireLoadEvent = false;
			fireEvent(new LoadPhenotypeEvent(phenotype));
		}
		getView().getDisplayDriver().display(phenotype);
		getView().setState(State.DISPLAYING,currentUser.getPermissionMask(phenotype.getUserPermission()));
		getProxy().getTab().setTargetHistoryToken(placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest()));
		LoadingIndicatorEvent.fire(this, false);
		getView().setPhenotypePieChartData(statisticTypes);
		getView().setGeoChartData(null);
		getView().setHistogramChartData(null);
		getView().setPhenotypExplorerData(null);
		getView().scheduledLayout();
	}
	
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		Receiver<PhenotypeProxy> receiver = new Receiver<PhenotypeProxy>() {
			@Override
			public void onSuccess(PhenotypeProxy phen) {
				phenotype = phen;
				statisticTypes = phen.getStatisticTypes();
				fireLoadEvent = true;
				getProxy().manualReveal(PhenotypeDetailPresenter.this);
			}

			@Override
			public void onFailure(ServerFailure error) {
				statisticTypes = null;
				fireEvent(new LoadingIndicatorEvent(false));
				getProxy().manualRevealFailed();
				placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
			}
		};
		try {
			Long phenotypeId = Long.valueOf(placeRequest.getParameter("id",
					null));
			if (phenotype == null || !phenotype.getId().equals(phenotypeId)) {
				statisticTypes = null;
				cache.clear();
				phenotypeManager.findOne(receiver, phenotypeId);
			} else {
				getProxy().manualReveal(PhenotypeDetailPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
		}
	}
	

	@Override
	public void onEdit() {
		getView().setState(State.EDITING,getPermission());
		PhenotypeRequest ctx = phenotypeManager.getContext();
		getView().getEditDriver().edit(phenotype, ctx);
		
		///TODO Fix this better. 
		List<String> paths = ImmutableList.<String>builder().addAll(Arrays.asList(getView().getEditDriver().getPaths())).add("userPermission").add("statisticTypes").add("traitOntologyTerm").build();
		ctx.save(phenotype).with(paths.toArray(new String[0])).to(receiver);
	}

	@Override
	public void onSave() {
		getView().setState(State.SAVING,getPermission());
		RequestContext req = getView().getEditDriver().flush();
		req.fire();
	}

	@Override
	public void onCancel() {
		getView().setState(State.DISPLAYING,getPermission());
		getView().getDisplayDriver().display(phenotype);
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	@ProxyEvent
	public void onLoadPhenotype(LoadPhenotypeEvent event) {
		if (phenotype != event.getPhenotype()) {
			cache.clear();
			statisticTypes = phenotype.getStatisticTypes();
		}
		phenotype = event.getPhenotype();
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id",phenotype.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(new TabDataDynamic(tabData.getLabel(), tabData.getPriority(), historyToken));
	}
	
	private int getPermission() {
		int permission = 0;
		if (phenotype != null) 
		    permission =  currentUser.getPermissionMask(phenotype.getUserPermission());
		return permission;
	}
	
	private void calculateHistogramData(List<TraitProxy> traits) {
		SortedMultiset<Double> data = TreeMultiset.create();
		for (TraitProxy trait : traits) {
			if (trait.getValue() != null) {
				try {
					data.add(Double.parseDouble(trait.getValue()));
				} catch (NumberFormatException e) {
				}
			}
		}
		if (data.size() == 0)
			return;
		Double min = data.elementSet().first();
		Double max = data.elementSet().last();
		if (min == max)
			return;
		Double binWidth = (max - min) / BIN_COUNT;
		ImmutableSortedMap.Builder<Double, Integer> builder = ImmutableSortedMap
				.naturalOrder();
		for (int i = 0; i < BIN_COUNT; i++) {
			Double lowBound = min + i * binWidth;
			Double upperBound = lowBound + binWidth;
			builder.put(
					lowBound,
					data.subMultiset(lowBound, BoundType.CLOSED,
							upperBound, BoundType.CLOSED).size());
		}
		builder.put(max, 0);
		histogramData = builder.build();
	}

	private void calculateGeoChartData(List<TraitProxy> traits) {
		ImmutableMultiset.Builder<String> builder = ImmutableMultiset.builder();
		for (TraitProxy trait : traits) {
			try {
				String cty = trait.getObsUnit().getStock().getPassport()
						.getCollection().getLocality().getCountry();
				builder.add(cty);
			} catch (NullPointerException e) {

			}
		}
		geochartData = builder.build();
	}

	@Override
	public void onSelectPhenotypeType(Integer index) {
		if (index == null) {
			getView().setGeoChartData(null);
			getView().setHistogramChartData(null);
			getView().setPhenotypExplorerData(null);
			getView().drawCharts();
		}
		else {
			final StatisticTypeProxy type = Iterables.get(statisticTypes,index);
			List<TraitProxy> cachedTraits = cache.get(type);
			if (cachedTraits != null) 
			{
				calculateChartDataAndDisplay(cachedTraits);
			}
			else{
				fireEvent(new LoadingIndicatorEvent(true));
				phenotypeManager.findAllTraitValuesByType(phenotype.getId(),type.getId(),new Receiver<List<TraitProxy>>() {
	
					@Override
					public void onSuccess(List<TraitProxy> response) {
						fireEvent(new LoadingIndicatorEvent(false));
						cache.put(type, response);
						calculateChartDataAndDisplay(response);
					}
				});
			}
	 	}
	}
	
	private void calculateChartDataAndDisplay(List<TraitProxy> traits) {
		calculateGeoChartData(traits);
		calculateHistogramData(traits);
		getView().setGeoChartData(geochartData);
		getView().setHistogramChartData(histogramData);
		getView().setPhenotypExplorerData(ImmutableList.copyOf(traits));
		getView().drawCharts();
	}
	
}
