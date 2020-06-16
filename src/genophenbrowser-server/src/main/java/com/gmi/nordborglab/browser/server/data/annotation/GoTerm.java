package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.09.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class GoTerm {

    private String relation;
    private String exact;
    private String narrow;

    public GoTerm() {
    }

    public GoTerm(String relation, String exact, String narrow) {
        this.relation = relation;
        this.exact = exact;
        this.narrow = narrow;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getExact() {
        return exact;
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    public String getNarrow() {
        return narrow;
    }

    public void setNarrow(String narrow) {
        this.narrow = narrow;
    }
}
