package com.gmi.nordborglab.browser.client.mvp.diversity.study.snp;

import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.snps.SNPDetailPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.SNPGWASInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.gquery.PromiseRF;
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

public class SNPDetailPresenter extends Presenter<SNPDetailPresenter.MyView, SNPDetailPresenter.MyProxy> implements SNPDetailUiHandlers {

    public interface MyView extends View, HasUiHandlers<SNPDetailUiHandlers> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> SLOT_SNPDetailView = new Type<RevealContentHandler<?>>();

    @NameToken(NameTokens.snps)
    @ProxyCodeSplit
    public interface MyProxy extends ProxyPlace<SNPDetailPresenter> {
    }

    protected StudyProxy study;
    protected Integer chr;
    protected Integer position;
    protected boolean dataLoaded = false;

    protected final PlaceManager placeManager;
    protected boolean fireLoadEvent = false;
    protected final CdvManager cdvManager;
    protected final SNPDetailPresenterWidget snpDetailPresenterWidget;


    @Inject
    SNPDetailPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                       CdvManager cdvManager, final SNPDetailPresenterWidget snpDetailPresenterWidget) {
        super(eventBus, view, proxy, StudyTabPresenter.TYPE_SetTabContent);
        this.snpDetailPresenterWidget = snpDetailPresenterWidget;
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(SLOT_SNPDetailView, snpDetailPresenterWidget);
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireEvent(new LoadStudyEvent(study));
            fireLoadEvent = false;
        }
        if (!dataLoaded) {
            snpDetailPresenterWidget.setData(chr, position, study.getAlleleAssay().getId(), study.getTraits());
            dataLoaded = true;
            cdvManager.requestFactory().gwasDataRequest().getSNPGWASInfoByStudyId(study.getId(), chr, position).fire(new Receiver<SNPGWASInfoProxy>() {
                @Override
                public void onSuccess(SNPGWASInfoProxy response) {
                    snpDetailPresenterWidget.setSNPGWASInfo(response);
                }
            });
        }
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
            Long studyIdToLoad = parseURLs();
            loadStudy(studyIdToLoad)
                    .done(new com.google.gwt.query.client.Function() {
                    @Override
                    public void f() {
                        LoadingIndicatorEvent.fire(SNPDetailPresenter.this, false);
                        study = getArgument(0);
                        getProxy().manualReveal(SNPDetailPresenter.this);
                    }
                    })
                    .fail(new com.google.gwt.query.client.Function() {
                        @Override
                        public void f() {
                            LoadingIndicatorEvent.fire(SNPDetailPresenter.this, false);
                            getProxy().manualRevealFailed();
                            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
                        }
                    });
        } catch (NumberFormatException | NullPointerException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }


    @ProxyEvent
    public void onLoadStudy(LoadStudyEvent event) {
        if (event.getStudy() != study) {
            dataLoaded = false;
            study = event.getStudy();
        }
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", study.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
    }

    private Long parseURLs() {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        final Long studyIdToLoad = Long.valueOf(request.getParameter("id", ""));
        final Integer chrToLoad = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter("chr", null));
        final Integer positionToLoad = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter("position", null));
        dataLoaded = dataLoaded && !isStudyChanged(studyIdToLoad) && chrToLoad.equals(chr) && positionToLoad.equals(position);
        chr = chrToLoad;
        position = positionToLoad;
        return studyIdToLoad;
    }


    private Promise loadStudy(Long studyId) {
        if (isStudyChanged(studyId)) {
            fireLoadEvent = true;
            return new PromiseRF(
                    cdvManager.requestFactory().cdvRequest().findStudy(studyId).with(cdvManager.FULL_PATH)
            );
        }
        return new PromiseFunction() {
            @Override
            public void f(Deferred dfd) {
                dfd.resolve(study);
            }
        };
    }

    private boolean isStudyChanged(Long studyId) {
        return study == null || !studyId.equals(study.getId());
    }


}
