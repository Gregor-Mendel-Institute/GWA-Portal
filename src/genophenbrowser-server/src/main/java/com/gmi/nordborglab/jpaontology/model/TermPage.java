package com.gmi.nordborglab.jpaontology.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 01.08.13
 * Time: 10:57
 * To change this template use File | Settings | File Templates.
 */
public class TermPage {

    private List<Term> contents;
    long totalElements;

    public TermPage() {

    }

    public TermPage(List<Term> contents, long totalElements) {
        this.contents = contents;
        this.totalElements = totalElements;
    }

    public List<Term> getContents() {
        return contents;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
