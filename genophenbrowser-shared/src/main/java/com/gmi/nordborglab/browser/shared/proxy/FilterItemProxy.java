package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.10.13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName("com.gmi.nordborglab.browser.shared.dto.FilterItem")
public interface FilterItemProxy extends ValueProxy {

    public List<FilterItemValueProxy> getValues();

    public void setValues(List<FilterItemValueProxy> values);

    void setType(ConstEnums.FILTERS type);

    public ConstEnums.FILTERS getType();


}
