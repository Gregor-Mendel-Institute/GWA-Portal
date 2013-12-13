package com.gmi.nordborglab.browser.client.mvp.presenter;

import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListEnrichmentPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;


/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 06.12.13
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public abstract class CandidateGeneListEnrichmentPresenter<T extends Proxy<?>> extends Presenter<CandidateGeneListEnrichmentPresenter.MyView, T> {

    public interface MyView extends View {
    }

    public static Object TYPE_SetCandidateGeneListEnrichment = new Object();
    protected final GwtEvent.Type<RevealContentHandler<?>> SLOT;

    protected final PlaceManager placeManager;
    protected final CustomRequestFactory rf;
    protected final CandidateGeneListEnrichmentPresenterWidget candidateGeneListEnrichmentWidget;
    protected final EnrichmentProvider dataProvider;
    protected boolean fireLoadEvent = false;

    public CandidateGeneListEnrichmentPresenter(EventBus eventBus, MyView view, T proxy,
                                                CustomRequestFactory rf, PlaceManager placeManager,
                                                final ClientModule.AssistedInjectionFactory factory,
                                                final EnrichmentProvider dataProvider,
                                                final GwtEvent.Type<RevealContentHandler<?>> SLOT) {
        super(eventBus, view, proxy);
        this.rf = rf;
        this.placeManager = placeManager;
        this.SLOT = SLOT;
        this.dataProvider = dataProvider;
        this.candidateGeneListEnrichmentWidget = factory.createCandidateGeneListEnrichmentPresenter(dataProvider);

    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        setInSlot(TYPE_SetCandidateGeneListEnrichment, candidateGeneListEnrichmentWidget);
    }

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, SLOT,
                this);
    }


}
