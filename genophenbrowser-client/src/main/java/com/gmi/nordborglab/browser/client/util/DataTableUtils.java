package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.googlecode.gwt.charts.client.ColumnType;

public class DataTableUtils {

    public static CustomDataTable createDataTableFromString(String json) {
        CustomDataTable dataTable = null;
        try {
            JavaScriptObject jsData = JsonUtils.unsafeEval(json);
            dataTable = (CustomDataTable) DataTable.create(jsData);
        } catch (Exception e) {
        }
        return dataTable;
    }

    public static com.googlecode.gwt.charts.client.DataTable createChartDataTableFromString(String json) {
        com.googlecode.gwt.charts.client.DataTable dataTable = null;
        try {
            dataTable = (com.googlecode.gwt.charts.client.DataTable) com.googlecode.gwt.charts.client.DataTable.create(json);
        } catch (Exception e) {
        }
        return dataTable;
    }

    public static DataTable createPhentoypeExplorerTable(ImmutableList<TraitProxy> traits) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "ID Name Phenotype");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Date");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Longitude");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Latitude");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Phenotype");
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "Accession");
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "Country");
        if (traits != null) {
            dataTable.addRows(traits.size());
            int i = 0;
            String name = "";
            String accession = "";
            Double longitude = null;
            Double latitude = null;
            Double phenotype = null;
            String country = "";
            for (TraitProxy trait : traits) {
                try {
                    PassportProxy passport = trait.getObsUnit().getStock()
                            .getPassport();
                    accession = passport.getAccename();
                    name = accession + " ID:" + passport.getId() + " Phenotype:"
                            + trait.getValue();
                    if (trait.getValue() != null && !trait.getValue().equals(""))
                        phenotype = Double.parseDouble(trait.getValue());
                    LocalityProxy locality = trait.getObsUnit().getStock()
                            .getPassport().getCollection().getLocality();
                    longitude = locality.getLongitude();
                    latitude = locality.getLatitude();
                    country = locality.getCountry();

                } catch (Exception e) {

                }

                dataTable.setValue(i, 0, name);
                dataTable.setValue(i, 1, 1900);
                if (longitude != null)
                    dataTable.setValue(i, 2, longitude);
                if (latitude != null)
                    dataTable.setValue(i, 3, latitude);
                if (phenotype != null)
                    dataTable.setValue(i, 4, phenotype);
                dataTable.setValue(i, 5, accession);
                dataTable.setValue(i, 6, country);
                i = i + 1;
            }
        }
        return dataTable;
    }

    public static DataTable createPhenotypeHistogramTable(ImmutableSortedMap<Double, Integer> histogram) {
        DataTable histogramData = DataTable.create();
        NumberFormat numberFormat = NumberFormat.getDecimalFormat().overrideFractionDigits(2);
        histogramData.addColumn(AbstractDataTable.ColumnType.STRING, "Bin");
        histogramData.addColumn(AbstractDataTable.ColumnType.NUMBER, "Frequency");
        if (histogram != null) {
            histogramData.addRows(histogram.size() - 1);
            ImmutableList<Double> keys = histogram.keySet().asList();
            ImmutableList<Integer> values = histogram.values().asList();
            for (int i = 0; i < histogram.size() - 1; i++) {
                histogramData.setValue(i, 0, numberFormat.format(keys.get(i)) + " - " + numberFormat.format(keys.get(i + 1)));
                histogramData.setValue(i, 1, values.get(i));
            }
        } else {
            histogramData.addRows(3);
            histogramData.setValue(0, 0, "A");
            histogramData.setValue(0, 1, 5);
            histogramData.setValue(1, 0, "B");
            histogramData.setValue(1, 1, 10);
            histogramData.setValue(2, 0, "C");
            histogramData.setValue(2, 1, 7);
        }
        return histogramData;
    }

    public static DataTable createPhenotypeGeoChartTable(Multiset<String> data) {
        DataTable geoChartData = DataTable.create();
        geoChartData.addColumn(AbstractDataTable.ColumnType.STRING, "Country");
        geoChartData.addColumn(AbstractDataTable.ColumnType.NUMBER, "Frequency");
        if (data != null) {
            for (String cty : data.elementSet()) {
                int i = geoChartData.addRow();
                geoChartData.setValue(i, 0, cty);
                geoChartData.setValue(i, 1, data.count(cty));
            }
        }
        return geoChartData;
    }

    public static com.googlecode.gwt.charts.client.DataTable createFroMFacets(FacetProxy facet) {
        com.googlecode.gwt.charts.client.DataTable dataTable = com.googlecode.gwt.charts.client.DataTable.create();
        dataTable.addColumn(ColumnType.STRING, facet.getName());
        dataTable.addColumn(ColumnType.NUMBER, "count");
        int rowCount = dataTable.addRows(facet.getTerms().size());
        for (int i = 0; i <= rowCount; i++) {
            FacetTermProxy term = facet.getTerms().get(i);
            dataTable.setValue(i, 0, term.getTerm());
            dataTable.setValue(i, 1, term.getValue());
        }
        return dataTable;
    }

    public static Options getDefaultPhenotypeHistogramOptions() {
        Options options = Options.create();
        options.setTitle("Phenotype Histogram");
        Options animationOptions = Options.create();
        animationOptions.set("duration", 1000.0);
        animationOptions.set("easing", "out");
        options.set("animation", animationOptions);
        return options;
    }
}
