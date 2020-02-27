package com.gmi.nordborglab.browser.client.dispatch;

import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.Response;
import com.gwtplatform.dispatch.rpc.shared.Action;
import com.gwtplatform.dispatch.rpc.shared.Result;


public interface RequestBuilderAction<R extends Result> extends Action<R> {

    String getUrl();

    Method getMethod();

    R extractResult(Response response);

    String getRequestData();

    String getContentType();
}
