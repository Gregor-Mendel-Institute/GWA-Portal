package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailView.ExperimentDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailView.ExperimentEditDriver;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.client.testutils.SecurityUtils;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class ExperimentDetailPresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
            bind(ExperimentManager.class).in(Singleton.class);
        }
    }

    @Inject
    ExperimentDetailPresenter presenter;

    @Inject
    ExperimentDetailPresenter.MyView view;

    @Mock
    Tab tab;

    @Mock
    ExperimentDisplayDriver experimentDisplayDriver;

    @Mock
    ExperimentEditDriver experimentEditDriver;

    @Captor
    ArgumentCaptor<ExperimentProxy> captor;


    @Inject
    CurrentUser currentUser;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Ignore
    @Test
    public void testAnnonymousUserHasNoPermissions() {
        assertEquals(0, presenter.getPermission());
    }

    @Test
    public void testUserWithNoPermission() {
        currentUser.setAppUser(SecurityUtils.createUser());
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(NameTokens.experiment)
                .with("id", "1").build();
        presenter.prepareFromRequest(request);
        assertEquals(0, presenter.getPermission());
    }

    @Test
    @Ignore
    public void testRevealOverviewPageWhenNoExperimentId() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.experiment).build();
        presenter.prepareFromRequest(request);
        verify(placeManager).revealRelativePlace(-1);
    }
}
