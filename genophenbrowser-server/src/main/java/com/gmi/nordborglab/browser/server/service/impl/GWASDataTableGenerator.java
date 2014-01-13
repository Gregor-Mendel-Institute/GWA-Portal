package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASDataForClient;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.google.common.collect.ImmutableList;
import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataTableGenerator;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.render.JsonRenderer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GWASDataTableGenerator implements DataTableGenerator {

    public static enum TYPE {STUDY, GWASVIEWER}

    @Resource
    GWASDataService gwasDataService;

    @Override
    public DataTable generateDataTable(Query query, HttpServletRequest request)
            throws DataSourceException {
        String chr = request.getParameter("chr");
        GWASData gwasData = getGWASData(request);
        ChrGWAData data = gwasData.getChrGWASData().get(chr);
        DataTable table = convertGWASDataToDataTable(data);
        return table;
    }

    @Override
    public Capabilities getCapabilities() {
        return Capabilities.NONE;
    }

    public GWASDataForClient getGWASDataForClient(HttpServletRequest request) throws DataSourceException {
        GWASDataForClient gwasDataForClient = null;
        GWASData gwasData = getGWASData(request);
        List<String> dataTables = new ArrayList<String>();
        List<String> chromosomes = new ArrayList<String>();
        ///TODO not hardcoded
        List<Integer> chrLenghts = ImmutableList.of(30429953, 19701870, 23467451, 18578708, 26992130);
        for (Map.Entry<String, ChrGWAData> entry : gwasData.getChrGWASData().entrySet()) {
            String chr = entry.getKey();
            chromosomes.add(chr);
            ChrGWAData data = entry.getValue();
            DataTable dataTable = convertGWASDataToDataTable(data);
            CharSequence json = JsonRenderer.renderDataTable(dataTable, true, false, true);
            dataTables.add(json.toString());
        }
        gwasDataForClient = new GWASDataForClient(gwasData.getMaxScore(), gwasData.getBonferroniScore(), chromosomes, chrLenghts, dataTables);
        return gwasDataForClient;
    }

    private GWASData getGWASData(HttpServletRequest request) {
        GWASData gwasData = null;
        Long id = Long.parseLong(request.getParameter("id"));
        TYPE type = TYPE.valueOf(request.getParameter("type").toUpperCase());
        switch (type) {
            case STUDY:
                gwasData = gwasDataService.getGWASDataByStudyId(id);
                break;
            case GWASVIEWER:
                gwasData = gwasDataService.getGWASDataByViewerId(id);
                break;
            default:
                throw new RuntimeException("unknown type " + type);
        }
        return gwasData;
    }

    public static DataTable convertGWASDataToDataTable(ChrGWAData data) throws DataSourceException {
        DataTable table = new DataTable();
        table.addColumn(new ColumnDescription("pos", ValueType.NUMBER, "Position"));
        table.addColumn(new ColumnDescription("pValue", ValueType.NUMBER, "pValue"));
        if (data.getMacs() != null) {
            table.addColumn(new ColumnDescription("mac", ValueType.NUMBER, "MAC"));
        }
        if (data.getMafs() != null) {
            table.addColumn(new ColumnDescription("maf", ValueType.NUMBER, "MAF"));
        }
        if (data.getGVEs() != null) {
            table.addColumn(new ColumnDescription("gve", ValueType.NUMBER, "GVE"));
        }
        if (data.getSnpAnnotations() != null) {
            table.addColumn(new ColumnDescription("annotation", ValueType.TEXT, "Annotation"));
            table.addColumn(new ColumnDescription("inGene", ValueType.BOOLEAN, "inGene"));
        }

        for (int i = 0; i < data.getPositions().length; i++) {
            TableRow row = new TableRow();
            row.addCell(new TableCell(data.getPositions()[i]));
            row.addCell(new TableCell(round(data.getPvalues()[i])));
            if (data.getMacs() != null) {
                row.addCell(new TableCell(data.getMacs()[i]));
            }
            if (data.getMafs() != null) {
                row.addCell(new TableCell(round(data.getMafs()[i])));
            }
            if (data.getGVEs() != null) {
                row.addCell(new TableCell(round(data.getGVEs()[i])));
            }
            if (data.getSnpAnnotations() != null) {
                SNPAnnot annot = data.getSnpAnnotations().get(i);
                Value annotationValue = TextValue.getNullValue();

                if (annot.getAnnotation() != null) {
                    annotationValue = new TextValue(data.getSnpAnnotations().get(i).getAnnotation());
                }
                row.addCell(new TableCell(annotationValue));
                Value inGeneValue = BooleanValue.getInstance(annot.isInGene());
                row.addCell(new TableCell(inGeneValue));
            }
            table.addRow(row);
        }
        return table;
    }

    private static double round(float value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

}
