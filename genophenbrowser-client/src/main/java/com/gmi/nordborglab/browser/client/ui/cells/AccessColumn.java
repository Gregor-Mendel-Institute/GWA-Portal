package com.gmi.nordborglab.browser.client.ui.cells;

import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.cellview.client.Column;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.07.13
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class AccessColumn<T extends SecureEntityProxy> extends Column<SecureEntityProxy, String> {
    public AccessColumn() {
        super(new LabelTypeCell(ImmutableMap.<String, LabelType>builder().put("PUBLIC", LabelType.SUCCESS).put("RESTRICTED", LabelType.WARNING).build()));
    }

    @Override
    public String getValue(SecureEntityProxy object) {
        return (object.isPublic() ? "PUBLIC" : "RESTRICTED");
    }
}
