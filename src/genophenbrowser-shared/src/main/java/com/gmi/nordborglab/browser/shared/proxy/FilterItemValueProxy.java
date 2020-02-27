package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.10.13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName("com.gmi.nordborglab.browser.shared.dto.FilterItemValue")
public interface FilterItemValueProxy extends ValueProxy {

    public String getText();

    public String getValue();

    void setValue(String value);

    void setText(String text);
}
