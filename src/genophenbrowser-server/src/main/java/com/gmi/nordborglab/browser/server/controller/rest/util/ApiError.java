package com.gmi.nordborglab.browser.server.controller.rest.util;

/**
 * Created by uemit.seren on 7/6/15.
 */
public class ApiError {

    private final String type;
    private final String message;

    public ApiError(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
