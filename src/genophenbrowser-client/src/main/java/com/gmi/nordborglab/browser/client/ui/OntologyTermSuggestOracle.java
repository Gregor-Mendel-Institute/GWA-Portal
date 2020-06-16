package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 01.08.13
 * Time: 10:38
 * To change this template use File | Settings | File Templates.
 */
public abstract class OntologyTermSuggestOracle extends SuggestOracle {

    public OntologyTermSuggestOracle() {
        super();
    }


    public static class Request extends SuggestOracle.Request {
        private String type;

        public Request() {
        }

        public Request(String query, String type) {
            super(query);
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static class OntologySuggestion implements Suggestion {

        public OntologySuggestion() {
        }

        private TermProxy term;

        public OntologySuggestion(TermProxy term) {
            this.term = term;
        }

        public TermProxy getTerm() {
            return term;
        }

        @Override
        public String getDisplayString() {
            return term.getName() + " (" + term.getAcc() + ")";
        }

        @Override
        public String getReplacementString() {
            return term.getName() + " (" + term.getAcc() + ")";
        }
    }
}
