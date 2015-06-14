package com.gmi.nordborglab.browser.server.data.csv;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.SNPGWASInfo;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.Equals;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/26/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSVGWASReader implements GWASReader {

    private static CellProcessor[] headerCellProcessors = new CellProcessor[]{
            new Trim(new Equals("chr")), new Trim(new Equals("pos")), new Trim(new IsIncludedIn(new String[]{"score", "pvalue"})),
            new Optional(new Trim(new Equals("maf"))),
            new Optional(new Trim(new Equals("mac"))),
            new Optional(new Trim(new Equals("GVE")))
    };

    private static class ParseNAs extends CellProcessorAdaptor implements StringCellProcessor {

        private ParseNAs() {
        }

        private ParseNAs(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);
            if (value instanceof String && ((String) value).equalsIgnoreCase("NA")) {
                return null;
            }
            return next.execute(value, context);
        }

    }

    private static CellProcessor[] cellProcessors = new CellProcessor[]{
            new Trim(new ParseInt()), new Trim(new ParseDouble()), new Trim(new ParseDouble()), new Optional(new Trim(new ParseNAs(new ParseDouble()))), new Optional(new Trim(new ParseNAs(new ParseInt()))), new Optional(new Trim(new ParseNAs(new ParseDouble())))
    };

    public CSVGWASReader() {
    }

    @Override
    public ChrGWAData readForChr(String file, String chr, Double limit) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public GWASData readAll(String file, Double limit) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void isValidGWASFile(File file) throws Exception {
        if (!file.exists())
            throw new Exception("File does not exist");

        try (
                FileReader fin = new FileReader(file);
                ICsvListReader reader = new CsvListReader(fin, CsvPreference.STANDARD_PREFERENCE)) {
            reader.read(headerCellProcessors);
            reader.read(cellProcessors);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Map<String, ChrGWAData> parseGWASDataFromFile(File originalFile) throws Exception {
        try (FileReader fin = new FileReader(originalFile);
             ICsvListReader reader = new CsvListReader(fin, CsvPreference.STANDARD_PREFERENCE)) {
            Map<String, ChrGWAData> data = Maps.newHashMap();
            reader.getHeader(true);
            List<Object> row = null;
            ChrGWAData chrGWAData = null;
            int chr = 1;
            List<Integer> positions = Lists.newArrayList();
            List<Float> pvalues = Lists.newArrayList();
            List<Integer> macs = Lists.newArrayList();
            List<Float> mafs = Lists.newArrayList();
            List<Float> GVEs = Lists.newArrayList();
            while ((row = reader.read(cellProcessors)) != null) {
                if (chr != (Integer) row.get(0)) {
                    String key = "chr" + chr;
                    chrGWAData = ChrGWAData.sortAndConvertToScores(new ChrGWAData(Ints.toArray(positions), Floats.toArray(pvalues), Ints.toArray(macs), Floats.toArray(mafs), Floats.toArray(GVEs), key));
                    data.put(key, chrGWAData);
                    positions.clear();
                    pvalues.clear();
                    mafs.clear();
                    macs.clear();
                    GVEs.clear();
                    chr = (Integer) row.get(0);
                } else {
                    Integer position = ((Double) row.get(1)).intValue();
                    positions.add(position);
                    Double value = (Double) row.get(2);
                    Float floatValue = (float) (double) value;
                    pvalues.add(floatValue);
                    if (row.get(3) != null) {
                        mafs.add((float) (double) (Double) row.get(3));
                    }
                    if (row.get(4) != null) {
                        macs.add((Integer) row.get(4));
                    }
                    if (row.get(5) != null) {
                        GVEs.add((float) (double) (Double) row.get(5));
                    }
                }
            }
            String key = "chr" + chr;
            data.put(key, ChrGWAData.sortAndConvertToScores(new ChrGWAData(Ints.toArray(positions), Floats.toArray(pvalues), Ints.toArray(macs), Floats.toArray(mafs), Floats.toArray(GVEs), key)));
            return data;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void saveGWASDataToFile(Map<String, ChrGWAData> data, File destFile) throws Exception {
        throw new Exception("not supported");
    }

    @Override
    public SNPGWASInfo readSingle(String file, Integer chromosome, Integer position) {
        throw new RuntimeException("Not supported");
    }
}
