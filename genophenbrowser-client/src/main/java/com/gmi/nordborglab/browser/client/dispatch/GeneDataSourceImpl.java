package com.gmi.nordborglab.browser.client.dispatch;

import com.github.timeu.gwtlibs.gwasviewer.client.events.FetchGeneInfoCallback;
import com.github.timeu.gwtlibs.gwasviewer.client.events.FetchGenesCallback;
import com.github.timeu.gwtlibs.gwasviewer.client.events.GeneDataSource;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * Created by uemit.seren on 2/29/16.
 */
public class GeneDataSourceImpl implements GeneDataSource {

    private final String url;

    public GeneDataSourceImpl(String url) {
        this.url = url;
    }

    @Override
    public void fetchGenes(String chr, int start, int end, boolean fetchGeneFeatures, FetchGenesCallback fetchGenesCallback) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url + "genes?chromosome=" + chr + "&start=" + start + "&end=" + end + "&isFeatures=" + fetchGeneFeatures);
        request.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                JsArrayMixed data = JsonUtils.safeEval(response.getText());
                fetchGenesCallback.onFetchGenes(data);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                throw new RuntimeException("Error requesting genes");
            }
        });
        try {
            request.send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fetchGeneInfo(String geneId, FetchGeneInfoCallback fetchGeneInfoCallback) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url + "gene/" + geneId);
        request.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                // TODO switch to AutoBean or elemental
                JSONObject data = JSONParser.parseStrict(response.getText()).isObject();
                String description = data.get("description").isString().stringValue();
                fetchGeneInfoCallback.onFetchGeneInfo(description);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                throw new RuntimeException("Error requesting gene info");
            }
        });
        try {
            request.send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
