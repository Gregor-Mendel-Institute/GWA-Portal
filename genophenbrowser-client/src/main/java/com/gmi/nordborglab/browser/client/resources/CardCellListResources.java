package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.CellList;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/5/13
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CardCellListResources extends CellList.Resources{

    public interface CardStyle extends CellList.Style {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String DEFAULT_CSS = "cardCellListStyle.css";

        /**
         * Applied to even items.
         */
        String cellListEvenItem();

        /**
         * Applied to the keyboard selected item.
         */
        String cellListKeyboardSelectedItem();

        /**
         * Applied to odd items.
         */
        String cellListOddItem();

        /**
         * Applied to selected items.
         */
        String cellListSelectedItem();

        /**
         * Applied to the widget.
         */
        String cellListWidget();
    }


    @Source(CardStyle.DEFAULT_CSS)
    @Override
    CardStyle cellListStyle();
}
