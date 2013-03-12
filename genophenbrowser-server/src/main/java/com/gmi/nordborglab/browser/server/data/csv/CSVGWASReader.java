package com.gmi.nordborglab.browser.server.data.csv;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.Equals;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;
import sun.util.locale.StringTokenIterator;

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

    private static CellProcessor[] headerCellProcessors = new CellProcessor[] {
       new Equals("chr"),new Equals("position"),new IsIncludedIn(new String[] {"score","pvalue"}),
       new Optional(new Equals("maf")),
       new Optional(new Equals("mac")),
       new Optional(new Equals("GVE"))
    };

    private static class ParseNAs extends CellProcessorAdaptor {

        private ParseNAs() {
        }

        private ParseNAs(CellProcessor next) {
            super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);
            if( value instanceof String && ((String) value).equalsIgnoreCase("NA")) {
                return null;
            }
            return next.execute(value,context);
        }

    }

    private static CellProcessor[] cellProcessors = new CellProcessor[] {
       new ParseInt(),new ParseInt(),new ParseDouble(), new Optional(new ParseNAs(new ParseDouble())), new Optional(new ParseNAs(new ParseInt())), new Optional(new ParseNAs(new ParseDouble()))
    };
    private static String header[] = new String[] {"chr","pos","pval","maf","max"};

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
        ICsvListReader reader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
        try {
            reader.read(headerCellProcessors);
            reader.read(cellProcessors);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }

    @Override
    public Map<String, ChrGWAData> parseGWASDataFromFile(File originalFile) throws Exception {
        ICsvListReader reader = new CsvListReader(new FileReader(originalFile), CsvPreference.STANDARD_PREFERENCE);
        try {
            Map<String,ChrGWAData> data = Maps.newHashMap();
            reader.getHeader(true);
            List<Object> row = null;
            ChrGWAData chrGWAData = null;
            int chr = 1;
            List<Integer> positions = Lists.newArrayList();
            List<Float> pvalues = Lists.newArrayList();
            while ((row = reader.read(cellProcessors)) != null) {
                if (chr != (Integer)row.get(0)) {
                    String key = "Chr"+chr;
                    chrGWAData = ChrGWAData.sortAndConvertToScores(new ChrGWAData(Ints.toArray(positions), Floats.toArray(pvalues), key));
                    data.put(key, chrGWAData);
                    positions.clear();
                    pvalues.clear();
                    chr = (Integer)row.get(0);
                }
                else {
                    positions.add((Integer)row.get(1));
                    Double value = (Double)row.get(2);
                    Float floatValue = (float)(double)value;
                    pvalues.add(floatValue);
                }
            }
            String key = "Chr"+chr;
            data.put(key,ChrGWAData.sortAndConvertToScores(new ChrGWAData(Ints.toArray(positions),Floats.toArray(pvalues),key)));
            return data;
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }

    @Override
    public void saveGWASDataToFile(Map<String, ChrGWAData> data, File destFile) throws Exception {
        throw new Exception("not supported");
    }
}
