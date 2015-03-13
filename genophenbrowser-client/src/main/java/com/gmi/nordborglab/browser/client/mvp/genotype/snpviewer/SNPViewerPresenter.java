package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.client.mvp.genotype.GenotypePresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.snps.SNPDetailPresenterWidget;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.gquery.PromiseRF;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class SNPViewerPresenter extends Presenter<SNPViewerPresenter.MyView, SNPViewerPresenter.MyProxy> implements SNPViewerUiHandlers {

    public interface MyView extends View, HasUiHandlers<SNPViewerUiHandlers> {
        void setAvailableGenotypes(List<AlleleAssayProxy> alleleAssayList);

        void setGenotype(AlleleAssayProxy genotype);

        void setRegion(String region);

        void showRegionError();

        void clearRegionError();

        HasData<SNPInfoProxy> getSNPSDisplay();

        void setPhenotype(String phenotype);

        void showDefaultLoadingIndicator(boolean show);

        void showSNPDetail(boolean show);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.snpviewer)
    public interface MyProxy extends ProxyPlace<SNPViewerPresenter> {

    }

    public static final Object TYPE_SetSNPDetailContent = new Object();
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;

    protected Integer chr;
    protected Long position;
    protected Long alleleAssayId;
    protected String region;
    protected final CustomRequestFactory rf;
    protected final SNPDetailPresenterWidget snpDetailPresenter;
    protected PhenotypeProxy phenotype;
    protected final GoogleAnalyticsManager analyticsManager;

    protected final AsyncDataProvider<SNPInfoProxy> snpsDataProvider = new AsyncDataProvider<SNPInfoProxy>() {
        @Override
        protected void onRangeChanged(HasData<SNPInfoProxy> display) {
            if (!isFilterSet()) {
                updateRowCount(0, false);
                return;
            }
            analyticsManager.startTimingEvent("SNPViewer", "Load");
            final Range range = display.getVisibleRange();
            rf.annotationDataRequest().getSNPInfosForFilter(alleleAssayId, region, range.getStart(), range.getLength(), getPassportIdsFromTraits()).fire(new Receiver<SNPInfoPageProxy>() {
                @Override
                public void onSuccess(SNPInfoPageProxy response) {
                    analyticsManager.endTimingEvent("SNPViewer", "Load", "SUCCESS");
                    updateRowCount((int) response.getTotalElements(), true);
                    updateRowData(range.getStart(), response.getContents());
                }
            });
        }
    };

    RegExp geneRegExp = RegExp.compile("^AT([1-5]{1})G\\d+");
    RegExp regionRegExp = RegExp.compile("^Chr([1-5]{1}):\\d+-\\d+");

    @Inject
    public SNPViewerPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                              final CurrentUser currentUser,
                              final PlaceManager placeManager,
                              final CustomRequestFactory rf,
                              SNPDetailPresenterWidget snpDetailPresenter,
                              final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view, proxy, GenotypePresenter.TYPE_SetMainContent);
        this.snpDetailPresenter = snpDetailPresenter;
        this.analyticsManager = analyticsManager;
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.rf = rf;
        this.placeManager = placeManager;
    }

    @Override
    public void onBind() {
        super.onBind();
        setInSlot(TYPE_SetSNPDetailContent, snpDetailPresenter);
        getView().setAvailableGenotypes(currentUser.getAppData().getAlleleAssayList());
    }


    @Override
    public void onReset() {
        super.onReset();
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        // Set the genotype from the filter
        final boolean isViewerChanged = parseViewerURL();
        final boolean isDetailSNPChanged = parseSNPChromURL();
        getView().showDefaultLoadingIndicator(isFilterSet());
        getView().setGenotype(getGenotype());
        // set the region from the filter
        getView().setRegion(region);
        loadPhenotype(Longs.tryParse(place.getParameter("phenotype", "")))
                .done(new Function() {
                    @Override
                    public void f() {
                        phenotype = getArgument(0);
                        if (snpsDataProvider.getDataDisplays().contains(getView().getSNPSDisplay())) {
                            if (isViewerChanged)
                                getView().getSNPSDisplay().setVisibleRangeAndClearData(getView().getSNPSDisplay().getVisibleRange(), true);
                        } else {
                            snpsDataProvider.addDataDisplay(getView().getSNPSDisplay());
                        }
                        if (phenotype == null) {
                            getView().setPhenotype(null);
                        } else {
                            getView().setPhenotype(phenotype.getLocalTraitName() + " (" + phenotype.getNumberOfObsUnits() + ")");
                        }
                        if (isDetailSNPChanged || isViewerChanged)
                            setDetailData();

                    }
                })
                .fail(new Function() {
                    @Override
                    public void f() {
                        getView().setPhenotype(null);
                        placeManager.updateHistory(new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest()).without("phenotype").build(), true);
                    }
                });
    }

    private Promise loadPhenotype(Long phenotypeId) {
        boolean isLoad = isPhenotypeChanged(phenotypeId);
        if (phenotypeId == null) {
            phenotype = null;
        }
        if (!isLoad) {
            return new PromiseFunction() {
                @Override
                public void f(Deferred dfd) {
                    dfd.resolve(phenotype);
                }
            };
        }
        return new PromiseRF(rf.phenotypeRequest().findPhenotype(phenotypeId).with("traits.obsUnit.stock.passport.collection.locality"));
    }


    @Override
    public void onSearchPhenotype(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        rf.searchRequest().searchByFilter(request.getQuery(), ConstEnums.FILTERS.PHENOTYPE).fire(new Receiver<SearchFacetPageProxy>() {
            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                SuggestOracle.Response searchResponse = new SuggestOracle.Response();
                Collection<SuggestOracle.Suggestion> suggestions = Lists.newArrayList();
                if (response != null) {
                    for (SearchItemProxy searchItem : response.getContents()) {
                        suggestions.add(new SearchSuggestOracle.SearchSuggestion(searchItem, response));
                    }
                }
                searchResponse.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, searchResponse);
            }
        });
    }

    @Override
    public void onSelectRegion(String region) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        getView().clearRegionError();
        if (region == null || region.isEmpty()) {
            request.without("region");
        } else {
            region = parseRegion(region);
            if (region == null)
                return;
            request.with("region", region);
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectPhenotype(SuggestOracle.Suggestion suggestion) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        SearchSuggestOracle.SearchSuggestion searchSuggestion = (SearchSuggestOracle.SearchSuggestion) suggestion;
        if (searchSuggestion == null) {
            request.without("phenotype");
        } else {
            request.with("phenotype", searchSuggestion.getId());
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectAlleleAssay(AlleleAssayProxy alleleAssay) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        if (alleleAssay == null) {
            request.without("genotype");
        } else {
            request.with("genotype", alleleAssay.getId().toString());
        }
        placeManager.revealPlace(request.build());
    }

    @Override
    public void onSelectSNP(SNPInfoProxy snp) {
        PlaceRequest.Builder request = new PlaceRequest.Builder(placeManager.getCurrentPlaceRequest());
        if (snp == null) {
            request.without("chr").without("position");
        } else {
            request.with("chr", snp.getChr()).with("position", String.valueOf(snp.getPosition()));
        }
        placeManager.revealPlace(request.build());
    }


    private void setDetailData() {
        boolean isSNPSelected = isSNPSelected();
        if (isSNPSelected && isFilterSet()) {
            snpDetailPresenter.setData(chr, position.intValue(), alleleAssayId, phenotype != null ? phenotype.getTraits() : null);
        }
        getView().showSNPDetail(isSNPSelected);
    }

    private boolean isSNPSelected() {
        return chr != null && position != null;
    }

    private List<Long> getPassportIdsFromTraits() {
        if (phenotype == null || phenotype.getTraits() == null)
            return null;
        return FluentIterable.from(phenotype.getTraits())
                .transform(new com.google.common.base.Function<TraitProxy, Long>() {
                    @Nullable
                    @Override
                    public Long apply(TraitProxy input) {
                        if (input != null && input.getObsUnit() != null
                                && input.getObsUnit().getStock() != null && input.getObsUnit().getStock().getPassport() != null) {
                            return input.getObsUnit().getStock().getPassport().getId();
                        }
                        return null;
                    }
                }).filter(Predicates.notNull()).toList();
    }

    private String parseRegion(String region) {
        if (region == null || region.isEmpty())
            return null;
        MatchResult geneMatcher = geneRegExp.exec(region);
        MatchResult regionMatcher = regionRegExp.exec(region);
        if (geneMatcher == null && regionMatcher == null) {
            getView().showRegionError();
            return null;
        }
        return region;
    }

    private AlleleAssayProxy getGenotype() {
        if (alleleAssayId == null)
            return null;
        return FluentIterable.from(currentUser.getAppData().getAlleleAssayList())
                .firstMatch(new Predicate<AlleleAssayProxy>() {
                    @Override
                    public boolean apply(AlleleAssayProxy input) {
                        if (input == null)
                            return false;
                        return alleleAssayId.equals(input.getId());
                    }
                }).orNull();
    }

    private boolean isFilterSet() {
        return alleleAssayId != null && region != null;
    }


    private boolean parseViewerURL() {
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        Long alleleAssayIdToLoad = Longs.tryParse(place.getParameter("genotype", ""));
        String regionToLoad = parseRegion(place.getParameter("region", null));
        Long phenotypeToLoad = Longs.tryParse(place.getParameter("phenotype", ""));
        boolean isChanged = isPhenotypeChanged(phenotypeToLoad) || alleleAssayIdToLoad != alleleAssayId || regionToLoad != region;
        alleleAssayId = alleleAssayIdToLoad;
        region = regionToLoad;
        return isChanged;
    }

    private boolean isPhenotypeChanged(Long phenotypeId) {
        return phenotypeId != null
                && (phenotype == null
                || (phenotype != null && phenotypeId != phenotype.getId())
        );
    }

    private boolean parseSNPChromURL() {
        PlaceRequest place = placeManager.getCurrentPlaceRequest();
        Integer chrToLoad = Ints.tryParse(place.getParameter("chr", ""));
        Long positionToLoad = Longs.tryParse(place.getParameter("position", ""));
        boolean isChanged = chrToLoad != chr || positionToLoad != position;
        chr = chrToLoad;
        position = positionToLoad;
        return isChanged;
    }
}
