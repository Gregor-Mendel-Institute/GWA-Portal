package com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list;

import com.gmi.nordborglab.browser.client.events.FilterModifiedEvent;
import com.gmi.nordborglab.browser.client.mvp.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.DropDownFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TextBoxFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.PassportProxyPredicates;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportSearchCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampStatProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
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
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class PassportListPresenter extends
        Presenter<PassportListPresenter.MyView, PassportListPresenter.MyProxy> implements PassportListViewUiHandlers {

    public interface MyView extends View, HasUiHandlers<PassportListViewUiHandlers> {
        HasData<PassportProxy> getPassportDisplay();

        void initDataGrid(PassportProxyFilter passportProxyFilter);

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

        public void setSampStatId(Long sampStatId) {
            this.sampStatId = sampStatId;
        }

        public void setPassportId(Long passportId) {
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
            for (Long alleleAssayId : alleleAssayIds) {
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
    private final FilterPresenterWidget filterPresenterWidget;
    private final DropDownFilterItemPresenterWidget genotypeFilterWidget;
    public static final Object TYPE_FilterContent = new Object();

    @Inject
    public PassportListPresenter(final EventBus eventBus, final MyView view,
                                 final MyProxy proxy, final PassportDataProvider dataProvider,
                                 final PlaceManager placeManager, final CurrentUser currentUser,
                                 final FilterPresenterWidget filterPresenterWidget,
                                 final Provider<DropDownFilterItemPresenterWidget> dropDownFilterProvider,
                                 final Provider<TextBoxFilterItemPresenterWidget> textBoxFilterProvider) {
        super(eventBus, view, proxy, GermplasmPresenter.TYPE_SetMainContent);
        this.filterPresenterWidget = filterPresenterWidget;
        this.dataProvider = dataProvider;
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        dataProvider.setPassportProxyFilter(passportProxyFilter);
        getView().setUiHandlers(this);
        getView().initDataGrid(passportProxyFilter);

        TextBoxFilterItemPresenterWidget idFilterWidget = textBoxFilterProvider.get();
        idFilterWidget.setFilterType(ConstEnums.FILTERS.ID);

        TextBoxFilterItemPresenterWidget nameFilterWidget = textBoxFilterProvider.get();
        nameFilterWidget.setFilterType(ConstEnums.FILTERS.PASSPORT_NAME);


        TextBoxFilterItemPresenterWidget collectorFilterWidget = textBoxFilterProvider.get();
        collectorFilterWidget.setFilterType(ConstEnums.FILTERS.PASSPORT_COLLECTOR);


        TextBoxFilterItemPresenterWidget countryFilterWidget = textBoxFilterProvider.get();
        countryFilterWidget.setFilterType(ConstEnums.FILTERS.COUNTRY);

        DropDownFilterItemPresenterWidget typeFilterWidget = dropDownFilterProvider.get();
        typeFilterWidget.setFilterType(ConstEnums.FILTERS.PASSPORT_TYPE);
        typeFilterWidget.setAvailableOptions(Lists.newArrayList(Iterables.filter(Iterables.transform(currentUser.getAppData().getSampStatList(), new Function<SampStatProxy, String[]>() {
            @Nullable
            @Override
            public String[] apply(@Nullable SampStatProxy sampStatProxy) {
                if (sampStatProxy == null)
                    return null;
                String[] retvalue = {sampStatProxy.getGermplasmType(), sampStatProxy.getId().toString()};
                return retvalue;
            }
        }), Predicates.notNull())));


        genotypeFilterWidget = dropDownFilterProvider.get();
        genotypeFilterWidget.setFilterType(ConstEnums.FILTERS.GENOTYPE);
        genotypeFilterWidget.setAvailableOptions(Lists.newArrayList(Iterables.filter(Iterables.transform(currentUser.getAppData().getAlleleAssayList(), new Function<AlleleAssayProxy, String[]>() {
            @Nullable
            @Override
            public String[] apply(@Nullable AlleleAssayProxy alleleAssay) {
                if (alleleAssay == null)
                    return null;
                String[] retvalue = {alleleAssay.getName(), alleleAssay.getId().toString()};
                return retvalue;
            }
        }), Predicates.notNull())));
        List<FilterItemPresenterWidget> filterWidgets = Lists.newArrayList();
        filterWidgets.add(idFilterWidget);
        filterWidgets.add(nameFilterWidget);
        filterWidgets.add(collectorFilterWidget);
        filterWidgets.add(countryFilterWidget);
        filterWidgets.add(genotypeFilterWidget);
        filterWidgets.add(typeFilterWidget);
        filterPresenterWidget.setHasMultiple(false);
        filterPresenterWidget.setFilterItemWidgets(filterWidgets);
        genotypeFilterWidget.setHasMultiple(true);
    }


    @Override
    protected void onBind() {
        super.onBind();
        dataProvider.addDataDisplay(getView().getPassportDisplay());
        setInSlot(TYPE_FilterContent, filterPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(FilterModifiedEvent.TYPE, filterPresenterWidget, new FilterModifiedEvent.Handler() {
            @Override
            public void onFilterModified(FilterModifiedEvent event) {
                boolean expanding = true;
                boolean isDirty = true;
                passportProxyFilter.reset();
                List<FilterItem> filterItems = filterPresenterWidget.getActiveFilterItems();
                for (FilterItem filterItem : filterItems) {
                    switch (filterItem.getType()) {
                        case ID:
                            passportProxyFilter.setPassportId(Long.parseLong(filterItem.getValues().get(0).getText()));
                            break;
                        case PASSPORT_NAME:
                            passportProxyFilter.getNameSearchTerm().setValue(filterItem.getValues().get(0).getText());
                            break;
                        case PASSPORT_COLLECTOR:
                            passportProxyFilter.getCollectorSearchTerm().setValue(filterItem.getValues().get(0).getText());
                            break;
                        case COUNTRY:
                            passportProxyFilter.getCountrySearchTerm().setValue(filterItem.getValues().get(0).getText());
                            break;
                        case PASSPORT_TYPE:
                            passportProxyFilter.setSampStatId(Long.parseLong(filterItem.getValues().get(0).getValue()));
                            break;
                        case GENOTYPE:
                            for (FilterItemValue value : filterItem.getValues()) {
                                passportProxyFilter.getAlleleAssayIds().add(Long.parseLong(value.getValue()));
                            }
                            break;
                        default:
                            isDirty = false;
                            expanding = false;
                            break;
                    }
                }
                passportProxyFilter.setDirty(isDirty);
                passportProxyFilter.setExpanding(expanding);
                if (isDirty) {
                    onStartSearch();
                }
            }
        }));
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
            final Long taxonomyIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
            final Long alleleAssayIdToLoad = Long.valueOf(placeRequest.getParameter("alleleAssayId", null));
            AlleleAssayProxy alleleAssayProxy = null;
            if (!taxonomyIdToLoad.equals(taxonomyId)) {
                preSelectedAlleleAssayId = alleleAssayIdToLoad;
                filterPresenterWidget.reset(false);
                taxonomyId = taxonomyIdToLoad;
                dataProvider.setTaxonomyId(taxonomyId);
                if (preSelectedAlleleAssayId != 0) {
                    alleleAssayProxy = getGentoypeFromId(preSelectedAlleleAssayId);
                    genotypeFilterWidget.setFilterItemValue(Lists.newArrayList(new FilterItemValue(alleleAssayProxy.getName(), preSelectedAlleleAssayId.toString())));
                }
                getView().getPassportDisplay().setVisibleRangeAndClearData(getView().getPassportDisplay().getVisibleRange(), true);
            } else if (!alleleAssayIdToLoad.equals(preSelectedAlleleAssayId)) {
                filterPresenterWidget.reset(false);
                preSelectedAlleleAssayId = alleleAssayIdToLoad;
                if (preSelectedAlleleAssayId != 0) {
                    alleleAssayProxy = getGentoypeFromId(preSelectedAlleleAssayId);
                    genotypeFilterWidget.setFilterItemValue(Lists.newArrayList(new FilterItemValue(alleleAssayProxy.getName(), preSelectedAlleleAssayId.toString())));
                }
            }

            getProxy().manualReveal(PassportListPresenter.this);
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.taxonomies).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onStartSearch() {
        Range range = getView().getPassportDisplay().getVisibleRange();
        getView().getPassportDisplay().setVisibleRangeAndClearData(range, true);

    }

    private AlleleAssayProxy getGentoypeFromId(Long id) {
        for (AlleleAssayProxy alleleAssayProxy : currentUser.getAppData().getAlleleAssayList()) {
            if (alleleAssayProxy != null && id.equals(alleleAssayProxy.getId())) {
                return alleleAssayProxy;
            }
        }
        return null;
    }
}
