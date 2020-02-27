package com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes;

import com.gmi.nordborglab.browser.shared.proxy.AssociationProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.SNPAnnotationProxy;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.builder.shared.TableBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.builder.shared.TableSectionBuilder;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractCellTableBuilder;
import com.google.gwt.view.client.SelectionModel;

import java.util.Set;

/**
 * Created by uemit.seren on 1/18/16.
 */
public class MetaAnalysisGeneTableBuilder extends AbstractCellTableBuilder<MetaAnalysisProxy> {

    private final Set<Long> showMoreSet;
    private final String evenRowStyle;
    private final String oddRowStyle;
    private final String selectedRowStyle;
    private final String evenCellStyle;
    private final String oddCellStyle;
    private final String selectedCellStyle;
    private final SelectionModel<? super MetaAnalysisProxy> selectionModel;
    private final MetaAnalysisGeneView.ScoreCell scoreCell = new MetaAnalysisGeneView.ScoreCell();
    private int maxAssocCount = 0;

    /**
     * Construct a new table builder.
     *
     * @param cellTable the table this builder will build rows for
     */
    public MetaAnalysisGeneTableBuilder(AbstractCellTable<MetaAnalysisProxy> cellTable, final Set<Long> showMoreSet, final int maxAssocCount) {
        super(cellTable);
        AbstractCellTable.Style style = cellTable.getResources().style();
        evenRowStyle = style.evenRow();
        oddRowStyle = style.oddRow();
        selectedRowStyle = " " + style.selectedRow();
        evenCellStyle = style.cell() + " " + style.evenRowCell();
        oddCellStyle = style.cell() + " " + style.oddRowCell();
        selectedCellStyle = " " + style.selectedRowCell();
        //childCell = " " + style.childCell();
        this.showMoreSet = showMoreSet;
        // Calculate the row styles.
        this.selectionModel = cellTable.getSelectionModel();
        this.maxAssocCount = maxAssocCount;
    }

    @Override
    protected void buildRowImpl(MetaAnalysisProxy rowValue, int absRowIndex) {
        boolean isEven = isEvent(absRowIndex);
        boolean isSelected = isSelected(rowValue);
        String trClasses = getRowClassName(isEven, isSelected);
        String tdClasses = getCellClassName(isEven, isSelected);
        buildMetaAnalysisRow(rowValue, trClasses, tdClasses);

        // Display list of assoc
        if (showMoreSet.contains(rowValue.getAnalysisId())) {
            buildSubTable(rowValue, absRowIndex, trClasses, tdClasses);
        }
    }

    private void buildSubTable(MetaAnalysisProxy rowValue, int absRowIndex, String trClasses, String tdClasses) {
        TableRowBuilder row = startRow();
        row.className(trClasses);
        row.startTD().className(trClasses).style().outlineStyle(Style.OutlineStyle.NONE);
        row.endTD();
        TableCellBuilder td = row.startTD().className(tdClasses).colSpan(cellTable.getColumnCount() - 1);
        td.style().outlineStyle(Style.OutlineStyle.NONE);
        TableBuilder tbl = td.startTable();
        tbl.style().width(100, Style.Unit.PCT);
        TableSectionBuilder thead = tbl.startTHead();
        TableRowBuilder rowHeader = thead.startTR();
        rowHeader.startTH().text("Score").endTH();
        rowHeader.startTH().text("Mac").endTH();
        rowHeader.startTH().text("Maf").endTH();
        rowHeader.startTH().text("SNP").endTH();
        rowHeader.endTR();
        thead.endTHead();
        TableSectionBuilder tbody = tbl.startTBody();
        int subrowIndex = 0;
        for (AssociationProxy assoc : rowValue.getAssociations()) {
            buildAssociations(assoc, absRowIndex, tbody, subrowIndex);
            subrowIndex++;
        }
        tbody.endTBody();
        TableSectionBuilder tfooter = tbl.startTFoot();
        buildSummaryAssocRow(rowValue, tfooter);
        tfooter.endTFoot();
        tbl.endTable();
        td.endTD();
        row.endTR();
    }


