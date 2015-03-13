package com.gmi.nordborglab.browser.client.mvp.genotype;

import com.gmi.nordborglab.browser.client.mvp.widgets.search.SearchPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.google.inject.Inject;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jukito.All;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Created by uemit.seren on 3/3/15.
 */

public class GenotypePresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
            bindManyInstances(String.class, NameTokens.genomebrowser, NameTokens.snpviewer);
        }
    }

    @Inject
    GenotypePresenter presenter;

    @Inject
    GenotypePresenter.MyView view;

    @Inject
    SearchPresenter searchPresenter;


    @Captor
    ArgumentCaptor<List<BreadcrumbItemProxy>> captor;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_SearchPresenterIsBound() {
        presenter.onBind();
        verify(view).setInSlot(presenter.TYPE_SearchPresenterContent, searchPresenter);
    }

    @Test
    public void test_onResetSetTitleAndActiveMenu(@All String place) {
        given(placeManager.getCurrentPlaceRequest()).willReturn(new PlaceRequest.Builder().nameToken(place).build());
        presenter.onBind();
        presenter.onReset();

        GenotypeView.MENU_ITEM menuItem = getMenuItemFromPlace(place);
        String title = getTitleFromPlace(place);
        verify(view).setActiveMenuItem(menuItem);
        verify(view).setTitle(title);

    }


   /* @Ignore
    @Test
    public void testBreadcrumbs() {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken("phenotype").with("id", "1");
        given(placeManager.getCurrentPlaceRequest()).willReturn(request.build());
        presenter.onBind();
        presenter.onReset();
        verify(view).clearBreadcrumbs(3);
        verify(view).setTitle("Phenotype");
        verify(view).setBreadcrumbs(0, "ALL", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build()));
        verify(view).setBreadcrumbs(1, "Experiment", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.experiment).with("id", "1").build()));
        verify(view).setBreadcrumbs(2, "Phenotype", placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken("phenotype").with("id", "1").build()));
    }

    @Test
    public void testTitleRequestRequired() {
        assertFalse(presenter.titleUpdateRequired(null, null));
        assertFalse(presenter.titleUpdateRequired(null, 1L));
        assertFalse(presenter.titleUpdateRequired("experiments", null));
        presenter.titleType = "experiments";
        assertTrue(presenter.titleUpdateRequired("phenotype", 1L));
        presenter.titleType = "experiment";
        presenter.titleId = 1L;
        assertFalse(presenter.titleUpdateRequired("experiment", 1L));
        assertTrue(presenter.titleUpdateRequired("experiment", 2L));
        assertTrue(presenter.titleUpdateRequired("phenotype", 2L));
        assertTrue(presenter.titleUpdateRequired("phenotype", 1L));

    }    */

    private GenotypeView.MENU_ITEM getMenuItemFromPlace(String place) {
        switch (place) {
            case NameTokens.genomebrowser:
                return GenotypeView.MENU_ITEM.GENOMEBROWSER;
            case NameTokens.snpviewer:
                return GenotypeView.MENU_ITEM.SNPVIEWER;
        }
        throw new RuntimeException("Place " + place + " unkniwn");
    }

    private String getTitleFromPlace(String place) {
        switch (place) {
            case NameTokens.genomebrowser:
                return "Genome Browser";
            case NameTokens.snpviewer:
                return "SNP Viewer";
        }
        throw new RuntimeException("Place " + place + " unkniwn");
    }
}
