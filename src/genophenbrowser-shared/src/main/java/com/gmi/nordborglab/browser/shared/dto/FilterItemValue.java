package com.gmi.nordborglab.browser.shared.dto;

import com.gmi.nordborglab.browser.shared.proxy.FilterItemValueProxy;
import com.gmi.nordborglab.browser.shared.service.MetaAnalysisRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 15.10.13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class FilterItemValue {

    protected String text;

    protected String value;

    public FilterItemValue() {
    }

    public FilterItemValue(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FilterItemValueProxy getProxy(MetaAnalysisRequest context) {
        FilterItemValueProxy filterItemValue = context.create(FilterItemValueProxy.class);
        filterItemValue.setText(getText());
        filterItemValue.setValue(getValue());
        return filterItemValue;
    }
}
