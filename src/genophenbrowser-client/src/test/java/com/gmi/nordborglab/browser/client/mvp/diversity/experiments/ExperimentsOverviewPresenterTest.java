package com.gmi.nordborglab.browser.client.mvp.diversity.experiments;

import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExperimentsOverviewPresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
            bind(ExperimentManager.class).in(Singleton.class);
        }
    }

    @Inject
    ExperimentsOverviewPresenter presenter;

    @Inject
    ExperimentsOverviewPresenter.MyView view;

    @Mock
    HasData<ExperimentProxy> display;


    @Captor
    ArgumentCaptor<List<ExperimentProxy>> captor;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Ignore
    @Test
    public void testLoadExperimentNavigateToNewPlace() {
        ExperimentProxy experiment = mock(ExperimentProxy.class);
        when(experiment.getId()).thenReturn(1L);
        presenter.onBind();
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(NameTokens.experiment)
                .with("id", experiment.getId().toString()).build();
        verify(placeManager).revealPlace(request);
    }

}
