package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.client.mvp.widgets.snps.SNPDetailPresenterWidget;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.AnnotationDataRequest;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.gmi.nordborglab.browser.shared.service.SearchRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jukito.All;
import org.jukito.TestSingleton;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class SNPViewerPresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
            bind(CurrentUser.class).in(TestSingleton.class);
            bindManyNamedInstances(String.class, "valid_regions", "AT1G12321", "Chr1:1-10");
            bindMock(SNPDetailPresenterWidget.class).in(TestSingleton.class);
            bindMock(GoogleAnalyticsManager.class).in(TestSingleton.class);
        }
    }

    @Inject
    SNPViewerPresenter presenter;

    @Inject
    SNPViewerPresenter.MyView view;


    @Inject
    CurrentUser currentUser;

    @Inject
    HasData<SNPInfoProxy> snpsDisplay;

    @Inject
    SNPDetailPresenterWidget snpDetailPresenter;

    @Inject
    CustomRequestFactory rf;

    @Inject
    PhenotypeRequest phenotypeCtx;

    @Inject
    Request<PhenotypeProxy> phenotypeRq;

    @Inject
    AnnotationDataRequest annotationCtx;

    @Inject
    Request<SNPInfoPageProxy> annotationRq;

    @Inject
    SNPInfoPageProxy snpInfoPage;


    private final PlaceRequest currentEmtpyPlace = new PlaceRequest.Builder().nameToken(NameTokens.snpviewer).build();
    private final PlaceRequest currentPlaceWithGenotype = new PlaceRequest.Builder().nameToken(NameTokens.snpviewer).with("genotype", "1").build();
    private final PlaceRequest currentPlaceWithPhenotype = new PlaceRequest.Builder().nameToken(NameTokens.snpviewer).with("phenotype", "1").build();
    private final PlaceRequest currentPlaceWithFullFilter = new PlaceRequest.Builder().nameToken(NameTokens.snpviewer).with("genotype", "1").with("phenotype", "1").with("region", "AT1G12321").with("chr", "2").with("position", "100").build();

    @Before
    public void setup() {
        mockAppData();
        given(view.getSNPSDisplay()).willReturn(snpsDisplay);
        given(snpsDisplay.getVisibleRange()).willReturn(new Range(0, 10));
        //mock phenotype request
        given(phenotypeCtx.findPhenotype(anyLong())).willReturn(phenotypeRq);
        given(phenotypeRq.with("traits.obsUnit.stock.passport.collection.locality")).willReturn(phenotypeRq);
        given(rf.phenotypeRequest()).willReturn(phenotypeCtx);
        given(phenotypeRq.getRequestContext()).willReturn(phenotypeCtx);

        //mock annotationrequest
        given(annotationCtx.getSNPInfosForFilter(anyLong(), anyString(), anyInt(), anyInt(), anyListOf(Long.class))).willReturn(annotationRq);
        given(rf.annotationDataRequest()).willReturn(annotationCtx);

        given(snpInfoPage.getTotalElements()).willReturn(100L);
        List<SNPInfoProxy> items = Lists.newArrayList(mock(SNPInfoProxy.class));
        given(snpInfoPage.getContents()).willReturn(items);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<SNPInfoPageProxy> receiver = (Receiver<SNPInfoPageProxy>) args[0];
                receiver.onSuccess(snpInfoPage);
                return null;
            }
        }).when(annotationRq).fire(any(Receiver.class));

    }

    @Test
    public void test_setGenotypesAndDataDisplayOnBind() {
        presenter.onBind();
        verify(view).setAvailableGenotypes(currentUser.getAppData().getAlleleAssayList());
        verify(view).setInSlot(presenter.TYPE_SetSNPDetailContent, snpDetailPresenter);
    }


    @Test
    public void test_PlaceChangeAfterNullGenotype() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithGenotype);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentPlaceWithGenotype).without("genotype").build();
        presenter.onSelectAlleleAssay(null);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterNonNullGenotype() {
        AlleleAssayProxy alleleAssay = mock(AlleleAssayProxy.class);
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        given(alleleAssay.getId()).willReturn(1L);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentEmtpyPlace).with("genotype", "1").build();
        presenter.onSelectAlleleAssay(alleleAssay);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterNonNullPhenotype() {
        SearchItemProxy searchItem = mock(SearchItemProxy.class);
        SearchSuggestOracle.SearchSuggestion suggestion = new SearchSuggestOracle.SearchSuggestion(searchItem, null);
        given(searchItem.getId()).willReturn("1");
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentEmtpyPlace).with("phenotype", "1").build();
        presenter.onSelectPhenotype(suggestion);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterNullPhenotype() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentPlaceWithFullFilter).without("phenotype").build();
        presenter.onSelectPhenotype(null);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterNotNullSNP() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        SNPInfoProxy snp = mock(SNPInfoProxy.class);
        given(snp.getChr()).willReturn("2");
        given(snp.getPosition()).willReturn(100L);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentEmtpyPlace).with("chr", snp.getChr()).with("position", String.valueOf(snp.getPosition())).build();
        presenter.onSelectSNP(snp);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterNullSNP() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentPlaceWithFullFilter).without("chr").without("position").build();
        presenter.onSelectSNP(null);
        verify(placeManager).revealPlace(newRequest);
    }


    @Test
    public void test_NoPlaceChangeAndShowErrorsAfterInvalidRegion() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        presenter.onSelectRegion("WRONG_REGION");
        verify(placeManager, never()).revealPlace(any(PlaceRequest.class));
        verify(view).showRegionError();
    }

    @Test
    public void test_PlaceChangeAfterNullRegion() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentPlaceWithFullFilter).without("region").build();
        presenter.onSelectRegion(null);
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterEmptyRegion() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentPlaceWithFullFilter).without("region").build();
        presenter.onSelectRegion("");
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_PlaceChangeAfterValidRegion(@All("valid_regions") String region) {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        presenter.onSelectRegion(region);
        PlaceRequest newRequest = new PlaceRequest.Builder(currentEmtpyPlace).with("region", region).build();
        verify(placeManager).revealPlace(newRequest);
    }

    @Test
    public void test_searchForPhenotypesDisplayHits() {
        SearchRequest searchRequest = mock(SearchRequest.class);
        Request<SearchFacetPageProxy> rfRequest = mock(Request.class);
        final SearchFacetPageProxy searchResult = mock(SearchFacetPageProxy.class);
        SearchItemProxy searchItem = mock(SearchItemProxy.class);
        SuggestOracle.Callback callback = mock(SuggestOracle.Callback.class);
        ArgumentCaptor<SuggestOracle.Response> responseCaptor = ArgumentCaptor.forClass(SuggestOracle.Response.class);
        List<SearchItemProxy> items = Lists.newArrayList(searchItem);
        given(searchResult.getContents()).willReturn(items);
        given(rf.searchRequest()).willReturn(searchRequest);
        given(searchRequest.searchByFilter(any(String.class), any(ConstEnums.FILTERS.class))).willReturn(rfRequest);
        SuggestOracle.Request request = new SuggestOracle.Request("test");
        willDoNothing().given(callback).onSuggestionsReady(eq(request), responseCaptor.capture());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<SearchFacetPageProxy> receiver = (Receiver<SearchFacetPageProxy>) args[0];
                receiver.onSuccess(searchResult);
                return null;
            }
        }).when(rfRequest).fire(any(Receiver.class));

        presenter.onSearchPhenotype(request, callback);

        verify(searchRequest).searchByFilter("test", ConstEnums.FILTERS.PHENOTYPE);
        verify(rfRequest).fire(any(Receiver.class));
        verify(callback).onSuggestionsReady(eq(request), any(SuggestOracle.Response.class));
        SuggestOracle.Response response = responseCaptor.getValue();
        assertThat(response.getSuggestions().size(), is(1));
        SuggestOracle.Suggestion suggestion = Iterables.get(response.getSuggestions(), 0);
        assertThat(suggestion, instanceOf(SearchSuggestOracle.SearchSuggestion.class));
        SearchSuggestOracle.SearchSuggestion searchSuggestion = (SearchSuggestOracle.SearchSuggestion) suggestion;
    }

    @Test
    public void test_onResetNoDataToLoad() {
        mockAppData();
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentEmtpyPlace);
        presenter.onBind();
        presenter.onReset();
        assertThat(presenter.snpsDataProvider.getDataDisplays().size(), is(1));
        assertThat(Iterables.getFirst(presenter.snpsDataProvider.getDataDisplays(), null), is(snpsDisplay));

        assertThat(presenter.alleleAssayId, nullValue());
        assertThat(presenter.phenotype, nullValue());
        assertThat(presenter.region, nullValue());
        verify(view).setRegion(null);
        verify(view).setGenotype(null);
        verify(view).setPhenotype(null);
        verify(view).showDefaultLoadingIndicator(false);

        verify(snpsDisplay).setRowCount(0, false);
    }

    @Test
    public void test_onResetAllParameters() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        AlleleAssayProxy alleleAssayProxy = currentUser.getAppData().getAlleleAssayList().get(1);
        Range range = view.getSNPSDisplay().getVisibleRange();
        presenter.phenotype = createPhenotypeData();
        List<Long> passportIds = getPassportIdsFromTrait(presenter.phenotype.getTraits());


        presenter.onBind();
        presenter.onReset();

        assertThat(presenter.snpsDataProvider.getDataDisplays().size(), is(1));
        assertThat(Iterables.getFirst(presenter.snpsDataProvider.getDataDisplays(), null), is(snpsDisplay));
        assertThat(presenter.alleleAssayId, is(1L));
        assertThat(presenter.phenotype.getId(), is(1L));
        assertThat(presenter.region, is("AT1G12321"));
        verify(view).setRegion("AT1G12321");
        verify(view).setGenotype(alleleAssayProxy);
        verify(view).showDefaultLoadingIndicator(true);
        verify(annotationCtx).getSNPInfosForFilter(presenter.alleleAssayId, presenter.region, range.getStart(), range.getLength(), passportIds);
        verify(annotationRq).fire(any(Receiver.class));
        verify(snpsDisplay).setRowCount((int) snpInfoPage.getTotalElements(), true);
        verify(snpsDisplay).setRowData(range.getStart(), snpInfoPage.getContents());
    }


    @Test
    public void test_onResetPhenotypeIdSetLoadAndDisplayPhenotype() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithPhenotype);
        final PhenotypeProxy phenotype = mock(PhenotypeProxy.class);
        given(phenotype.getId()).willReturn(1L);
        given(phenotype.getLocalTraitName()).willReturn("Phenotype");
        given(phenotype.getNumberOfObsUnits()).willReturn(100L);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<PhenotypeProxy> receiver = (Receiver<PhenotypeProxy>) args[0];
                receiver.onSuccess(phenotype);
                return null;
            }
        }).when(phenotypeRq).to(any(Receiver.class));


        presenter.onReset();
        verify(phenotypeCtx).findPhenotype(phenotype.getId());
        verify(phenotypeCtx).fire();
        verify(view).setPhenotype(phenotype.getLocalTraitName() + " (" + phenotype.getNumberOfObsUnits() + ")");
    }

    @Test
    public void test_onResetPhenotypeAccessDeniedSetPhenotypeNull() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithPhenotype);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<PhenotypeProxy> receiver = (Receiver<PhenotypeProxy>) args[0];
                receiver.onFailure(new ServerFailure("Access Denied", "AccessDeniedException", "", true));
                return null;
            }
        }).when(phenotypeRq).to(any(Receiver.class));

        presenter.onReset();

        verify(phenotypeCtx).findPhenotype(1L);
        verify(phenotypeCtx).fire();
        verify(view).setPhenotype(null);
        verify(placeManager).updateHistory(new PlaceRequest.Builder(currentPlaceWithPhenotype).without("phenotype").build(), true);
    }

    @Test
    public void test_onReset_SNPSelected_FetchDataOnPresenterWidgetWithTraits() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        presenter.alleleAssayId = 1L;
        presenter.phenotype = createPhenotypeData();
        presenter.onReset();
        assertThat(presenter.chr, is(2));
        assertThat(presenter.position, is(100L));
        verify(snpDetailPresenter).setData(2, 100, 1L, presenter.phenotype.getTraits());
        verify(view).showSNPDetail(true);
    }

    @Test
    public void test_onReset_SNPSelected_FetchDataOnPresenterWidgetWithOutTraits() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(new PlaceRequest.Builder(currentPlaceWithFullFilter).without("phenotype").build());
        presenter.alleleAssayId = 1L;
        presenter.phenotype = null;
        presenter.onReset();
        assertThat(presenter.chr, is(2));
        assertThat(presenter.position, is(100L));
        verify(snpDetailPresenter).setData(2, 100, 1L, null);
        verify(view).showSNPDetail(true);
    }


    @Test
    public void test_onReset_ViewerParameterNotChanged_NoRequest() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        presenter.alleleAssayId = 1L;
        presenter.region = "AT1G12321";
        presenter.onReset();
        verify(annotationRq, never()).fire(any(Receiver.class));
    }

    @Test
    public void test_onReset_DetailParameterNotChanged_NoRequest() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(currentPlaceWithFullFilter);
        presenter.alleleAssayId = 1L;
        presenter.region = "AT1G12321";
        presenter.chr = 2;
        presenter.position = 100L;
        presenter.onReset();
        verify(snpDetailPresenter, never()).setData(anyInt(), anyInt(), anyLong(), anySetOf(TraitProxy.class));
    }


    private AppDataProxy mockAppData() {
        AppDataProxy appData = mock(AppDataProxy.class);
        List<AlleleAssayProxy> alleleAssays = Lists.newArrayList();
        AlleleAssayProxy alleleAssay = mock(AlleleAssayProxy.class);
        given(alleleAssay.getId()).willReturn(1L);
        alleleAssays.add(alleleAssay);
        given(appData.getAlleleAssayList()).willReturn(alleleAssays);
        currentUser.setAppData(appData);
        return appData;
    }

    private PhenotypeProxy createPhenotypeData() {
        PhenotypeProxy phenotype = mock(PhenotypeProxy.class);
        given(phenotype.getId()).willReturn(1L);
        Set<TraitProxy> traits = createTraits();
        given(phenotype.getTraits()).willReturn(traits);
        return phenotype;
    }

    private Set<TraitProxy> createTraits() {
        Set<TraitProxy> traits = Sets.newHashSet();
        TraitProxy trait1 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        given(trait1.getObsUnit().getStock().getPassport().getId()).willReturn(1L);
        TraitProxy trait2 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        given(trait1.getObsUnit().getStock().getPassport().getId()).willReturn(2L);
        traits.add(trait1);
        traits.add(trait2);
        return traits;
    }

    private List<Long> getPassportIdsFromTrait(Set<TraitProxy> traits) {
        return FluentIterable.from(traits)
                .transform(new Function<TraitProxy, Long>() {
                    @Nullable
                    @Override
                    public Long apply(TraitProxy input) {
                        return input.getObsUnit().getStock().getPassport().getId();
                    }
                }).toList();
    }

}
