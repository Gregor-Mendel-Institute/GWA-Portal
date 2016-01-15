package com.gmi.nordborglab.browser.server.servlet;

import com.google.web.bindery.requestfactory.server.ExceptionHandler;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by uemit.seren on 1/15/16.
 */
public class LoggingAwareRequestFactoryServlet extends RequestFactoryServlet {

    static class RequestFactoryExceptionHandler implements ExceptionHandler {
        private static final Logger LOG = LoggerFactory.getLogger(RequestFactoryExceptionHandler.class);

        @Override
        public ServerFailure createServerFailure(Throwable throwable) {
            LOG.error("Server error", throwable);
            return new ServerFailure(
                    "Server Error: " + (throwable == null ? null : throwable.getMessage()), null, null, true);
        }
    }

    public LoggingAwareRequestFactoryServlet() {
        super(new RequestFactoryExceptionHandler());
    }
}