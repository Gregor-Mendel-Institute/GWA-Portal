package com.gmi.nordborglab.browser.shared.dto;

import com.gmi.nordborglab.browser.shared.proxy.FilterItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.FilterItemValueProxy;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */


public class FilterItem {

    protected ConstEnums.FILTERS type;

    protected List<FilterItemValue> filters;

    public FilterItem() {
    }

    public FilterItem(ConstEnums.FILTERS type, List<FilterItemValue> filters) {
        this.type = type;
        this.filters = filters;
    }

    public ConstEnums.FILTERS getType() {
        return type;
    }

    public List<FilterItemValue> getValues() {
        return filters;
    }

    public void setValues(List<FilterItemValue> filters) {
        this.filters = filters;
    }

    public void setType(ConstEnums.FILTERS type) {
        this.type = type;
    }

    public FilterItemProxy getProxy(MetaAnalysisRequest context) {
        FilterItemProxy filterItemProxy = context.create(FilterItemProxy.class);
        filterItemProxy.setType(type);
        List<FilterItemValueProxy> filterItemValues = Lists.newArrayList();
        for (FilterItemValue filterItemValue : getValues()) {
            FilterItemValueProxy filterItemValueProxy = filterItemValue.getProxy(context);
            filterItemValues.add(filterItemValueProxy);
        }
        filterItemProxy.setValues(filterItemValues);
        return filterItemProxy;
    }
}
