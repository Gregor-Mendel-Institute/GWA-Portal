package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.user.cellview.client.SimplePager;
import org.gwtbootstrap3.client.ui.Pagination;

public class NumberedPager extends SimplePager {

    final Pagination pagination;

    public NumberedPager(Pagination pagination) {
        super();
        this.pagination = pagination;
    }


    @Override
    protected void onRangeOrRowCountChanged() {
        pagination.rebuild(this);
    }
}