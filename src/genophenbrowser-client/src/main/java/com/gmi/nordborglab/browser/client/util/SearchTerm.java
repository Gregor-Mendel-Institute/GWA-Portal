package com.gmi.nordborglab.browser.client.util;

import com.google.gwt.regexp.shared.RegExp;

/**
* Created with IntelliJ IDEA.
* User: uemit.seren
* Date: 2/6/13
* Time: 10:34 AM
* To change this template use File | Settings | File Templates.
*/
public class SearchTerm {

    private String value ="";

    public SearchTerm() {
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RegExp getSearchRegExp() {
        if (value == null || value == "")
            return null;
        else
            return RegExp.compile("(" + value + ")", "ig");
    }
}
