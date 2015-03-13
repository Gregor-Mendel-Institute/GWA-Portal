package com.gmi.nordborglab.browser.client.mvp.widgets.snps;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAlleleInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.AnnotationDataRequest;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by uemit.seren on 3/9/15.
 */
public class SNPDetailPresenterWidgetTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
        }
    }


    @Inject
    SNPDetailPresenterWidget presenter;


    @Inject
    SNPDetailPresenterWidget.MyView view;

    @Inject
    HasData<SNPAllele> display;

    @Inject
    CustomRequestFactory rf;

    @Inject
    AnnotationDataRequest annotationRequest;

    @Inject
    Request<SNPAlleleInfoProxy> request;

    @Captor
    ArgumentCaptor<List<SNPAllele>> captor;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        given(view.getSNPAlleleDisplay()).willReturn(display);
        given(display.getVisibleRange()).willReturn(new Range(0, 10));
        given(rf.annotationDataRequest()).willReturn(annotationRequest);
        given(request.with("passports.collection.locality")).willReturn(request);
        given(annotationRequest.getSNPAlleleInfo(anyLong(), anyInt(), anyInt(), anyListOf(Long.class), anyBoolean())).willReturn(request);
    }


    @Test
    public void test_AddDisplayToDataProvider() {
        presenter.onBind();
        assertThat(presenter.dataProvider.getDataDisplays().size(), is(1));
    }

    @Test
    public void test_setDataAndCreateMaps() {
        Set<TraitProxy> traits = createTraits();
        presenter.onBind();
        presenter.setData(1, 1, 1L, traits);
        assertThat(presenter.passportIds.size(), is(4));
        assertThat(presenter.passportId2Phenotype.keySet().size(), is(4));
        assertThat(presenter.passportId2Passport.keySet().size(), is(4));
    }

    @Test
    public void test_setSNPGWASInfoUpdateView() {
        presenter.setSNPGWASInfo(null);
        verify(view).displaySNPInfo(null);
    }

    @Test
    public void test_setData_ClearData_WhenNoParameters() {
        presenter.setData(null, null, null, null);
        verify(display).setRowCount(0, false);
        verify(view).displayAlleInfo(null);
        verify(view).displaySNPInfo(null);
    }


    @Test
    public void test_SetData_NoTraits_FetchDataAndDisplay() {
        final SNPAlleleInfoProxy alleleInfo = createAlleleInfos();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<SNPAlleleInfoProxy> receiver = (Receiver<SNPAlleleInfoProxy>) args[0];
                receiver.onSuccess(alleleInfo);
                return null;
            }
        }).when(request).fire(any(Receiver.class));

        presenter.onBind();
        presenter.setData(1, 1, 1L, null);
        verify(display).setRowCount(0, false);
        assertThat(eventBus().getFiredCount(LoadingIndicatorEvent.getType()), is(2));
        verify(annotationRequest).getSNPAlleleInfo(presenter.alleleAssayId, presenter.chr, presenter.position, null, true);
        verify(request).with("passports.collection.locality");
        verify(request).fire(any(Receiver.class));
        verify(view).setPhenotypeRange(com.google.common.collect.Range.closed(0.0, 0.0));
        verify(display, atLeastOnce()).setRowCount(alleleInfo.getAlleles().size(), true);
        verify(display).setRowData(eq(0), captor.capture());
        List<SNPAllele> snpAlles = captor.getValue();
        verify(view).setList(snpAlles);
        verify(view).showPhenotypeColumns(false);
        verify(view).displayAlleInfo(alleleInfo.getSnpInfo());
        verify(view).setExplorerData(snpAlles);
        verify(view).scheduledLayout();
        assertThat(snpAlles.size(), is(4));
        int i = 0;
        for (SNPAllele allele : snpAlles) {
            assertThat(allele.getRowid(), is(i));
            assertThat(allele.getAllele(), is(alleleInfo.getAlleles().get(i) == 1 ? alleleInfo.getSnpInfo().getAlt() : alleleInfo.getSnpInfo().getRef()));
            assertThat(allele.getPhenotype(), nullValue());
            assertThat(allele.getPassport(), is(alleleInfo.getPassports().get(i)));
            i += 1;
        }
    }

    @Test
    public void test_SetData_WithTraits_FetchDataAndDisplay() {
        final SNPAlleleInfoProxy alleleInfo = createAlleleInfos();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Receiver<SNPAlleleInfoProxy> receiver = (Receiver<SNPAlleleInfoProxy>) args[0];
                receiver.onSuccess(alleleInfo);
                return null;
            }
        }).when(request).fire(any(Receiver.class));
        Set<TraitProxy> tratis = createTraits();
        presenter.onBind();
        presenter.setData(1, 1, 1L, tratis);
        verify(display).setRowCount(0, false);
        assertThat(eventBus().getFiredCount(LoadingIndicatorEvent.getType()), is(2));
        verify(annotationRequest).getSNPAlleleInfo(presenter.alleleAssayId, presenter.chr, presenter.position, presenter.passportIds, false);
        verify(request).with("passports.collection.locality");
        verify(request).fire(any(Receiver.class));
        verify(view).setPhenotypeRange(com.google.common.collect.Range.closed(6.0, 20.5));
        verify(display, atLeastOnce()).setRowCount(alleleInfo.getAlleles().size(), true);
        verify(display).setRowData(eq(0), captor.capture());
        List<SNPAllele> snpAlles = captor.getValue();
        verify(view).setList(snpAlles);
        verify(view).showPhenotypeColumns(true);
        verify(view).displayAlleInfo(alleleInfo.getSnpInfo());
        verify(view).setExplorerData(snpAlles);
        verify(view).scheduledLayout();
        assertThat(snpAlles.size(), is(4));
        int i = 0;
        for (SNPAllele allele : snpAlles) {
            assertThat(allele.getRowid(), is(i));
            assertThat(allele.getAllele(), is(alleleInfo.getAlleles().get(i) == 1 ? alleleInfo.getSnpInfo().getAlt() : alleleInfo.getSnpInfo().getRef()));
            assertThat(allele.getPhenotype(), is(presenter.passportId2Phenotype.get(presenter.passportIds.get(i))));
            assertThat(allele.getPassport(), is(presenter.passportId2Passport.get(presenter.passportIds.get(i))));
            i += 1;
        }
    }


    private SNPAlleleInfoProxy createAlleleInfos() {
        SNPAlleleInfoProxy alleleInfo = mock(SNPAlleleInfoProxy.class);
        SNPInfoProxy snpInfo = createSNPInfo();
        given(alleleInfo.getAlleles()).willReturn(createAlleles());
        given(alleleInfo.getPassports()).willReturn(createPassports());
        given(alleleInfo.getSnpInfo()).willReturn(snpInfo);
        return alleleInfo;
    }

    private List<PassportProxy> createPassports() {
        List<PassportProxy> passports = Lists.newArrayList();
        passports.add(mock(PassportProxy.class));
        passports.add(mock(PassportProxy.class));
        passports.add(mock(PassportProxy.class));
        passports.add(mock(PassportProxy.class));
        return passports;
    }

    private SNPInfoProxy createSNPInfo() {
        SNPInfoProxy snpInfo = mock(SNPInfoProxy.class);
        given(snpInfo.getRef()).willReturn("A");
        given(snpInfo.getAlt()).willReturn("T");
        return snpInfo;
    }

    private List<Byte> createAlleles() {
        List<Byte> alleles = Lists.newArrayList();
        Byte allele1 = 1;
        Byte allele2 = 0;
        Byte allele3 = 0;
        Byte allele4 = 1;
        alleles.add(allele1);
        alleles.add(allele2);
        alleles.add(allele3);
        alleles.add(allele4);
        return alleles;
    }


    private Set<TraitProxy> createTraits() {
        Set<TraitProxy> traits = Sets.newHashSet();
        TraitProxy trait1 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        PassportProxy passport1 = mock(PassportProxy.class);
        given(passport1.getId()).willReturn(1L);
        given(trait1.getObsUnit().getStock().getPassport()).willReturn(passport1);
        given(trait1.getValue()).willReturn("8");
        TraitProxy trait2 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        given(trait2.getObsUnit().getStock().getPassport()).willReturn(passport1);
        given(trait2.getValue()).willReturn("4");

        TraitProxy trait3 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        PassportProxy passport3 = mock(PassportProxy.class);
        given(trait3.getValue()).willReturn("DIV/0");
        given(trait3.getObsUnit().getStock().getPassport()).willReturn(passport3);
        given(passport3.getId()).willReturn(3L);

        TraitProxy trait4 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        PassportProxy passport4 = mock(PassportProxy.class);
        given(trait4.getValue()).willReturn("20.5");
        given(passport4.getId()).willReturn(4L);
        given(trait4.getObsUnit().getStock().getPassport()).willReturn(passport4);

        TraitProxy trait5 = mock(TraitProxy.class, RETURNS_DEEP_STUBS);
        PassportProxy passport5 = mock(PassportProxy.class);
        given(trait5.getValue()).willReturn("10.5");
        given(passport5.getId()).willReturn(2L);
        given(trait5.getObsUnit().getStock().getPassport()).willReturn(passport5);

        traits.add(trait1);
        traits.add(trait2);
        traits.add(trait3);
        traits.add(trait4);
        traits.add(trait5);

        return traits;
    }

}
