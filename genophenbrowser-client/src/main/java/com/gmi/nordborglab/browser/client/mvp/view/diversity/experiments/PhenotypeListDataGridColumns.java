package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface PhenotypeListDataGridColumns {

    public static class NameColumn extends HyperlinkPlaceManagerColumn<PhenotypeProxy> {

        private final PlaceRequest placeRequest;


        public NameColumn(final PlaceManager placeManager, final PlaceRequest placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkParam getValue(PhenotypeProxy object) {
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
            String name = object.getLocalTraitName();
            return new HyperlinkParam(name, url);
        }

    }

    public static class ProtocolColumn extends Column<PhenotypeProxy, String> {

        public ProtocolColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PhenotypeProxy object) {
            return object.getTraitProtocol();
        }

    }

    public static class TraitOntologyColumn extends Column<PhenotypeProxy, String> {

        public TraitOntologyColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PhenotypeProxy object) {
            String to = "";
            if (object.getTraitOntologyTerm() != null) {
                to = object.getTraitOntologyTerm().getName() + " (" + object.getTraitOntologyTerm().getAcc() + ")";
            }
            return to;
        }
    }

    public static class ExperimentColumn extends TextColumn<PhenotypeProxy> {

        @Override
        public String getValue(PhenotypeProxy object) {
            String experiment = "";
            if (object.getExperiment() != null) {
                experiment = object.getExperiment().getName();
            }
            return experiment;
        }
    }

}
