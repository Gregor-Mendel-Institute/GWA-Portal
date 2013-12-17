package com.gmi.nordborglab.browser.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;


/**
 * Created by uemit.seren on 17.12.13.
 */

@ControllerAdvice
public class DefaultControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(DefaultControllerAdvice.class);


    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "General error")
    public void logExceptions(Exception ex, HttpServletResponse response) {
        logger.error("Exception thrown:", ex);
    }


}
