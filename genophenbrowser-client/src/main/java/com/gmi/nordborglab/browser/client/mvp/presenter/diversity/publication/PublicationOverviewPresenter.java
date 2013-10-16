package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.PublicationPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/2/13
 * Time: 6:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationOverviewPresenter extends
        Presenter<PublicationOverviewPresenter.MyView, PublicationOverviewPresenter.MyProxy> {

    public interface MyView extends View {

        HasData<PublicationProxy> getDisplay();

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.publications)
    public interface MyProxy extends ProxyPlace<PublicationOverviewPresenter> {
    }

    protected final AsyncDataProvider<PublicationProxy> dataProvider;
    protected final ExperimentManager experimentManager;

    @Inject
    public PublicationOverviewPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                        final ExperimentManager experimentManager) {
        super(eventBus, view, proxy);
        this.experimentManager = experimentManager;
        dataProvider = new AsyncDataProvider<PublicationProxy>() {

            @Override
            protected void onRangeChanged(HasData<PublicationProxy> display) {
                requestPublications(display.getVisibleRange());
            }
        };
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
    }

    private void requestPublications(final Range range) {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<PublicationPageProxy> receiver = new Receiver<PublicationPageProxy>() {
            @Override
            public void onSuccess(PublicationPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) studies.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studies.getContents());
            }
        };
        experimentManager.findAllPublications(receiver, range.getStart(), range.getLength());
    }

}
