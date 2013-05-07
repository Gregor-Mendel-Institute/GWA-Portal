package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.publication.PublicationDetailView;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/3/13
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationDetailPresenter extends
        Presenter<PublicationDetailPresenter.MyView, PublicationDetailPresenter.MyProxy> {

    public interface MyView extends View {

        PublicationDetailView.PublicationDisplayDriver getDisplayDriver();

        HasData<ExperimentProxy> getExperimentDisplay();

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.publication)
    public interface MyProxy extends ProxyPlace<PublicationDetailPresenter> {

    }

    private PublicationProxy publication;
    private Set<ExperimentProxy> experiments;
    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;
    private final ListDataProvider<ExperimentProxy> experimentDataProvider = new ListDataProvider<ExperimentProxy>();

    @Inject
    public PublicationDetailPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                      final CustomRequestFactory rf, final PlaceManager placeManager) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.rf = rf;
        experimentDataProvider.addDataDisplay(getView().getExperimentDisplay());
    }


    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent,
                this);
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
            Long id = Long.valueOf(placeRequest.getParameter("id", null));
            if (publication == null || !publication.getId().equals(id)) {
                ExperimentRequest ctx = rf.experimentRequest();
                ctx.findOnePublication(id).to(new Receiver<PublicationProxy>() {
                    @Override
                    public void onSuccess(PublicationProxy publicationProxy) {
                        publication = publicationProxy;
                    }
                });
                ctx.findExperimentsByPublication(id).to(new Receiver<Set<ExperimentProxy>>() {
                    @Override
                    public void onSuccess(Set<ExperimentProxy> response) {
                        experiments = response;
                    }
                });
                ctx.fire(new Receiver<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getProxy().manualReveal(PublicationDetailPresenter.this);
                    }
                    @Override
                    public void onFailure(ServerFailure error) {
                        getProxy().manualRevealFailed();
                        placeManager.revealPlace(new PlaceRequest(
                                NameTokens.publications));
                    }
                });
            } else {
                getProxy().manualReveal(PublicationDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
        }
    }

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        fireEvent(new LoadingIndicatorEvent(false));
        getView().getDisplayDriver().display(publication);
        experimentDataProvider.setList(ImmutableList.copyOf(experiments));
    }
}
