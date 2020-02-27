package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Created by uemit.seren on 1/8/16.
 */
public class AclEntypoIconActionCell<C extends SecureEntityProxy> extends EntypoIconActionCell<C> {

    final int permission;

    public AclEntypoIconActionCell(String entypoString, ActionCell.Delegate<C> delegate, boolean hasMargin, int permission) {
        super(entypoString, delegate, hasMargin);
        this.permission = permission;
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        if (value.getUserPermission() == null)
            return;
        if ((permission & value.getUserPermission().getMask()) == permission) {
            super.render(context, value, sb);
        }
    }
}
