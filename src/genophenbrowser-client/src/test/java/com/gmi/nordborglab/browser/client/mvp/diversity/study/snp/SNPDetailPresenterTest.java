package com.gmi.nordborglab.browser.client.mvp.diversity.study.snp;

import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.widgets.snps.SNPDetailPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.SNPGWASInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.GWASDataRequest;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jukito.TestSingleton;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


/**
 * Created by uemit.seren on 3/13/15.
 */
public class SNPDetailPresenterTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
            bind(CurrentUser.class).in(TestSingleton.class);
            bindMock(SNPDetailPresenterWidget.class).in(TestSingleton.class);
        }
    }

    @Inject
    SNPDetailPresenter presenter;

    @Inject
    SNPDetailPresenter.MyView view;

    @Inject
    CurrentUser currentUser;

    @Inject
    SNPDetailPresenter.MyProxy proxy;

    @Inject
    CustomRequestFactory rf;

    @Inject
    CdvRequest cdvContext;

    @Inject
    Request<StudyProxy> cdvRq;

    @Inject
    GWASDataRequest gwasContext;

    @Inject
    Request<SNPGWASInfoProxy> gwasRq;


    @Inject
    SNPDetailPresenterWidget snpDetailPresenterWidget;

    private final PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.snps).with("id", "1").with("chr", "1").with("position", "100").build();

    @Before
    public void setup() {
        given(rf.cdvRequest()).willReturn(cdvContext);
        given(cdvContext.findStudy(anyLong())).willReturn(cdvRq);
        given(cdvRq.with(CdvManager.FULL_PATH)).willReturn(cdvRq);
        given(cdvRq.getRequestContext()).willReturn(cdvContext);

        given(rf.gwasDataRequest()).willReturn(gwasContext);
        given(gwasContext.getSNPGWASInfoByStudyId(anyLong(), anyInt(), anyInt())).willReturn(gwasRq);
    }


    @Test
    public void test_onBind_SetPresenterWidgetInSlot() {

        presenter.onBind();
        verify(view).setInSlot(presenter.SLOT_SNP_DETAIL, snpDetailPresenterWidget);
    }

    @Test
    public void test_onLoadStudy_Changed() {
        StudyProxy study = createStudy();
        LoadStudyEvent event = new LoadStudyEvent(study);
        presenter.dataLoaded = true;
        presenter.onLoadStudy(event);
        assertThat(presenter.study, is(study));
        assertThat(presenter.dataLoaded, is(false));
    }

    @Test
    public void test_onLoadStudy_NotChanged() {
        StudyProxy study = createStudy();
        LoadStudyEvent event = new LoadStudyEvent(study);
        presenter.dataLoaded = true;
        presenter.study = study;
        presenter.onLoadStudy(event);
        assertThat(presenter.study, is(study));
        assertThat(presenter.dataLoaded, is(true));
    }

    @Test
    public void test_prepareFromRequest_With_Unchanged_Data() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(placeRequest);
        presenter.study = createStudy();
        presenter.chr = 1;
        presenter.position = 100;
        presenter.dataLoaded = true;
        presenter.onBind();
        presenter.prepareFromRequest(placeRequest);
        verify(cdvRq, never()).fire(any(Receiver.class));
        verify(proxy).manualReveal(presenter);
        assertThat(presenter.study, notNullValue());
        assertThat(presenter.dataLoaded, is(true));
    }

    @Test
    public void test_prepareFromRequest_From_Empty_State() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(placeRequest);
        final StudyProxy study = createStudy();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<StudyProxy> receiver = (Receiver<StudyProxy>) args[0];
                receiver.onSuccess(study);
                return null;
            }
        }).when(cdvRq).to(any(Receiver.class));

        presenter.onBind();
        presenter.prepareFromRequest(placeRequest);
        verify(cdvContext).findStudy(1L);
        verify(cdvRq).with(CdvManager.FULL_PATH);
        verify(cdvContext).fire();
        verify(proxy).manualReveal(presenter);
        assertThat(presenter.study.getId(), is(1L));
        assertThat(presenter.dataLoaded, is(false));
        assertThat(presenter.chr, is(1));
        assertThat(presenter.position, is(100));
    }

    @Test
    public void test_onReset_FireLoadEvent() {
        presenter.fireLoadEvent = true;
        presenter.study = createStudy();
        presenter.onReset();
        assertThat(eventBus().getFiredCount(LoadStudyEvent.getType()), is(1));
        LoadStudyEvent event = (LoadStudyEvent) Iterables.get(eventBus().getFiredEvents(), 0);
        assertThat(event.getStudy(), is(presenter.study));
    }

    @Test
    public void test_onReset_DataIsNotLoaded_LoadData() {
        presenter.study = createStudy();
        presenter.chr = 1;
        presenter.position = 100;
        final SNPGWASInfoProxy snpGWASInfo = mock(SNPGWASInfoProxy.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<SNPGWASInfoProxy> receiver = (Receiver<SNPGWASInfoProxy>) args[0];
                receiver.onSuccess(snpGWASInfo);
                return null;
            }
        }).when(gwasRq).fire(any(Receiver.class));


        presenter.onReset();
        assertThat(presenter.dataLoaded, is(true));
        verify(gwasRq).fire(any(Receiver.class));
        verify(snpDetailPresenterWidget).setData(1, 100, 1L, presenter.study.getTraits());
        verify(snpDetailPresenterWidget).setSNPGWASInfo(snpGWASInfo);
    }

    @Test
    public void test_prepareFromRequest_WrongParameter_RedirectToExperiments() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.snps).build();
        presenter.prepareFromRequest(request);
        verify(proxy, never()).manualReveal(presenter);
        verify(proxy).manualRevealFailed();
        verify(placeManager).revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());

    }


    private StudyProxy createStudy() {
        StudyProxy study = mock(StudyProxy.class, RETURNS_DEEP_STUBS);
        given(study.getId()).willReturn(1L);
        given(study.getAlleleAssay().getId()).willReturn(1L);
        Set<TraitProxy> traits = Sets.newHashSet();
        given(study.getTraits()).willReturn(traits);
        return study;
    }


}
