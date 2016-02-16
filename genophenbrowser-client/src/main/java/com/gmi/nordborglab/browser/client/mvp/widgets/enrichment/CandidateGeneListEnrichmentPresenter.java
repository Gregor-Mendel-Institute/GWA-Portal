package com.gmi.nordborglab.browser.client.mvp.widgets.enrichment;

import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail.CandidateGeneListEnrichmentPresenterWidget;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
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

    static final PermanentSlot<CandidateGeneListEnrichmentPresenterWidget> SLOT_ENRICHMENT = new PermanentSlot<>();
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
        super(eventBus, view, proxy, SLOT);
        this.rf = rf;
        this.placeManager = placeManager;
        this.SLOT = SLOT;
        this.dataProvider = dataProvider;
        this.candidateGeneListEnrichmentWidget = factory.createCandidateGeneListEnrichmentPresenter(dataProvider);

    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        setInSlot(SLOT_ENRICHMENT, candidateGeneListEnrichmentWidget);
    }

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
