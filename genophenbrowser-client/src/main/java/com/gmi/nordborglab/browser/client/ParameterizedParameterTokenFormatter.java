package com.gmi.nordborglab.browser.client;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatException;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jroper
 * Date: 30.03.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class ParameterizedParameterTokenFormatter implements TokenFormatter {
    protected static final String DEFAULT_HIERARCHY_SEPARATOR = "&";
    protected static final String DEFAULT_PARAM_SEPARATOR = ";";
    protected static final String DEFAULT_VALUE_SEPARATOR = "=";

    private final String hierarchySeparator;
    private final String paramSeparator;
    private final String valueSeparator;

    /**
     * Builds a {@link ParameterTokenFormatter} using the default separators.
     */
    @Inject
    public ParameterizedParameterTokenFormatter() {
        this(DEFAULT_HIERARCHY_SEPARATOR, DEFAULT_PARAM_SEPARATOR,
                DEFAULT_VALUE_SEPARATOR);
    }

    /**
     * This constructor makes it possible to use custom separators in your token
     * formatter. The separators must be 1-letter strings, they must all be
     * different from one another, and they must be encoded when ran through
     * {@link com.google.gwt.http.client.URL#encodeQueryString(String)})
     *
     * @param hierarchySeparator The symbol used to separate {@link com.gwtplatform.mvp.client.proxy.PlaceRequest}
     *                           in a hierarchy. Must be a 1-character string and can't be {@code %}.
     * @param paramSeparator     The symbol used to separate parameters in a
     *                           {@link com.gwtplatform.mvp.client.proxy.PlaceRequest}. Must be a 1-character string and can't be {@code %}.
     * @param valueSeparator     The symbol used to separate the parameter name from
     *                           its value. Must be a 1-character string and can't be {@code %}.
     */
    public ParameterizedParameterTokenFormatter(String hierarchySeparator,
                                                String paramSeparator, String valueSeparator) {

        assert hierarchySeparator.length() == 1;
        assert paramSeparator.length() == 1;
        assert valueSeparator.length() == 1;
        assert !hierarchySeparator.equals(paramSeparator);
        assert !hierarchySeparator.equals(valueSeparator);
        assert !paramSeparator.equals(valueSeparator);
        assert !valueSeparator.equals(URL.encodeQueryString(valueSeparator));
        assert !hierarchySeparator.equals(URL.encodeQueryString(hierarchySeparator));
        assert !paramSeparator.equals(URL.encodeQueryString(paramSeparator));
        assert !hierarchySeparator.equals("%");
        assert !paramSeparator.equals("%");
        assert !valueSeparator.equals("%");

        this.hierarchySeparator = hierarchySeparator;
        this.paramSeparator = paramSeparator;
        this.valueSeparator = valueSeparator;
    }

    @Override
    public String toHistoryToken(List<PlaceRequest> placeRequestHierarchy)
            throws TokenFormatException {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < placeRequestHierarchy.size(); ++i) {
            if (i != 0) {
                out.append(hierarchySeparator);
            }
            out.append(toPlaceToken(placeRequestHierarchy.get(i)));
        }
        return out.toString();
    }

    @Override
    public PlaceRequest toPlaceRequest(String placeToken)
            throws TokenFormatException {
        PlaceRequest req = null;

        int split = placeToken.indexOf(paramSeparator);
        if (split == 0) {
            throw new TokenFormatException("Place history token is missing.");
        } else if (split == -1) {
            req = new ParameterizedPlaceRequest(placeToken);
        } else if (split >= 0) {
            req = new ParameterizedPlaceRequest(placeToken.substring(0, split));
            String paramsChunk = placeToken.substring(split + 1);
            String[] paramTokens = paramsChunk.split(paramSeparator);
            for (String paramToken : paramTokens) {
                if (paramToken.isEmpty()) {
                    throw new TokenFormatException(
                            "Bad parameter: Successive parameters require a single '"
                                    + paramSeparator + "' between them.");
                }
                String[] param = splitParamToken(paramToken);
                String key = unescape(param[0]);
                String value = unescape(param[1]);
                req = req.with(key, value);
            }
        }
        return req;
    }

    @Override
    public List<PlaceRequest> toPlaceRequestHierarchy(String historyToken)
            throws TokenFormatException {
        int split = historyToken.indexOf(hierarchySeparator);
        if (split == 0) {
            throw new TokenFormatException("Place history token is missing.");
        } else {
            List<PlaceRequest> result = new ArrayList<PlaceRequest>();
            if (split == -1) {
                result.add(toPlaceRequest(historyToken)); // History token consists of a single place token
            } else {
                String[] placeTokens = historyToken.split(hierarchySeparator);
                for (String placeToken : placeTokens) {
                    if (placeToken.isEmpty()) {
                        throw new TokenFormatException(
                                "Bad parameter: Successive place tokens require a single '"
                                        + hierarchySeparator + "' between them.");
                    }
                    result.add(toPlaceRequest(placeToken));
                }
            }
            return result;
        }
    }

    @Override
    public String toPlaceToken(PlaceRequest placeRequest) {
        return formatPlaceToken(placeRequest);
    }

    public static String formatPlaceToken(PlaceRequest placeRequest) {
        String token = placeRequest.getNameToken();
        if (placeRequest instanceof ParameterizedPlaceRequest) {
            for (String name : ((ParameterizedPlaceRequest) placeRequest).getPathParameterNames()) {
                String value = placeRequest.getParameter(name, null);
                if (value != null) {
                    token = token.replace("{" + name + "}", value);
                }
            }
        }
        StringBuilder out = new StringBuilder();
        out.append(token);

        Set<String> params = placeRequest.getParameterNames();
        if (params != null) {
            for (String name : params) {
                out.append(DEFAULT_PARAM_SEPARATOR)
                        .append(escape(name)).append(DEFAULT_VALUE_SEPARATOR)
                        .append(escape(placeRequest.getParameter(name, null)));
            }
        }
        return out.toString();
    }

    private static String escape(String value) {
        return URL.encodeQueryString(value);
    }

    private String unescape(String value) {
        return URL.decodeQueryString(value);
    }

    private String[] splitParamToken(String paramToken) {
        String[] param = paramToken.split(valueSeparator, 2);
        if (param.length == 1                       // pattern didn't match
                || param[0].contains(valueSeparator)    // un-escaped separator encountered in the key
                || param[1].contains(valueSeparator)) { // un-escaped separator encountered in the value
            throw new TokenFormatException(
                    "Bad parameter: Parameters require a single '" + valueSeparator
                            + "' between the key and value.");
        }
        return param;
    }

}