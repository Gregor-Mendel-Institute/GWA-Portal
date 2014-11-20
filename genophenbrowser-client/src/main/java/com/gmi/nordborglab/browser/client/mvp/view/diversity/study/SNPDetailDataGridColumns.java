package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.cells.BarCell;
import com.gmi.nordborglab.browser.client.ui.cells.FlagCell;
import com.google.common.collect.Range;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import java.util.List;

/**
 * Created by uemit.seren on 10/29/14.
 */
public interface SNPDetailDataGridColumns {

    public static class IDColumn extends TextColumn<SNPAllele> {

        @Override
        public String getValue(SNPAllele object) {
            return object.getPassport().getId().toString();
        }
    }

    public static class NameColumn extends TextColumn<SNPAllele> {

        @Override
        public String getValue(SNPAllele object) {
            return object.getPassport().getAccename();
        }
    }

    public static class PhenotypeColumn extends TextColumn<SNPAllele> {

        @Override
        public String getValue(SNPAllele object) {
            return object.getPhenotype();
        }
    }

    public static class LongitudeColumn extends Column<SNPAllele, Number> {

        public LongitudeColumn() {
            super(new NumberCell());
        }


        @Override
        public Number getValue(SNPAllele object) {
            return object.getPassport().getCollection().getLocality().getLongitude();
        }
    }

    public static class LatitudeColumn extends Column<SNPAllele, Number> {

        public LatitudeColumn() {
            super(new NumberCell());
        }


        @Override
        public Number getValue(SNPAllele object) {
            return object.getPassport().getCollection().getLocality().getLatitude();
        }
    }

    public static class CountryColumn extends Column<SNPAllele, String> {

        public CountryColumn(FlagMap map) {
            super(new FlagCell(map));
        }

        @Override
        public String getValue(SNPAllele object) {
            String icon = null;
            try {
                icon = object.getPassport().getCollection().getLocality().getOrigcty();
            } catch (Exception e) {

            }
            return icon;
        }
    }

    public static class AlleleColumn extends TextColumn<SNPAllele> {

        @Override
        public String getValue(SNPAllele object) {
            return object.getAllele() != null ? object.getAllele() : "N/A";
        }
    }


    public static class RowIDColumn extends Column<SNPAllele, Number> {


        public RowIDColumn() {
            super(new NumberCell());
        }

        @Override
        public Number getValue(SNPAllele object) {
            return object.getRowid();
        }
    }


    public static class PhenotypeBarHasCell implements HasCell<SNPAllele, Number> {

        private Range<Double> valueRange;

        public PhenotypeBarHasCell() {
        }

        @Override
        public Cell<Number> getCell() {
            return new BarCell();
        }

        @Override
        public FieldUpdater<SNPAllele, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(SNPAllele object) {
            double percentage = 0;
            double value = 0;
            try {
                value = Double.parseDouble(object.getPhenotype());
            } catch (Exception e) {

            }
            if (valueRange.upperEndpoint() > 0 && value > 0) {
                percentage = ((value - valueRange.lowerEndpoint()) / (valueRange.upperEndpoint() - valueRange.lowerEndpoint())) * 100;
            }
            return percentage;
        }

        public void setRange(Range<Double> range) {
            this.valueRange = range;
        }

        public Range<Double> getRange() {
            return valueRange;
        }
    }

    public static class PhenotypeHasCell implements HasCell<SNPAllele, Number> {

        @Override
        public Cell<Number> getCell() {
            return new NumberCell(NumberFormat.getDecimalFormat());
        }

        @Override
        public FieldUpdater<SNPAllele, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(SNPAllele object) {
            double value = 0;
            try {
                value = Double.parseDouble(object.getPhenotype());
            } catch (Exception e) {

            }
            return value;
        }
    }


    public static class PhenotypeCell extends CompositeCell<SNPAllele> {

        public PhenotypeCell(List<HasCell<SNPAllele, ?>> hasCells) {
            super(hasCells);
        }
    }
}
