package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.SNPDetailUiHandlers;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAlleleInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAnnotProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPGWASInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import java.util.List;

public class SNPDetailPresenter extends Presenter<SNPDetailPresenter.MyView, SNPDetailPresenter.MyProxy> implements SNPDetailUiHandlers {

    public interface MyView extends View, HasUiHandlers<SNPDetailUiHandlers> {

        HasData<SNPAllele> getSNPAlleleDisplay();

        void scheduledLayout();

        void setExplorerData(List<SNPAllele> snpAllelSNPes);

        void displaySNPInfo(SNPGWASInfoProxy response);

        void setPhenotypeRange(Range<Double> valueRange);

        void displayAlleInfo(SNPAnnotProxy snpAnnot);
    }
    @ContentSlot
    public static final Type<RevealContentHandler<?>> SLOT_SNPDetailView = new Type<RevealContentHandler<?>>();

    @NameToken(NameTokens.snps)
    @ProxyCodeSplit
    public interface MyProxy extends ProxyPlace<SNPDetailPresenter> {
    }

    protected StudyProxy study;
    protected Long studyId;
    protected Integer chr;
    protected Integer position;
    protected List<Long> passportIds;

    protected ImmutableMap<Long, PassportProxy> id2Passport;
    protected ImmutableMap<Long, PassportProxy> idToPassport;
    protected boolean dataLoaded = false;
    protected final PlaceManager placeManager;
    protected boolean fireLoadEvent = false;
    protected final CdvManager cdvManager;
    protected ListDataProvider<SNPAllele> dataProvider = new ListDataProvider<>();
    private Ordering<SNPAllele> snpOrdering = new Ordering<SNPAllele>() {
        @Override
        public int compare(@Nullable SNPAllele left, @Nullable SNPAllele right) {
            Double value1 = 0d;
            Double value2 = 0d;
            try {
                value1 = Double.valueOf(left.getPhenotype());
            } catch (Exception e) {

            }
            try {
                value2 = Double.valueOf(right.getPhenotype());
            } catch (Exception e) {

            }

            return Doubles.compare(value1, value2);
        }
    };


    @Inject
    SNPDetailPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, CdvManager cdvManager) {
        super(eventBus, view, proxy, StudyTabPresenter.TYPE_SetTabContent);
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
        getView().setUiHandlers(this);
        dataProvider.addDataDisplay(getView().getSNPAlleleDisplay());
    }


    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireEvent(new LoadStudyEvent(study));
            fireLoadEvent = false;
        }
        final Integer chrToLoad = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter("chr", null));
        final Integer positionToLoad = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter("position", null));
        passportIds = Lists.newArrayList(Collections2.transform(study.getTraits(), new Function<TraitProxy, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable TraitProxy o) {
                return o.getObsUnit().getStock().getPassport().getId();
            }
        }));

        if (!dataLoaded || chrToLoad != chr || positionToLoad != position) {
            fireEvent(new LoadingIndicatorEvent(true));
            chr = chrToLoad;
            position = positionToLoad;
            getView().getSNPAlleleDisplay().setRowCount(0, false);
            cdvManager.requestFactory().annotationDataRequest().getSNPAlleleInfo(study.getAlleleAssay().getId(), chr, position, passportIds).fire(new Receiver<SNPAlleleInfoProxy>() {
                @Override
                public void onSuccess(SNPAlleleInfoProxy response) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    dataLoaded = true;
                    displayData(response);
                }
            });
            cdvManager.requestFactory().gwasDataRequest().getSNPGWASInfoByStudyId(study.getId(), chr, position).fire(new Receiver<SNPGWASInfoProxy>() {
                @Override
                public void onSuccess(SNPGWASInfoProxy response) {
                    getView().displaySNPInfo(response);
                }
            });

        }
    }

    private void displayData(SNPAlleleInfoProxy alleleInfo) {
        List<SNPAllele> snpAlleles = getListFromAlleleInfo(alleleInfo);
        //getView().getSNPAlleleDisplay().setVisibleRangeAndClearData(getView().getSNPAlleleDisplay().getVisibleRange(), true);

        double maxValue = Double.valueOf(snpOrdering.max(snpAlleles).getPhenotype());
        double minValue = Double.valueOf(snpOrdering.min(snpAlleles).getPhenotype());
        getView().setPhenotypeRange(Range.closed(minValue, maxValue));
        dataProvider.setList(snpAlleles);
        Integer score = 0;
        getView().displayAlleInfo(alleleInfo.getSnpAnnot());
        getView().setExplorerData(snpAlleles);
        getView().scheduledLayout();
    }

    private List<SNPAllele> getListFromAlleleInfo(final SNPAlleleInfoProxy alleleInfo) {
        ImmutableMap.Builder<Long, Byte> passport2ByteBuilder = new ImmutableMap.Builder<>();
        for (int i = 0; i < alleleInfo.getAlleles().size(); i++) {
            passport2ByteBuilder.put(passportIds.get(i), alleleInfo.getAlleles().get(i));
        }
        ImmutableMap<Long, Byte> passport2Byte = passport2ByteBuilder.build();
        List<SNPAllele> allelesInfo = Lists.newArrayList();
        SNPAnnotProxy annot = alleleInfo.getSnpAnnot();
        int i = 1;
        for (TraitProxy trait : study.getTraits()) {
            PassportProxy passport = trait.getObsUnit().getStock().getPassport();
            String allele = passport2Byte.get(passport.getId()) == 1 ? annot.getAlt() : annot.getRef();
            allelesInfo.add(new SNPAllele(i, passport, allele, trait.getValue()));
            i++;
        }
        return allelesInfo;
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }


    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
        try {
            final Long studyIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
            if (!studyIdToLoad.equals(studyId)) {
                studyId = studyIdToLoad;
                dataLoaded = false;
                passportIds = null;
                chr = null;
                position = null;
            }
            if (dataLoaded) {
                getProxy().manualReveal(SNPDetailPresenter.this);
                return;
            }
            if (study == null || !study.getId().equals(studyIdToLoad)) {
                cdvManager.findOne(new Receiver<StudyProxy>() {

                    @Override
                    public void onSuccess(StudyProxy response) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        study = response;
                        initMap();
                        fireLoadEvent = true;
                        getProxy().manualReveal(SNPDetailPresenter.this);
                    }


                }, studyIdToLoad);
            } else {
                getProxy().manualReveal(SNPDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    private void initMap() {
    }

    @ProxyEvent
    public void onLoad(LoadStudyEvent event) {
        study = event.getStudy();
        if (study == null || study.getTraits() == null) {
            return;
        }
        if (!study.getId().equals(studyId))
            dataLoaded = false;
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", study.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        /*TabData tabData = getProxy().getTabData();
        TabDataDynamic newTabData = new TabDataDynamic("Plot (" + study.getProtocol().getAnalysisMethod() + ")", tabData.getPriority(), historyToken);
        boolean hasPlots = study.getJob() != null && study.getJob().getStatus().equalsIgnoreCase("Finished");
        newTabData.setHasAccess(hasPlots);
        getProxy().changeTab(newTabData); */
    }

}
