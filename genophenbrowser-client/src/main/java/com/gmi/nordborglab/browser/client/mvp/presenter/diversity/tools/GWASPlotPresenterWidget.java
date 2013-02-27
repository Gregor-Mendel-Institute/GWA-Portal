package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.gmi.nordborglab.browser.client.dispatch.CustomCallback;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionResult;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASPlotUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
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
public class GWASPlotPresenterWidget  extends PresenterWidget<GWASPlotPresenterWidget.MyView> implements GWASPlotUiHandlers{

    public interface MyView extends View,HasUiHandlers<GWASPlotUiHandlers>{

        void drawGWASPlots(GWASDataDTO gwasData);
    }

    private Long id;
    private GetGWASDataAction.TYPE type;
    private final DispatchAsync dispatch;

    @Inject
    public GWASPlotPresenterWidget(EventBus eventBus, MyView view, final DispatchAsync dispatch) {
        super(eventBus, view);
        this.dispatch = dispatch;
        getView().setUiHandlers(this);
    }


    public void loadPlots(Long id,GetGWASDataAction.TYPE type) {
        this.id = id;
        this.type = type;
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        dispatch.execute(new GetGWASDataAction(id,type), new CustomCallback<GetGWASDataActionResult>(getEventBus()) {

            @Override
            public void onSuccess(GetGWASDataActionResult result) {
                getView().drawGWASPlots(result.getResultData());
                LoadingIndicatorEvent.fire(this, false);
            }
        });
    }


}
