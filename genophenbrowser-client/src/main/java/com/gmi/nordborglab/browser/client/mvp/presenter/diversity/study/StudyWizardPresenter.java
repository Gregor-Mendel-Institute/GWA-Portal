package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gwtplatform.mvp.client.HasUiHandlers;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientGinjector;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.ObsUnitManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyWizardView.StudyCreateDriver;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.BoundType;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class StudyWizardPresenter extends
		Presenter<StudyWizardPresenter.MyView, StudyWizardPresenter.MyProxy>
		implements StudyWizardUiHandlers {
	
	static class NamePredicate implements Predicate<TraitProxy> {
		private String query;
		
		public NamePredicate(String query) {
			this.query = query;
		}
		
		public void setQuery(String query) {
			this.query = query;
		}
		
		public String getQuery() {
			return query;
		}

		@Override
		public boolean apply(@Nullable TraitProxy input) {
			return (query == null || query.length() == 0 || input
					.getObsUnit().getName().indexOf(query) >= 0);
		}
	}
	
	static class CountryPredicate implements Predicate<TraitProxy> {
		
		private Map<String,Boolean> countriesToFilter = new HashMap<String, Boolean>();
		
		public Map<String,Boolean> getCountriesToFilter() {
			return countriesToFilter;
		}

		@Override
		public boolean apply(@Nullable TraitProxy input) {
			if (countriesToFilter == null || countriesToFilter.size() == 0)
				return true;
			return countriesToFilter.containsKey(input.getObsUnit().getStock().getPassport().getCollection().getLocality().getOrigcty());
		}
		
	}

	static class StatisticTypePredicate implements Predicate<TraitProxy> {

		private final StatisticTypeProxy typeToFilter;

		public StatisticTypePredicate(StatisticTypeProxy typeToFilter) {
			this.typeToFilter = typeToFilter;
		}

		@Override
		public boolean apply(@Nullable TraitProxy input) {
			if (typeToFilter == null)
				return true;
			if (input.getStatisticType() == null)
				return false;
			return typeToFilter.getId()
					.equals(input.getStatisticType().getId());
		}

	}

	static class ObsUnitNamePredicate implements Predicate<TraitProxy> {
		private final String nameToFilter;

		public ObsUnitNamePredicate(String nameToFilter) {
			this.nameToFilter = nameToFilter;
		}

		@Override
		public boolean apply(@Nullable TraitProxy input) {
			if (nameToFilter == null)
				return true;
			if (input == null || input.getObsUnit() == null)
				return false;
			return false;
		}
	}

	public interface MyView extends View, HasUiHandlers<StudyWizardUiHandlers> {

		StudyCreateDriver getStudyCreateDriver();

		void setAcceptableValues(List<StudyProtocolProxy> studyProtocolValues,
				List<AlleleAssayProxy> alleleAssayValues,
				List<StatisticTypeProxy> statisticTypeValues);

		void setPreviousStep();

		void setNextStep();

		void showAccessionGenotypeOverlapChart(
				int numberOfObsUnitsWithGenotype,
				int numberOfObsUnitsWithoutGenotype);

		void showBlankPieChart();

		void scheduledLayout();

		void showMissingGenotypes(List<ObsUnitProxy> missingGenotypes);

		HasData<ObsUnitProxy> getMissingGenotypesDisplay();

		HasData<TraitProxy> getPhenotypesDisplay();

		void resetMissingGenotypesDataGrid();

		void resetPhenotypeDataGrid();

		void showBlankColumnChart();

		void showPhenotypeHistogramChart(
				ImmutableSortedMap<Double, Integer> histogram);

		void showBlankGeoChart();

		void showGeoChart(Multiset<String> geochartData);

		TakesValue<StatisticTypeProxy> getStatisticTypeListBox();

		SearchTerm getNameSearchTerm();

		void setCountriesToFilter(Set<String> countries);
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.studywizard)
	@UseGatekeeper(IsLoggedInGatekeeper.class)
	public interface MyProxy extends TabContentProxyPlace<StudyWizardPresenter> {
	}

	@TabInfo(container = PhenotypeDetailTabPresenter.class)
	static TabData getTabLabel(ClientGinjector ginjector) {
		// Priority = 1000, means it will be the right-most tab in the home tab
		TabDataDynamic tabData = new TabDataDynamic("New Study", 1000, "",
				ginjector.getLoggedInGatekeeper());
		tabData.setHasAccess(false);
		return tabData;
	}

	public enum STATE {
		GENOTYPE, PHENOTYPE;
	}

	private STATE state = STATE.GENOTYPE;

	private StudyProxy study;
	protected PhenotypeProxy phenotype;
	protected Long phenotypeId;
	private final CdvManager cdvManager;
	private final PhenotypeManager phenotypeManager;
	private final ObsUnitManager obsUnitManager;
	private final CurrentUser currentUser;
	private boolean fireLoadEvent = false;
	private final PlaceManager placeManager;
	private final Validator validator;
	private Map<Long, List<ObsUnitProxy>> studyStats = new HashMap<Long, List<ObsUnitProxy>>();
	private ListDataProvider<ObsUnitProxy> missingGenotypesDataProvider = new ListDataProvider<ObsUnitProxy>();
	private ListDataProvider<TraitProxy> phenotypesDataProvider = new ListDataProvider<TraitProxy>();
	private MultiSelectionModel<TraitProxy> phenotypeSelectionModel = new MultiSelectionModel<TraitProxy>(
			new EntityProxyKeyProvider<TraitProxy>());
	private SortedMultiset<Double> histogramData = TreeMultiset.create();
	private Multiset<String> geochartData;
	private static int BIN_COUNT = 20;
	private ImmutableList<TraitProxy> phenotypeValues = null;
	private ImmutableList<TraitProxy> filteredPhenotypeValues = null;
	private NamePredicate namePredicate = new NamePredicate("");
	private CountryPredicate countryPredicate = new CountryPredicate();
	private CdvRequest createRequest = null;

	@Inject
	public StudyWizardPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final CdvManager cdvManager,
			final CurrentUser currentUser,
			final PhenotypeManager phenotypeManager,
			final PlaceManager placeManager, final ObsUnitManager obsUnitManager) {
		super(eventBus, view, proxy);
		validator = Validation.buildDefaultValidatorFactory().getValidator();
		getView().setUiHandlers(this);
		this.cdvManager = cdvManager;
		this.obsUnitManager = obsUnitManager;
		this.currentUser = currentUser;
		this.phenotypeManager = phenotypeManager;
		this.placeManager = placeManager;
		getView().setAcceptableValues(
				currentUser.getAppData().getStudyProtocolList(),
				currentUser.getAppData().getAlleleAssayList(),
				currentUser
						.getAppData()
						.getStatisticTypeList()
						.subList(
								1,
								currentUser.getAppData().getStatisticTypeList()
										.size()));
		getView().getPhenotypesDisplay().setSelectionModel(
				phenotypeSelectionModel);
		phenotypeSelectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					@Override
					public void onSelectionChange(SelectionChangeEvent event) {

						showGeoChartData();
						calculatePhenotypeHistogram();
						showPhenotypeHistogram();
					}
				});
	}

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this,
                PhenotypeDetailTabPresenter.TYPE_SetTabContent, this);
    }

	@Override
	protected void onReset() {
		if (fireLoadEvent) {
			fireEvent(new LoadPhenotypeEvent(phenotype));
			fireLoadEvent = false;
		}
		LoadingIndicatorEvent.fire(this, false);
		createRequest = cdvManager.getContext();
		study = createRequest.create(StudyProxy.class);
		getView().getStudyCreateDriver().edit(study, createRequest);
		getView().scheduledLayout();
		// ctx.create(study).with("userPermission").to(receiver);
	}

	@Override
	protected void onBind() {
		super.onBind();
		missingGenotypesDataProvider.addDataDisplay(getView()
				.getMissingGenotypesDisplay());
		getView().resetMissingGenotypesDataGrid();
		phenotypesDataProvider.addDataDisplay(getView().getPhenotypesDisplay());
	}

	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		try {
			final Long phenotypeIdToLoad = Long.valueOf(placeRequest
					.getParameter("id", null));
			if (!phenotypeIdToLoad.equals(phenotypeId)) {
				phenotypeId = phenotypeIdToLoad;
			}
			if (phenotype == null
					|| !phenotype.getId().equals(phenotypeIdToLoad)) {
				phenotypeManager.getContext().findPhenotype(phenotypeIdToLoad)
						.with("userPermission")
						.fire(new Receiver<PhenotypeProxy>() {

							@Override
							public void onSuccess(PhenotypeProxy response) {
								phenotype = response;
								fireLoadEvent = true;
								getProxy().manualReveal(
										StudyWizardPresenter.this);
							}
						});
			} else {
				getProxy().manualReveal(StudyWizardPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new ParameterizedPlaceRequest(
					NameTokens.experiments));
		}
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@ProxyEvent
	void onLoad(LoadPhenotypeEvent event) {
		boolean hasAccess = true;
		phenotype = event.getPhenotype();
		if (!phenotype.getId().equals(phenotypeId)) {
			hasAccess = false;
			study = null;
			studyStats.clear();
			getView().resetMissingGenotypesDataGrid();
		}
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy()
				.getNameToken()).with("id", phenotype.getId().toString());
		String historyToken = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		TabDataDynamic newTabData = new TabDataDynamic("New Study",
				tabData.getPriority(), historyToken);
		newTabData.setHasAccess(hasAccess);
		getProxy().changeTab(newTabData);
	}

	@Override
	public void onCancel() {
		PlaceRequest request = new ParameterizedPlaceRequest(
				NameTokens.studylist).with("id", phenotype.getId().toString());
		placeManager.revealPlace(request);
		resetState();

	}

	@Override
	public void onNext() {
		switch (state) {
		case GENOTYPE:
			getView().getStudyCreateDriver().flush();
			@SuppressWarnings("unchecked")
			Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
					.validate(study, Default.class);
			if (!violations.isEmpty()) {
				getView().getStudyCreateDriver().setConstraintViolations(
						violations);
			} else {
				getView().setNextStep();
				if (phenotypeSelectionModel.getSelectedSet().size() == 0) {
					getView().showBlankColumnChart();
					getView().showBlankGeoChart();
				}
				if (phenotypeValues == null) {
					getView().resetPhenotypeDataGrid();
					phenotypeManager.findAllTraitValues(
							new Receiver<List<TraitProxy>>() {

								@Override
								public void onSuccess(List<TraitProxy> response) {
									phenotypeValues = ImmutableList
											.copyOf(response);
									filterAndShowPhenotypeValues(null);
								}

							}, phenotype.getId(), study.getAlleleAssay()
									.getId(), null);
				}
				state = STATE.PHENOTYPE;
			}
			break;
		case PHENOTYPE:
			study.setTraits(phenotypeSelectionModel.getSelectedSet());
			createRequest.saveStudy(study).with("alleleAssay","protocol","userPermission").fire(new Receiver<StudyProxy>() {

				@Override
				public void onFailure(ServerFailure error) {
					fireEvent(new DisplayNotificationEvent("Error",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,DisplayNotificationEvent.DURATION_NORMAL));
				}

				@Override
				public void onConstraintViolation(
						Set<ConstraintViolation<?>> violations) {
					super.onConstraintViolation(violations);
					StringBuilder builder = new StringBuilder();
					for (ConstraintViolation<?> violation:violations) {
						builder.append(violation.getMessage());
					}
					fireEvent(new DisplayNotificationEvent("Warning",builder.toString(),true,DisplayNotificationEvent.LEVEL_WARNING,DisplayNotificationEvent.DURATION_NORMAL));
				}

				@Override
				public void onSuccess(StudyProxy study) {
					fireEvent(new LoadStudyEvent(study));
					resetState();
					placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.study).with("id", study.getId().toString()));
					
				}
				
			});
			break;
		}
	}

	@Override
	public void onPrevious() {
		getView().setPreviousStep();
	}

	@Override
	public void onGenotypeChange(final AlleleAssayProxy value) {
		if (value == null) {
			getView().scheduledLayout();
			getView().resetMissingGenotypesDataGrid();
		} else {
			final int numberOfObsUnits = phenotype.getNumberOfObsUnits()
					.intValue();
			List<ObsUnitProxy> missingGenotypes = studyStats.get(value.getId());
			if (missingGenotypes != null) {
				getView().showAccessionGenotypeOverlapChart(
						numberOfObsUnits - missingGenotypes.size(),
						missingGenotypes.size());
				missingGenotypesDataProvider.setList(missingGenotypes);
				getView().showMissingGenotypes(missingGenotypes);
			} else {
				obsUnitManager.findObsUnitsWithNoGenotype(
						new Receiver<List<ObsUnitProxy>>() {

							@Override
							public void onSuccess(List<ObsUnitProxy> response) {
								studyStats.put(value.getId(), response);
								getView().showAccessionGenotypeOverlapChart(
										numberOfObsUnits - response.size(),
										response.size());
								getView().showMissingGenotypes(response);
								missingGenotypesDataProvider.setList(response);
							}

						}, phenotypeId, value.getId());
			}
		}
	}

	@Override
	public void selectAllPhenotypeValues(Boolean value) {
		for (TraitProxy trait : phenotypesDataProvider.getList()) {
			phenotypeSelectionModel.setSelected(trait, value);
		}
		if (!value) {
			histogramData.clear();
			getView().showBlankColumnChart();
			getView().showBlankGeoChart();
		} else {
			calculatePhenotypeHistogram();
			showPhenotypeHistogram();
			showGeoChartData();
		}
	}

	private void showPhenotypeHistogram() {
		if (histogramData.size() == 0)
			return;
		Double min = histogramData.elementSet().first();
		Double max = histogramData.elementSet().last();
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
					histogramData.subMultiset(lowBound, BoundType.CLOSED,
							upperBound, BoundType.CLOSED).size());
		}
		builder.put(max, 0);
		ImmutableSortedMap<Double, Integer> histogram = builder.build();
		getView().showPhenotypeHistogramChart(histogram);
	}

	private void calculatePhenotypeHistogram() {
		histogramData.clear();
		for (TraitProxy trait : phenotypeSelectionModel.getSelectedSet()) {
			if (trait.getValue() != null) {
				try {
					histogramData.add(Double.parseDouble(trait.getValue()));
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	private void showGeoChartData() {
		ImmutableMultiset.Builder<String> builder = ImmutableMultiset.builder();
		for (TraitProxy trait : phenotypeSelectionModel.getSelectedSet()) {
			try {
				String cty = trait.getObsUnit().getStock().getPassport()
						.getCollection().getLocality().getCountry();
				builder.add(cty);
			} catch (NullPointerException e) {

			}
		}
		geochartData = builder.build();
		getView().showGeoChart(geochartData);
	}

	private void filterAndShowPhenotypeValues(StatisticTypeProxy type) {
		if (phenotypeValues == null || phenotypeValues.size() == 0)
			return;
		if (type == null) {
			type = phenotypeValues.get(0).getStatisticType();
		}
		getView().getStatisticTypeListBox().setValue(type);
		filteredPhenotypeValues = ImmutableList.copyOf(Collections2.filter(
				phenotypeValues, new StatisticTypePredicate(type)));
		ImmutableSet<String> countriesToFilter = ImmutableSet.copyOf(Lists
				.transform(filteredPhenotypeValues,
						new Function<TraitProxy, String>() {
							public String apply(TraitProxy trait) {
								return trait.getObsUnit().getStock()
										.getPassport().getCollection()
										.getLocality().getOrigcty();
							}
						}));
		getView().setCountriesToFilter(countriesToFilter);
		phenotypesDataProvider.setList(filteredPhenotypeValues);
	}

	@Override
	public void onStatisticTypeChanged(StatisticTypeProxy type) {
		filterAndShowPhenotypeValues(type);
	}

	@Override
	public void onSearchName(final String query) {
		List<TraitProxy> listToFilter = null;
		if (namePredicate.getQuery().length() > query.length()) {
			listToFilter = filteredPhenotypeValues;
		} else {
			listToFilter = phenotypesDataProvider.getList();
		}
		namePredicate.setQuery(query);
		getView().getNameSearchTerm().setValue(query);
		phenotypesDataProvider.setList(Lists.newArrayList(Collections2.filter(
				listToFilter, Predicates.and(namePredicate,countryPredicate))));
	}

	@Override
	public void onFilterCountry(String country, boolean selected) {
		if (country == null)
			return;
		
		if (selected) {
			countryPredicate.getCountriesToFilter().put(country, true);
		}
		else {
			if (countryPredicate.getCountriesToFilter().containsKey(country))
				countryPredicate.getCountriesToFilter().remove(country);
		}
		phenotypesDataProvider.setList(Lists.newArrayList(Collections2.filter(
				filteredPhenotypeValues, Predicates.and(namePredicate,countryPredicate))));
	}
	
	private void resetState() {
		this.study = null;
		studyStats.clear();
		getView().resetMissingGenotypesDataGrid();
		phenotypesDataProvider.getList().clear();
		phenotypeValues = null;
		filteredPhenotypeValues = null;
		TabDataDynamic tabData = (TabDataDynamic) getProxy().getTabData();
		if (tabData != null) {
			tabData.setHasAccess(false);
			getProxy().changeTab(tabData);
		}
	}
	
	
}
