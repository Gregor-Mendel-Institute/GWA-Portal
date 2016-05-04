package com.gmi.nordborglab.browser.server.exceptions;

/**
 * Created by uemit.seren on 4/26/16.
 */
public class CommandLineException extends Exception {

    public CommandLineException(String message) {
        super(message);
    }

    public CommandLineException(String message, Throwable e) {
        super(message, e);
    }
}
