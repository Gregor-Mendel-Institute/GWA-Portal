package com.gmi.nordborglab.browser.server.data.csv;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.Equals;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import sun.util.locale.StringTokenIterator;

import java.io.File;
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
       new Equals("chr"),new Equals("position"),new Equals("pval"),new Optional(new Equals("maf")),new Optional(new Equals("mac"))
    };

    private static CellProcessor[] cellProcessors = new CellProcessor[] {
       new ParseInt(),new ParseInt(),new ParseDouble(), new Optional(new ParseDouble()), new Optional(new ParseInt())
    };
    private static String header[] = new String[] {"chr","pos","pval","maf","max"};

    public CSVGWASReader() {
    }

    @Override
    public GWASData readForChr(String file, String chr, Double limit) {
       throw new RuntimeException("Not supported");
    }

    @Override
    public Map<String, GWASData> readAll(String file, Double limit) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void isValidGWASFile(File file) throws Exception {
        if (!file.exists())
            throw new Exception("File does not exist");
        ICsvListReader reader = null;
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
    public Map<String, GWASData> parseGWASDataFromFile(File originalFile) throws Exception {
        ICsvListReader reader = null;
        try {
            Map<String,GWASData> data = Maps.newHashMap();
            reader.getHeader(true);
            List<Object> row = null;
            GWASData gwasData = null;
            int chr = 1;
            List<Integer> positions = Lists.newArrayList();
            List<Float> pvalues = Lists.newArrayList();
            while ((row = reader.read(cellProcessors)) != null) {
                if (chr != (Integer)row.get(0)) {
                    String key = "Chr"+chr;
                    gwasData = GWASData.sortAndConvertToScores(new GWASData(Ints.toArray(positions), Floats.toArray(pvalues),key));
                    data.put(key,gwasData);
                    positions.clear();
                    pvalues.clear();
                    chr = (Integer)row.get(chr);
                }
                else {
                    positions.add((Integer)row.get(1));
                    pvalues.add((Float)row.get(2));
                }
            }
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
    public void saveGWASDataToFile(Map<String, GWASData> data, File destFile) throws Exception {
        throw new Exception("not supported");
    }
}
