package com.gmi.nordborglab.browser.client.mvp.view.diversity.meta;

import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAnnotProxy;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

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
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.study)
                    .with("id", object.getAnalysisId().toString()).build();
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
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.phenotype)
                    .with("id", object.getPhenotypeId().toString()).build();
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
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.experiment)
                    .with("id", object.getStudyId().toString()).build();
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
            return snpAnnot.getPosition() + (snpAnnot.getAnnotation() == null ? "" : " [" + snpAnnot.getAnnotation() + "]");

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
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.metaAnalysisGenes)
                    .with("id", gene).build();
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(gene, url);

        }
    }

    public static class MafColumn extends Column<MetaSNPAnalysisProxy, Number> {

        public MafColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(4)));
        }

        @Override
        public Number getValue(MetaSNPAnalysisProxy object) {
            return object.getMaf();
        }
    }

    public static class MacColumn extends Column<MetaSNPAnalysisProxy, Number> {

        public MacColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0)));
        }

        @Override
        public Number getValue(MetaSNPAnalysisProxy object) {
            return object.getMac();
        }
    }


    public static class ChrColumn extends Column<MetaSNPAnalysisProxy, String> {

        public ChrColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaSNPAnalysisProxy object) {
            return object.getSnpAnnotation().getChr();
        }
    }
}
