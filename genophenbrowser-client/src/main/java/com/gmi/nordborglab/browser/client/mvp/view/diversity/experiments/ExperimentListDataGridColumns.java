package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface ExperimentListDataGridColumns {


    public static class NameColumn extends HyperlinkPlaceManagerColumn<ExperimentProxy> {

        private final PlaceRequest placeRequest;

        public NameColumn(final PlaceManager placeManager, final PlaceRequest placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(ExperimentProxy object) {
            String name = object.getName();
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
            return new HyperlinkPlaceManagerColumn.HyperlinkParam(name, url);
        }
    }

    public static class DesignColumn extends Column<ExperimentProxy, String> {
        public DesignColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            String retval = object.getDesign();
            if (retval != null && retval.length() > 65)
                retval = retval.substring(0, 65) + "...";
            return retval;
        }
    }

    public static class OriginatorColumn extends Column<ExperimentProxy, String> {

        public OriginatorColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            return object.getOriginator();
        }
    }

    public static class CommentsColumn extends Column<ExperimentProxy, String> {
        public CommentsColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            return object.getComments();
        }
    }

}
