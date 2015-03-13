package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.client.ui.cells.BarCell;
import com.gmi.nordborglab.browser.client.ui.cells.BooleanIconCell;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;

import java.util.List;

/**
 * Created by uemit.seren on 3/3/15.
 */
public interface SNPViewerDataGridColumns {

    public static class ChrColumn extends Column<SNPInfoProxy, String> {

        public ChrColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            return object.getChr();
        }
    }

    public static class PositionColumn extends Column<SNPInfoProxy, Number> {

        public PositionColumn() {
            super(new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0)));
        }

        @Override
        public Number getValue(SNPInfoProxy object) {
            return object.getPosition();
        }
    }

    public static class InGeneColumn extends Column<SNPInfoProxy, Boolean> {

        public InGeneColumn() {
            super(new BooleanIconCell());
        }

        @Override
        public Boolean getValue(SNPInfoProxy object) {
            return object.isInGene();
        }
    }


    public static class GeneColumn extends Column<SNPInfoProxy, String> {

        public GeneColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            return object.getGene();
        }
    }

    public class EffectColumn extends Column<SNPInfoProxy, String> {
        public EffectColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            String effect = "N/A";
            if (object.getAnnotations() != null && object.getAnnotations().size() > 0) {
                effect = object.getAnnotations().get(0).getEffect();
            }
            return effect;
        }
    }

    public class FunctionColumn extends Column<SNPInfoProxy, String> {
        public FunctionColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            String effect = "N/A";
            if (object.getAnnotations() != null && object.getAnnotations().size() > 0) {
                effect = object.getAnnotations().get(0).getFunction();
            }
            return effect;
        }
    }

    public class CodonColumn extends Column<SNPInfoProxy, String> {
        public CodonColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            String effect = "N/A";
            if (object.getAnnotations() != null && object.getAnnotations().size() > 0) {
                effect = object.getAnnotations().get(0).getCodonChange();
            }
            return effect;
        }
    }

    public class AminoAcidColumn extends Column<SNPInfoProxy, String> {
        public AminoAcidColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            String effect = "N/A";
            if (object.getAnnotations() != null && object.getAnnotations().size() > 0) {
                effect = object.getAnnotations().get(0).getAminoAcidChange();
            }
            return effect;
        }
    }

    public class LyrColumn extends Column<SNPInfoProxy, String> {
        public LyrColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            return object.getLyr();
        }
    }


    public class AlleleColumn extends IdentityColumn<SNPInfoProxy> {


        public AlleleColumn(List<HasCell<SNPInfoProxy, ?>> hasCells) {
            super(new AlleleCompositeCell(hasCells));
        }
    }

    public class AlleleCompositeCell extends CompositeCell<SNPInfoProxy> {

        /**
         * Construct a new {@link com.google.gwt.cell.client.CompositeCell}.
         *
         * @param hasCells the cells that makeup the composite
         */
        public AlleleCompositeCell(List<HasCell<SNPInfoProxy, ?>> hasCells) {
            super(hasCells);
        }

        @Override
        protected <X> void render(Context context, SNPInfoProxy value, SafeHtmlBuilder sb, HasCell<SNPInfoProxy, X> hasCell) {
            final Cell<X> cell = hasCell.getCell();

            if (!(hasCell instanceof AlleleTypeCell) && (value.getRefCount() == null || value.getAltCount() == null)) {
                return;
            }
            if (hasCell instanceof AlleleCountCell) {
                sb.appendHtmlConstant("&nbsp;(");
            } else if (hasCell instanceof AlleleCountBarCell) {
                sb.appendHtmlConstant("<div>");
            }
            super.render(context, value, sb, hasCell);

            if (hasCell instanceof AlleleCountCell) {
                sb.appendHtmlConstant(")");
            } else if (hasCell instanceof AlleleCountBarCell) {
                sb.appendHtmlConstant("</div>");
            }

        }


    }

    public static abstract class AlleleCell<T> implements HasCell<SNPInfoProxy, T> {
        protected final boolean isAlt;

        public static final String REF_COLOR = "#3366cc";
        public static final String ALT_COLOR = "#dc3912";

        public AlleleCell(final boolean isAlt) {
            this.isAlt = isAlt;
        }

        protected String getColor() {
            if (isAlt)
                return REF_COLOR;
            return ALT_COLOR;
        }

        @Override
        public FieldUpdater<SNPInfoProxy, T> getFieldUpdater() {
            return null;
        }
    }


    public static class AlleleCountCell extends AlleleCell<Number> {

        public AlleleCountCell(boolean isAlt) {
            super(isAlt);
        }

        @Override
        public Cell<Number> getCell() {
            return new NumberCell(NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0));
        }

        @Override
        public Number getValue(SNPInfoProxy object) {
            if (object != null) {
                if (isAlt)
                    return object.getAltCount();
                return object.getRefCount();
            }
            return null;
        }
    }

    public static class AlleleTypeCell extends AlleleCell<String> {

        public AlleleTypeCell(boolean isAlt) {
            super(isAlt);
        }

        @Override
        public Cell<String> getCell() {
            return new TextCell();
        }

        @Override
        public String getValue(SNPInfoProxy object) {
            String type = "N/A";
            if (object != null) {
                type = object.getRef();
                if (isAlt)
                    type = object.getAlt();
            }
            return type;
        }
    }


    public static class AlleleCountBarCell extends AlleleCell<Number> {


        public AlleleCountBarCell(boolean isAlt) {
            super(isAlt);
        }

        @Override
        public Cell<Number> getCell() {
            return new BarCell(0.1, getColor());
        }

        @Override
        public Number getValue(SNPInfoProxy object) {
            if (object != null) {
                int total = (object.getAltCount() + object.getRefCount());
                if (total <= 0)
                    return null;
                double value = object.getRefCount();
                if (isAlt)
                    value = object.getAltCount();
                return value / total * 100.0;
            } else {
                return null;
            }
        }
    }
}
