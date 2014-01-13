package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/28/13
 * Time: 6:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PermissionDataGridResources extends CellTable.Resources {


    interface PermissionDataGridStyle extends CellTable.Style {


    }


    @Override
    @Source({"permissionDataGridStyle.css"})
    public PermissionDataGridStyle cellTableStyle();
}