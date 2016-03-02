package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.github.timeu.gwtlibs.gwasviewer.client.Track;
import com.gmi.nordborglab.browser.client.dispatch.CustomCallback;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionResult;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

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

        void setTracks(Track[] tracks);

        void setTrackData(String id, DataTable data, boolean isStacked, String chr);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public interface Tracks {
        @JsProperty
        Track[] getTracks();
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public interface TracksData {
        @JsProperty
        String getData();
    }

    private Long id;
    private GetGWASDataAction.TYPE type;
    private final DispatchAsync dispatch;
    private final GoogleAnalyticsManager analyticsManager;
    private Track[] tracks;
    private final Promise getGWASStats = new PromiseFunction() {
        @Override
        public void f(Deferred deferred) {
            if (tracks == null) {
                Ajax.get(GWT.getHostPageBaseURL() + "/provider/tracks").done(new Function() {
                    @Override
                    public void f() {
                        String response = arguments(0);
                        Tracks t = JsonUtils.safeEval(response).cast();
                        tracks = t.getTracks();
                        deferred.resolve(tracks);
                    }
                });
            } else {
                deferred.resolve(tracks);
            }
        }
    };

    @Inject
    public GWASPlotPresenterWidget(EventBus eventBus, MyView view, final DispatchAsync dispatch, final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view);
        this.dispatch = dispatch;
        this.analyticsManager = analyticsManager;
        getView().setUiHandlers(this);
    }


    public void loadPlots(Long id, GetGWASDataAction.TYPE type) {
        this.id = id;
        this.type = type;
        getGWASStats.done(new Function() {
            @Override
            public void f() {
                getView().setTracks(tracks);
            }
        });
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        analyticsManager.startTimingEvent("GWAS", "View");
        dispatch.execute(new GetGWASDataAction(id, type), new CustomCallback<GetGWASDataActionResult>(getEventBus()) {

            @Override
            public void onSuccess(GetGWASDataActionResult result) {
                getView().drawGWASPlots(result.getResultData());
                LoadingIndicatorEvent.fire(this, false);
                analyticsManager.endTimingEvent("GWAS", "View", "SUCCESS");
                analyticsManager.sendEvent("GWAS", "Display", "Type:" + type + ",ID:" + id);
            }

            @Override
            public void onFailure(Throwable caught) {
                analyticsManager.endTimingEvent("GWAS", "View", "ERROR");
                analyticsManager.sendError("GWAS", "Type:" + type + ",ID:" + id + ",Error:" + caught.getMessage(), true);
                //FIXME fix backend to not show HTML error page
                //super.onFailure(caught);
            }
        });
    }

    @Override
    public void onSelectSNP(int chromosome, int xVal, int clientX, int clientY) {
        getEventBus().fireEventFromSource(new SelectSNPEvent(chromosome, xVal, clientX, clientY), this);
    }

    @Override
    public void onLoadTrackData(String id, boolean isStacked, String chr) {
        Ajax.get(GWT.getHostPageBaseURL() + "/provider/tracks/" + id + "/" + chr).done(new Function() {
            @Override
            public void f() {
                String response = arguments(0);
                TracksData tracksData = JsonUtils.safeEval(response);
                DataTable data = DataTable.create(tracksData.getData());
                getView().setTrackData(id, data, isStacked, chr);
            }
        });
    }


}
