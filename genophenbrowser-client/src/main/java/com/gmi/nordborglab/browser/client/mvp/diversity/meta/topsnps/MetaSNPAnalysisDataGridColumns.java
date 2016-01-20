package com.gmi.nordborglab.browser.client.mvp.diversity.meta.topsnps;

import com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes.MetaAnalysisGeneView;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.AssociationProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.SNPAnnotationProxy;
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


    class AnalysisColumn extends HyperlinkPlaceManagerColumn<MetaAnalysisProxy> {

        public AnalysisColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaAnalysisProxy object) {
            String name = object.getAnalysis();
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.study)
                    .with("id", object.getAnalysisId().toString()).build();
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    class PhenotypeColumn extends HyperlinkPlaceManagerColumn<MetaAnalysisProxy> {

        public PhenotypeColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaAnalysisProxy object) {
            String name = object.getPhenotype();
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.phenotype)
                    .with("id", object.getPhenotypeId().toString()).build();
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    class StudyColumn extends HyperlinkPlaceManagerColumn<MetaAnalysisProxy> {

        public StudyColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaAnalysisProxy object) {
            String name = object.getStudy();
            PlaceRequest request = new PlaceRequest.Builder()
                    .nameToken(NameTokens.experiment)
                    .with("id", object.getStudyId().toString()).build();
            String url = "#" + placeManager.buildHistoryToken(request);
            return new HyperlinkParam(name, url);
        }
    }

    class GenotypeColumn extends Column<MetaAnalysisProxy, String> {

        public GenotypeColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaAnalysisProxy object) {
            return object.getGenotype();
        }
    }

    class MethodColumn extends Column<MetaAnalysisProxy, String> {

        public MethodColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaAnalysisProxy object) {
            return object.getMethod();
        }
    }

    class SNPColumn extends Column<MetaAnalysisProxy, String> {

        public SNPColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaAnalysisProxy object) {
            if (object.getAssociations().size() == 0)
                return null;
            SNPInfoProxy snpInfo = object.getAssociations().get(0).getSnpInfo();
            // TODO adapt to new annotation
            String snpText = String.valueOf(snpInfo.getPosition());
            if (snpInfo.getAnnotations() != null && snpInfo.getAnnotations().size() > 0) {
                SNPAnnotationProxy annotation = snpInfo.getAnnotations().get(0);
                snpText += " [" + annotation.getEffect() + "]";

            }
            return snpText;
        }
    }

    class GeneColumn extends HyperlinkPlaceManagerColumn<MetaAnalysisProxy> {

        public GeneColumn(PlaceManager placeManager) {
            super(new HyperlinkCell(), placeManager);
        }

        @Override
        public HyperlinkParam getValue(MetaAnalysisProxy object) {
            String gene = object.getAssociations().get(0).getSnpInfo().getGene();
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

    class MafColumn extends Column<MetaAnalysisProxy, Number> {

        public MafColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(4)));
        }

        @Override
        public Number getValue(MetaAnalysisProxy object) {
            return object.getAssociations().get(0).getMaf();
        }
    }

    class MacColumn extends Column<MetaAnalysisProxy, Number> {

        public MacColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0)));
        }

        @Override
        public Number getValue(MetaAnalysisProxy object) {
            return object.getAssociations().get(0).getMac();
        }
    }


    class ChrColumn extends Column<MetaAnalysisProxy, String> {

        public ChrColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(MetaAnalysisProxy object) {
            return object.getAssociations().get(0).getSnpInfo().getChr();
        }
    }


    class ScoreColumn extends Column<MetaAnalysisProxy, AssociationProxy> {

        public ScoreColumn() {
            super(new MetaAnalysisGeneView.ScoreCell());
        }

        @Override
        public AssociationProxy getValue(MetaAnalysisProxy object) {
            if (object.getAssociations() != null && object.getAssociations().size() > 0)
                return object.getAssociations().get(0);
            return null;
        }
    }

    class AssocCountColumn extends Column<MetaAnalysisProxy, Number> {

        public AssocCountColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0)));
        }

        @Override
        public Number getValue(MetaAnalysisProxy object) {
            return object.getTotalAssocCount();
        }
    }
}
