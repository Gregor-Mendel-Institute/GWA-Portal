package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.PassportListViewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.ui.HighlightCell.SearchTerm;
import com.gmi.nordborglab.browser.client.util.PassportProxyPredicates;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportSearchCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampStatProxy;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class PassportListPresenter extends
		Presenter<PassportListPresenter.MyView, PassportListPresenter.MyProxy> implements PassportListViewUiHandlers{

	public interface MyView extends View,HasUiHandlers<PassportListViewUiHandlers> {

		HasData<PassportProxy> getPassportDisplay();

		void initDataGrid(PassportProxyFilter passportProxyFilter);

		void setCountriesToFilter(Set<String> countries);
		void setSampstatsToFilter(List<SampStatProxy> sampStats);

		void setAlleleAssaysToFilter(List<AlleleAssayProxy> alleleAssays);

		void setSelectedAlleleAssayId(Long preSelectedAlleleAssayId, boolean startSearch);
		void initMap();
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.passports)
	public interface MyProxy extends ProxyPlace<PassportListPresenter> {
	}
	
	public static class PassportProxyFilter {

		private SearchTerm nameSearchTerm = new SearchTerm();
		private SearchTerm accNumberSearchTerm = new SearchTerm();
		private SearchTerm collectorSearchTerm = new SearchTerm();
		private SearchTerm sourceSearchTerm = new SearchTerm();
		private SearchTerm countrySearchTerm = new SearchTerm();
		private Long sampStatId = null;
		private Long passportId = null;
		private List<Long> alleleAssayIds = new ArrayList<Long>();
		
		private boolean isDirty = false;
		private boolean isExpanding = false;
		
		public PassportProxyFilter() {
			
		}
		
		public SearchTerm getNameSearchTerm() {
			return nameSearchTerm;
		}

		public SearchTerm getAccNumberSearchTerm() {
			return accNumberSearchTerm;
		}

		public SearchTerm getCollectorSearchTerm() {
			return collectorSearchTerm;
		}

		public SearchTerm getSourceSearchTerm() {
			return sourceSearchTerm;
		}
		
		public void setSampStatId(Long sampStatId)  {
			this.sampStatId = sampStatId;
		}
		
		public void setPassportId(Long passportId)  {
			this.passportId = passportId;
		}

		public PassportSearchCriteriaProxy apply(PassportSearchCriteriaProxy passportSearchCriteriaProxy) {
			passportSearchCriteriaProxy.setPassportId(passportId);
			passportSearchCriteriaProxy.setAccName(nameSearchTerm.getValue());
			passportSearchCriteriaProxy.setAccNumber(accNumberSearchTerm.getValue());
			passportSearchCriteriaProxy.setCollector(collectorSearchTerm.getValue());
			passportSearchCriteriaProxy.setSource(sourceSearchTerm.getValue());
			passportSearchCriteriaProxy.setCountries(Lists.newArrayList(countrySearchTerm.getValue()));
			passportSearchCriteriaProxy.setSampStatId(sampStatId);
			passportSearchCriteriaProxy.setAlleleAssayIds(alleleAssayIds);
			return passportSearchCriteriaProxy;
		}

		public boolean isDirty() {
			return isDirty;
		}

		public void setDirty(boolean isDirty) {
			this.isDirty = isDirty;
		}

		public boolean isExpanding() {
			return isExpanding;
		}

		public void setExpanding(boolean isExpanding) {
			this.isExpanding = isExpanding;
		}

		public Iterable<Predicate<PassportProxy>> getPredicates() {
			List<Predicate<PassportProxy>> predicates = new ArrayList<Predicate<PassportProxy>>();
			predicates.add(PassportProxyPredicates.accNameContains(nameSearchTerm.getValue()));
			predicates.add(PassportProxyPredicates.idEquals(passportId));
			predicates.add(PassportProxyPredicates.sampStatIdEquals(sampStatId));
			predicates.add(PassportProxyPredicates.accNumberContains(accNumberSearchTerm.getValue()));
			predicates.add(PassportProxyPredicates.collectorContains(collectorSearchTerm.getValue()));
			predicates.add(PassportProxyPredicates.sourceContains(sourceSearchTerm.getValue()));
			predicates.add(PassportProxyPredicates.countryContains(countrySearchTerm.getValue()));
			for (Long alleleAssayId:alleleAssayIds) {
				predicates.add(PassportProxyPredicates.alleleAssayIdEquals(alleleAssayId));
			}
			return predicates;
		}

		public SearchTerm getCountrySearchTerm() {
			return countrySearchTerm;
		}
		
		public List<Long> getAlleleAssayIds() {
			return alleleAssayIds;
		}
		
		public void reset() {
			alleleAssayIds.clear();
			nameSearchTerm.setValue("");
			accNumberSearchTerm.setValue("");
			collectorSearchTerm.setValue("");
			sourceSearchTerm.setValue("");
			countrySearchTerm.setValue("");
			sampStatId = null;
			passportId = null;
			isDirty = false;
			isExpanding = false;
		}

		public Long getPassportId() {
			return passportId;
		}
	}
	
	
	private final PassportDataProvider dataProvider;
	private final PlaceManager placeManager;
	private Long taxonomyId;
	private Long preSelectedAlleleAssayId = 0L;
	private PassportProxyFilter passportProxyFilter = new PassportProxyFilter();
	private final CurrentUser currentUser;
	 

	@Inject
	public PassportListPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PassportDataProvider dataProvider, 
			final PlaceManager placeManager,final CurrentUser currentUser) {
		super(eventBus, view, proxy);
		this.dataProvider = dataProvider;
		this.placeManager = placeManager;
		this.currentUser = currentUser;
		dataProvider.setPassportProxyFilter(passportProxyFilter);
		getView().setUiHandlers(this);
		getView().initDataGrid(passportProxyFilter);
		getView().setSampstatsToFilter(currentUser.getAppData().getSampStatList());
		getView().setAlleleAssaysToFilter(currentUser.getAppData().getAlleleAssayList());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, GermplasmPresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
		getView().initMap();
	}
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		try {
			final Long taxonomyIdToLoad = Long.valueOf(placeRequest.getParameter("id",null));
			final Long alleleAssayIdToLoad = Long.valueOf(placeRequest.getParameter("alleleAssayId", null));
			if (!taxonomyIdToLoad.equals(taxonomyId)) {
				preSelectedAlleleAssayId = alleleAssayIdToLoad;
				passportProxyFilter.reset();
				taxonomyId = taxonomyIdToLoad;
				dataProvider.setTaxonomyId(taxonomyId);
				if (preSelectedAlleleAssayId != 0) {
					passportProxyFilter.setDirty(true);
					passportProxyFilter.getAlleleAssayIds().add(preSelectedAlleleAssayId);
					getView().setSelectedAlleleAssayId(preSelectedAlleleAssayId,false);
				}
				if (dataProvider.getDataDisplays().contains(getView().getPassportDisplay()))
					dataProvider.removeDataDisplay(getView().getPassportDisplay());
				dataProvider.addDataDisplay(getView().getPassportDisplay());
			}
			else if (!alleleAssayIdToLoad.equals(preSelectedAlleleAssayId)) {
				passportProxyFilter.reset();
				passportProxyFilter.setDirty(true);
				passportProxyFilter.setExpanding(true);
				preSelectedAlleleAssayId = alleleAssayIdToLoad;
				if (preSelectedAlleleAssayId != 0)
					passportProxyFilter.getAlleleAssayIds().add(preSelectedAlleleAssayId);
				getView().setSelectedAlleleAssayId(preSelectedAlleleAssayId,true);
			}
			
			getProxy().manualReveal(PassportListPresenter.this);
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.taxonomies));
		}
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void onStartSearch() {
		Range range =  getView().getPassportDisplay().getVisibleRange();
		getView().getPassportDisplay().setVisibleRangeAndClearData(range, true);
		
	}
}
