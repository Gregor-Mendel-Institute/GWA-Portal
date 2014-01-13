package com.gmi.nordborglab.browser.client.dispatch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.gwtplatform.dispatch.rpc.shared.Result;

public abstract class RequestBuilderActionImpl<R extends Result> implements RequestBuilderAction<R> {

    public static String BaseURL = GWT.getHostPageBaseURL() + "gwasdata";

    @Override
    public String getServiceName() {
        return BaseURL;
    }

    @Override
    public boolean isSecured() {
        return false;
    }

    @Override
    public Method getMethod() {
        return RequestBuilder.GET;
    }


    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getRequestData() {
        return null;
    }
}
