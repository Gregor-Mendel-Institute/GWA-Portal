package com.gmi.nordborglab.browser.server.controller.rest.exceptions;

import com.gmi.nordborglab.browser.server.controller.rest.util.ApiError;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/**
 * Created by uemit.seren on 7/6/15.
 */

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandlerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandlerAdvice.class);

    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handleGeneric(Exception exception, WebRequest request) {
        logger.error("REST-Exception", exception);
        return new ApiError("generic_error", Throwables.getRootCause(exception).getMessage());
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiError handleResourceNotFound(Exception exception, WebRequest request) {
        logger.error("Resource not found", exception);
        return new ApiError("not_found", Throwables.getRootCause(exception).getMessage());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleAccessDenied(Exception exception, WebRequest request) {
        logger.error("Access Denied", exception);
        String message = Throwables.getRootCause(exception).getMessage();
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return new ResponseEntity<>(new ApiError("unauthorized", "You need to authenticate"), HttpStatus.UNAUTHORIZED);
        } else {
            return new ResponseEntity<>(new ApiError("forbidden", message), HttpStatus.FORBIDDEN);
        }
    }
}
