package com.gmi.nordborglab.browser.client.mvp.view.diversity.meta;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAnnotProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.06.13
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public interface MetaSNPAnalysisDataGridColumns {


    public static class AnalysisColumn extends HyperlinkPlaceManagerColumn<MetaSNPAnalysisProxy> {

        public AnalysisColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaSNPAnalysisProxy object) {
            String name = object.getAnalysis();
            PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.study).with("id", object.getAnalysisId().toString());
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    public static class PhenotypeColumn extends HyperlinkPlaceManagerColumn<MetaSNPAnalysisProxy> {

        public PhenotypeColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaSNPAnalysisProxy object) {
            String name = object.getPhenotype();
            PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.phenotype).with("id", object.getPhenotypeId().toString());
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    public static class StudyColumn extends HyperlinkPlaceManagerColumn<MetaSNPAnalysisProxy> {

        public StudyColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaSNPAnalysisProxy object) {
            String name = object.getStudy();
            PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.experiment).with("id", object.getStudyId().toString());
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    public static class GenotypeColumn extends Column<MetaSNPAnalysisProxy, String> {

        public GenotypeColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaSNPAnalysisProxy object) {
            return object.getGenotype();
        }
    }

    public static class MethodColumn extends Column<MetaSNPAnalysisProxy, String> {

        public MethodColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaSNPAnalysisProxy object) {
            return object.getMethod();
        }
    }

    public static class SNPColumn extends Column<MetaSNPAnalysisProxy, String> {

        public SNPColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaSNPAnalysisProxy object) {
            SNPAnnotProxy snpAnnot = object.getSnpAnnotation();
            return snpAnnot.getPosition() + " [" + snpAnnot.getAnnotation() + "]";

        }
    }

    public static class GeneColumn extends HyperlinkPlaceManagerColumn<MetaSNPAnalysisProxy> {

        public GeneColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaSNPAnalysisProxy object) {
            String gene = object.getSnpAnnotation().getGene();
            if (gene == null || gene.isEmpty()) {
                return null;
            }
            PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.metaAnalysisGenes).with("id", gene);
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(gene, url);

        }
    }


}
