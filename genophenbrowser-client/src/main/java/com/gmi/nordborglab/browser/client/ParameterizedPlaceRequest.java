package com.gmi.nordborglab.browser.client;

import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.*;

/**
 * A place request that may be parameterized, for example:
 * <p/>
 * /user/{id}/friends
 */
public class ParameterizedPlaceRequest extends PlaceRequest {

    private final Map<String, String> params = new HashMap<String, String>();
    private final Map<String, String> pathParams = new HashMap<String, String>();

    public ParameterizedPlaceRequest() {
    }

    public ParameterizedPlaceRequest(String nameToken) {
        super(nameToken);
    }

    private static class ParameterizedPlaceToken {
        private final List<PlaceTokenPart> parts;

        private ParameterizedPlaceToken(List<PlaceTokenPart> parts) {
            this.parts = parts;
        }
    }

    private interface PlaceTokenPart {
        /**
         * Does this part match the given part
         *
         * @param part The part to match against
         * @return true if it matches
         */
        boolean matches(String part);

        /**
         * Get the parameter name
         *
         * @return The parameter name, or null if this is not a parameterized part
         */
        String getParameterName();
    }

    private static class ParameterizedPlaceTokenPart implements PlaceTokenPart {
        private final String parameterName;

        private ParameterizedPlaceTokenPart(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getParameterName() {
            return parameterName;
        }

        @Override
        public boolean matches(String part) {
            return true;
        }
    }

    private static class StaticPlaceTokenPart implements PlaceTokenPart {
        private final String partName;

        private StaticPlaceTokenPart(String partName) {
            this.partName = partName;
        }

        @Override
        public boolean matches(String part) {
            return partName.equals(part);
        }

        @Override
        public String getParameterName() {
            return null;
        }
    }

    @Override
    public boolean matchesNameToken(String nameToken) {
        ParameterizedPlaceToken token = parsePlaceToken(nameToken);
        String[] parts = getNameToken().split("/");
        if (token.parts.size() != parts.length) {
            return false;
        }
        for (int i = 0; i < parts.length; i++) {
            if (!token.parts.get(i).matches(parts[i])) {
                return false;
            }
        }
        for (int i = 0; i < parts.length; i++) {
            String parameterName = token.parts.get(i).getParameterName();
            if (parameterName != null) {
            	if (!pathParams.containsKey(parameterName))
            		pathParams.put(parameterName, parts[i]);
            }
        }
        return true;
    }

    @Override
    public String getParameter(String key, String defaultValue) {
        String val = params.get(key);
        if (val != null) {
            return val;
        }
        val = pathParams.get(key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

    @Override
    public Set<String> getParameterNames() {
        return params.keySet();
    }

    public Set<String> getPathParameterNames() {
        return pathParams.keySet();
    }

    @Override
    public PlaceRequest with(String name, String value) {
        if (getNameToken().contains("{" + name + "}")) {
            pathParams.put(name, value);
        } else {
            params.put(name, value);
        }
        return this;
    }

    private static ParameterizedPlaceToken parsePlaceToken(String placeToken) {
        List<PlaceTokenPart> parts = new ArrayList<PlaceTokenPart>();
        for (String part : placeToken.split("/")) {
            if (part.matches("\\{.*\\}")) {
                String parameterName = part.substring(1, part.length() - 1);
                parts.add(new ParameterizedPlaceTokenPart(parameterName));
            } else {
                parts.add(new StaticPlaceTokenPart(part));
            }
        }
        return new ParameterizedPlaceToken(parts);
    }
}