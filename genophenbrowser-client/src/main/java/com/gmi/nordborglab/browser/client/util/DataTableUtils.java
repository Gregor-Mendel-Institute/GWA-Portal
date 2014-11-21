package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAnnotProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataColumn;
import com.googlecode.gwt.charts.client.RoleType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataTableUtils {

    public static Ordering<SNPAllele> snpOrdering = new Ordering<SNPAllele>() {
        @Override
        public int compare(@Nullable SNPAllele left, @Nullable SNPAllele right) {
            Double value1 = 0d;
            Double value2 = 0d;
            try {
                value1 = Double.valueOf(left.getPhenotype());
            } catch (Exception e) {

            }
            try {
                value2 = Double.valueOf(right.getPhenotype());
            } catch (Exception e) {

            }

            return Doubles.compare(value1, value2);
        }
    };

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
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        dataTable.addRows(traits.size());
        int i = 0;
        String name = "";
        Long id = null;
        String accession = "";
        Double longitude = null;
        Double latitude = null;
        Double phenotype = null;
        String country = "";
        for (TraitProxy trait : traits) {
            try {
                PassportProxy passport = trait.getObsUnit().getStock()
                        .getPassport();
                id = passport.getId();
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
            addRowToPhentoypeExplorerTable(dataTable, i, accession, id, latitude, longitude, phenotype, country);
            i = i + 1;
        }
        return dataTable;
    }

    private static DataTable getDataTableForPhenotypeExplorereTable() {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "ID Name Phenotype");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Date");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Longitude");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Latitude");
        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, "Phenotype");
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "Accession");
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "Country");
        return dataTable;
    }

    private static void addRowToPhentoypeExplorerTable(DataTable dataTable, int index, String accession, Long id, Double latitude, Double longitude, Double value, String country) {
        String name = accession + " ID:" + id + " Phenotype:"
                + value;
        dataTable.setValue(index, 0, name);
        dataTable.setValue(index, 1, 1900);
        if (longitude != null)
            dataTable.setValue(index, 2, longitude);
        if (latitude != null)
            dataTable.setValue(index, 3, latitude);
        if (value != null)
            dataTable.setValue(index, 4, value);
        dataTable.setValue(index, 5, accession);
        dataTable.setValue(index, 6, country);
    }


    public static DataTable createPhentoypeExplorerTableFromStats(ImmutableList<TraitStatsProxy> traits) {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        if (traits != null) {
            dataTable.addRows(traits.size());
            int i = 0;
            for (TraitStatsProxy trait : traits) {
                addRowToPhentoypeExplorerTable(dataTable, i, trait.getAccename(), trait.getPassportId(), trait.getLatitude(), trait.getLongitude(), trait.getAvgValue(), trait.getCountry());
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

    public static DataTable getDataTableForSNPAllelePhenotypeTable() {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, "Allele");
        return dataTable;
    }

    public static DataTable createSNPAllelePhenotypeTable(Collection<SNPAllele> snpAlleles) {
        DataTable dataTable = getDataTableForSNPAllelePhenotypeTable();
        if (snpAlleles != null) {
            dataTable.addRows(snpAlleles.size());
            int i = 0;
            for (SNPAllele snpAllele : snpAlleles) {
                LocalityProxy locality = snpAllele.getPassport().getCollection().getLocality();
                addRowToPhentoypeExplorerTable(dataTable, i,
                        snpAllele.getPassport().getAccename(),
                        snpAllele.getPassport().getId(),
                        locality.getLatitude(),
                        locality.getLongitude(),
                        Double.parseDouble(snpAllele.getPhenotype()),
                        locality.getCountry());
                dataTable.setValue(i, 7, snpAllele.getAllele());
                i = i + 1;
            }
        }
        return dataTable;
    }

    public static ImmutableMultimap<String, SNPAllele> getAlleleToPhenotype(Collection<SNPAllele> snpAlleles) {
        return Multimaps.index(snpAlleles, new Function<SNPAllele, String>() {
            @Nullable
            @Override
            public String apply(@Nullable SNPAllele snpAllele) {
                return snpAllele.getAllele() != null ? snpAllele.getAllele() : "N/A";
            }
        });
    }

    public static com.googlecode.gwt.charts.client.DataTable createSNPAllelePhenotypeForBoxplotTable(ImmutableMultimap<String, SNPAllele> groupedByAllele, SNPAnnotProxy alleleInfo) {
        com.googlecode.gwt.charts.client.DataTable dataTable = com.googlecode.gwt.charts.client.DataTable.create();
        dataTable.addColumn(ColumnType.STRING, null);
        dataTable.addColumn(ColumnType.NUMBER, alleleInfo.getRef());
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, alleleInfo.getAlt());
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        Set<String> alleles = groupedByAllele.keySet();
        Map<String, String[]> stats = new HashMap<>();
        NumberFormat df = NumberFormat.getDecimalFormat();
        df.overrideFractionDigits(4);
        for (String allele : alleles) {
            ImmutableList<SNPAllele> orderedSNPAlleles = snpOrdering.immutableSortedCopy(groupedByAllele.get(allele));
            String[] stat = new String[4];
            int size = orderedSNPAlleles.size();
            stat[0] = df.format(Double.valueOf(orderedSNPAlleles.get(0).getPhenotype()));
            stat[1] = df.format(Double.valueOf(orderedSNPAlleles.get((int) Math.round(size * 25 / 100)).getPhenotype()));
            stat[2] = df.format(Double.valueOf(orderedSNPAlleles.get((int) Math.round(size * 75 / 100)).getPhenotype()));
            stat[3] = df.format(Double.valueOf(orderedSNPAlleles.get(orderedSNPAlleles.size() - 1).getPhenotype()));
            stats.put(allele, stat);
        }
        int i = 0;
        for (String allele : Arrays.asList(alleleInfo.getRef() != null ? alleleInfo.getRef() : "N/A", alleleInfo.getAlt() != null ? alleleInfo.getAlt() : "N/A")) {
            dataTable.addRow();
            int startix = i * 4;
            //dataTable.setValue(i,0,allele);
            String[] value = stats.get(allele);
            if (value != null) {
                dataTable.setValue(i, startix + 1, value[0]);
                dataTable.setValue(i, startix + 2, value[1]);
                dataTable.setValue(i, startix + 3, value[2]);
                dataTable.setValue(i, startix + 4, value[3]);
            }
            i = i + 1;
        }
        return dataTable;
    }

    public static com.googlecode.gwt.charts.client.DataTable createSNPAllelePhenotypeForStripChartTable(Collection<SNPAllele> snpAlleles, SNPAnnotProxy alleleInfo) {
        String allele1 = alleleInfo.getRef();
        String allele2 = alleleInfo.getAlt();
        com.googlecode.gwt.charts.client.DataTable dataTable = com.googlecode.gwt.charts.client.DataTable.create();
        dataTable.addColumn(ColumnType.NUMBER, "Allele");
        dataTable.addColumn(ColumnType.NUMBER, allele1);
        dataTable.addColumn(DataColumn.create(ColumnType.STRING, RoleType.TOOLTIP));
        dataTable.addColumn(ColumnType.NUMBER, allele2);
        dataTable.addColumn(DataColumn.create(ColumnType.STRING, RoleType.TOOLTIP));
        NumberFormat df = NumberFormat.getDecimalFormat();
        df.overrideFractionDigits(4);
        int jitter = 1;
        int i = 0;
        for (SNPAllele allele : snpAlleles) {
            dataTable.addRow();
            Double value = Double.valueOf(allele.getPhenotype());
            double random = Random.nextDouble();
            Double xValue = null;
            if (allele1.equals(allele.getAllele())) {

                dataTable.setValue(i, 1, value);
                xValue = (2.5 + jitter * random);
                dataTable.setValue(i, 2, df.format(value));
            } else {
                dataTable.setValue(i, 3, value);
                xValue = (6.5 + jitter * random);
                dataTable.setValue(i, 4, df.format(value));
            }
            dataTable.setValue(i, 0, xValue);
            i = i + 1;
        }
        return dataTable;
    }
}
