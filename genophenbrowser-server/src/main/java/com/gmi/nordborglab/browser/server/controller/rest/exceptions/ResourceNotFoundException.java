package com.gmi.nordborglab.browser.server.controller.rest.exceptions;

/**
 * Created by uemit.seren on 7/6/15.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
