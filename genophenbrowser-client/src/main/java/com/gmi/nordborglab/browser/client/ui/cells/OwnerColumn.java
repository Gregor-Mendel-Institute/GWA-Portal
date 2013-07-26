package com.gmi.nordborglab.browser.client.ui.cells;

import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentListDataGridColumns;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.07.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class OwnerColumn<T extends SecureEntityProxy> extends Column<SecureEntityProxy, String> {

    public OwnerColumn() {
        super(new TextCell());
    }

    @Override
    public String getValue(SecureEntityProxy object) {
        if (object.isOwner()) {
            return "me";
        } else if (object.getOwnerUser() != null) {
            return object.getOwnerUser().getFirstname() + " " + object.getOwnerUser().getLastname();
        } else {
            return "N/A";
        }
    }
}