    private void buildSummaryAssocRow(MetaAnalysisProxy rowValue, TableSectionBuilder tfooter) {
        String text = "";
        if (maxAssocCount < rowValue.getTotalAssocCount()) {
            text = "Top " + maxAssocCount + " of " + rowValue.getTotalAssocCount() + " associations in this region displayed";
        } else {
            text = "All " + rowValue.getTotalAssocCount() + " associations in this region  displayed";
        }
        TableRowBuilder tr = tfooter.startTR();
        tr.startTD().colSpan(6).text(text).endTD();
        tr.endTR();
    }

    private void buildAssociations(AssociationProxy assoc, int absRowIndex, TableSectionBuilder tbody, int subrowIndex) {
        SNPInfoProxy snpInfo = assoc.getSnpInfo();
        // TODO adapt to new annotation
        String snpText = String.valueOf(snpInfo.getPosition());
        if (snpInfo.getAnnotations() != null && snpInfo.getAnnotations().size() > 0) {
            SNPAnnotationProxy annotation = snpInfo.getAnnotations().get(0);
            snpText += " [" + annotation.getEffect() + "]";

        }
        boolean isEven = subrowIndex % 2 == 0;
        TableRowBuilder tr = tbody.startTR();
        tr.className(isEven ? evenRowStyle : oddRowStyle);
        tr.attribute("__assoc_ix", subrowIndex);

        SafeHtmlBuilder scoreSb = new SafeHtmlBuilder();
        scoreCell.render(createContext(0), assoc, scoreSb);

        tr.startTD().className(isEven ? evenCellStyle : oddCellStyle).html(scoreSb.toSafeHtml()).endTD();
        tr.startTD().className(isEven ? evenCellStyle : oddCellStyle).text(String.valueOf(assoc.getMac())).endTD();
        tr.startTD().className(isEven ? evenCellStyle : oddCellStyle).text(String.valueOf(assoc.getMaf())).endTD();
        tr.startTD().className(isEven ? evenCellStyle : oddCellStyle).text(snpText).endTD();
        tr.endTR();
    }

    private void buildMetaAnalysisRow(MetaAnalysisProxy rowValue, String trClasses, String tdClasses) {
        // Calculate the cell styles.
        TableRowBuilder row = startRow();
        row.className(trClasses.toString());
        for (int i = 0; i < cellTable.getColumnCount(); i++) {
            HasCell<MetaAnalysisProxy, ?> column = cellTable.getColumn(i);
            TableCellBuilder td = row.startTD();
            td.className(tdClasses);
            td.style().outlineStyle(Style.OutlineStyle.NONE).endStyle();
            renderCell(td, createContext(i), column, rowValue);
            td.endTD();
        }
        row.endTR();
    }

    private String getRowClassName(boolean isEven, boolean isSelected) {
        StringBuilder trClasses = new StringBuilder(isEven ? evenRowStyle : oddRowStyle);
        if (isSelected) {
            trClasses.append(selectedRowStyle);
        }
        return trClasses.toString();
    }

    private String getCellClassName(boolean isEven, boolean isSelected) {
        String cellStyles = (isEven ? evenCellStyle : oddCellStyle);
        if (isSelected) {
            cellStyles += selectedCellStyle;
        }
        return cellStyles;
    }

    private boolean isSelected(MetaAnalysisProxy rowValue) {
        return (selectionModel == null || rowValue == null) ? false : selectionModel.isSelected(rowValue);
    }

    private boolean isEvent(int rowIndex) {
        return rowIndex % 2 == 0;
    }

    public void setMaxAssocCount(int maxAssocCount) {
        this.maxAssocCount = maxAssocCount;
    }


}