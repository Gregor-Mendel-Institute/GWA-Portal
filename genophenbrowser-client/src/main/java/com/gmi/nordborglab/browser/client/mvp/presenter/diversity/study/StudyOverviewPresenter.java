package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class StudyOverviewPresenter
        extends
        Presenter<StudyOverviewPresenter.MyView, StudyOverviewPresenter.MyProxy> implements StudyOverviewUiHandlers {

    public interface MyView extends View, HasUiHandlers<StudyOverviewUiHandlers> {
        HasData<StudyProxy> getDisplay();
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.studyoverview)
    public interface MyProxy extends ProxyPlace<StudyOverviewPresenter> {

    }

    protected final AsyncDataProvider<StudyProxy> dataProvider;

    protected final CdvManager cdvManager;
    private final PlaceManager placeManager;
    private final FacetSearchPresenterWidget facetSearchPresenterWidget;

    @Inject
    public StudyOverviewPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy, final CdvManager cdvManager, final PlaceManager placeManager,
                                  final FacetSearchPresenterWidget facetSearchPresenterWidget) {
        super(eventBus, view, proxy, DiversityPresenter.TYPE_SetMainContent);
        this.facetSearchPresenterWidget = facetSearchPresenterWidget;
        getView().setUiHandlers(this);
        this.cdvManager = cdvManager;
        this.placeManager = placeManager;
        dataProvider = new AsyncDataProvider<StudyProxy>() {

            @Override
            protected void onRangeChanged(HasData<StudyProxy> display) {
                requestStudies(display.getVisibleRange());
            }
        };
        dataProvider.addDataDisplay(getView().getDisplay());
    }

    protected void requestStudies(final Range range) {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
            @Override
            public void onSuccess(StudyPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                dataProvider.updateRowCount((int) studies.getTotalElements(), true);
                dataProvider.updateRowData(range.getStart(), studies.getContents());
                facetSearchPresenterWidget.displayFacets(studies.getFacets());
            }
        };
        cdvManager.findAll(receiver, ConstEnums.TABLE_FILTER.valueOf(facetSearchPresenterWidget.getFilter()), facetSearchPresenterWidget.getSearchString(), range.getStart(), range.getLength());
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget, facetSearchPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(FacetSearchChangeEvent.TYPE, facetSearchPresenterWidget, new FacetSearchChangeEvent.Handler() {

            @Override
            public void onChanged(FacetSearchChangeEvent event) {
                getView().getDisplay().setVisibleRangeAndClearData(getView().getDisplay().getVisibleRange(), true);
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}
