package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASDataForClient;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GWASDataTableGenerator implements DataTableGenerator {

    public enum TYPE {STUDY, GWASVIEWER}

    @Resource
    GWASDataService gwasDataService;

    @org.springframework.beans.factory.annotation.Value("${GENOME.chrlengths}")
    private String[] chrLengthsString;
    private List<Integer> chrLengths;

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

        for (Map.Entry<String, ChrGWAData> entry : gwasData.getChrGWASData().entrySet()) {
            String chr = entry.getKey();
            chromosomes.add(chr);
            ChrGWAData data = entry.getValue();
            DataTable dataTable = convertGWASDataToDataTable(data);
            CharSequence json = JsonRenderer.renderDataTable(dataTable, true, false, true);
            dataTables.add(json.toString());
        }
        gwasDataForClient = new GWASDataForClient(gwasData.getMaxScore(), gwasData.getBonferroniScore(), chromosomes, chrLengths, dataTables, gwasData.hasLdData());
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
        if (data.getSNPInfos() != null) {
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
            if (data.getSNPInfos() != null) {
                SNPInfo snpInfo = data.getSNPInfos().get(i);
                Value annotationValue = TextValue.getNullValue();

                if (snpInfo.getAnnotations() != null && snpInfo.getAnnotations().size() > 0) {
                    SNPAnnotation annotation = snpInfo.getAnnotations().get(0);
                    //TODO add more information
                    annotationValue = new TextValue(getAnnotationFromEffect(annotation.getEffect()));
                }
                row.addCell(new TableCell(annotationValue));
                Value inGeneValue = BooleanValue.getInstance(snpInfo.isInGene());
                row.addCell(new TableCell(inGeneValue));
            }
            table.addRow(row);
        }
        return table;
    }

    public static String getAnnotationFromEffect(String effect) {
        if (effect == null)
            return null;
        switch (effect) {
            case "SYNONYMOUS_CODING":
                return "S";
            case "NON_SYNONYMOUS_CODING":
                return "NS";
        }
        return "*";
    }

    private static double round(float value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    @PostConstruct
    public void init() {
        ImmutableList.Builder builder = new ImmutableList.Builder<>();
        for (String chrLength : chrLengthsString) {
            builder.add(Integer.parseInt(chrLength));
        }
        chrLengths = builder.build();
    }

}
