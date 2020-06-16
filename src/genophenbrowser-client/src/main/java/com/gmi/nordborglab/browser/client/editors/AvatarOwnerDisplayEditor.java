package com.gmi.nordborglab.browser.client.editors;

import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 19:13
 * To change this template use File | Settings | File Templates.
 */
public class AvatarOwnerDisplayEditor extends CellWidget<AppUserProxy> {

    @Inject
    public AvatarOwnerDisplayEditor(AvatarNameCell cell) {
        super(cell);
        cell.setSize(45);
    }
}
