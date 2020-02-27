package com.gmi.nordborglab.browser.server.exceptions;

/**
 * Created by uemit.seren on 4/26/16.
 */
public class Hdf5ServerRestException extends Exception {

    public Hdf5ServerRestException(String message) {
        super(message);
    }

    public Hdf5ServerRestException(String message, Throwable e) {
        super(message, e);
    }
}
