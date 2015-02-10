package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.arcbees.analytics.shared.Analytics;
import com.gmi.nordborglab.browser.client.dispatch.CustomCallback;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionResult;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.events.GoogleAnalyticsEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASPlotUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/26/13
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASPlotPresenterWidget extends PresenterWidget<GWASPlotPresenterWidget.MyView> implements GWASPlotUiHandlers {

    public interface MyView extends View, HasUiHandlers<GWASPlotUiHandlers> {

        void drawGWASPlots(GWASDataDTO gwasData);

    }

    private Long id;
    private GetGWASDataAction.TYPE type;
    private final DispatchAsync dispatch;
    private final Analytics analytics;

    @Inject
    public GWASPlotPresenterWidget(EventBus eventBus, MyView view, final DispatchAsync dispatch, final Analytics analytics) {
        super(eventBus, view);
        this.dispatch = dispatch;
        this.analytics = analytics;
        getView().setUiHandlers(this);

    }


    public void loadPlots(Long id, GetGWASDataAction.TYPE type) {
        this.id = id;
        this.type = type;
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        analytics.startTimingEvent("GWAS", "View");
        dispatch.execute(new GetGWASDataAction(id, type), new CustomCallback<GetGWASDataActionResult>(getEventBus()) {

            @Override
            public void onSuccess(GetGWASDataActionResult result) {
                getView().drawGWASPlots(result.getResultData());
                LoadingIndicatorEvent.fire(this, false);
                analytics.endTimingEvent("GWAS", "View").userTimingLabel("SUCCESS").go();
                GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("GWAS", "Display", "Type:" + type + ",ID:" + id));
            }

            @Override
            public void onFailure(Throwable caught) {
                analytics.endTimingEvent("GWAS", "View").userTimingLabel("ERROR").go();
                GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("GWAS", "Error - Display", "Type:" + type + ",ID:" + id + ",Error:" + caught.getMessage()));
                //FIXME fix backend to not show HTML error page
                //super.onFailure(caught);
            }
        });
    }

    @Override
    public void onSelectSNP(int chromosome, int xVal, int clientX, int clientY) {
        getEventBus().fireEventFromSource(new SelectSNPEvent(chromosome, xVal, clientX, clientY), this);
    }


}
