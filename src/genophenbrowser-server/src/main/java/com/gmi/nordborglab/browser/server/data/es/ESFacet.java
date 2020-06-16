package com.gmi.nordborglab.browser.server.data.es;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class ESFacet {

    protected String name;
    protected long missing;
    protected long total;
    protected long other;
    protected List<ESTermsFacet> terms;

    public ESFacet() {
    }

    public ESFacet(String name, long missing, long total, long other, List<ESTermsFacet> terms) {
        this.name = name;
        this.missing = missing;
        this.total = total;
        this.other = other;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMissing() {
        return missing;
    }

    public long getTotal() {
        return total;
    }

    public long getOther() {
        return other;
    }

    public List<ESTermsFacet> getTerms() {
        return terms;
    }

    public void setMissing(long missing) {
        this.missing = missing;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setOther(long other) {
        this.other = other;
    }

    public void setTerms(List<ESTermsFacet> terms) {
        this.terms = terms;
    }
}
