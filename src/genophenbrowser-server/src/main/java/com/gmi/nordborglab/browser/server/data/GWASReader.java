package com.gmi.nordborglab.browser.server.data;

import java.io.File;
import java.util.Map;

public interface GWASReader {
    public ChrGWAData readForChr(String file, String chr, Double limit);

    public GWASData readAll(String file, Double limit);

    public void isValidGWASFile(File file) throws Exception;

    public Map<String, ChrGWAData> parseGWASDataFromFile(File originalFile) throws Exception;

    public void saveGWASDataToFile(Map<String, ChrGWAData> data, File destFile) throws Exception;

    SNPGWASInfo readSingle(String file, Integer chromosome, Integer position);

}
