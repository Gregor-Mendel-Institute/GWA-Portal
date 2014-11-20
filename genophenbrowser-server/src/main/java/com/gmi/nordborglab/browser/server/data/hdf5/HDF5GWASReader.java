package com.gmi.nordborglab.browser.server.data.hdf5;

import ch.systemsx.cisd.hdf5.HDF5DataClass;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.SNPGWASInfo;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HDF5GWASReader implements GWASReader {

    private final String dataFolder;
    private static String pValueGroup = "/pvalues/";

    private static class GWASStats  {
        protected final long numberOfSNPs;
        protected final double bonferroniScore;
        protected final float maxScore;

        private GWASStats(long numberOfSNPs,double bonferroniScore, float maxScore) {
            this.numberOfSNPs = numberOfSNPs;
            this.bonferroniScore = bonferroniScore;
            this.maxScore = maxScore;
        }

        public static GWASStats read(IHDF5Reader reader) {
            long numberOfSNPs = 0;
            if (reader.hasAttribute(pValueGroup, "numberOfSNPs")) {
                numberOfSNPs = reader.getLongAttribute(pValueGroup, "numberOfSNPs");
            }
            else {
                numberOfSNPs = getNumberOfSNPsFromDatasets(reader);
            }
            double bonferroniScore = 6;
            if (reader.hasAttribute(pValueGroup, "bonferroniScore")) {
                bonferroniScore = reader.getDoubleAttribute(pValueGroup, "bonferroniScore");
            } else if (reader.hasAttribute(pValueGroup, "bonferroni_threshold")) {
                bonferroniScore = reader.getDoubleAttribute(pValueGroup, "bonferroni_threshold");
            }
            float maxScore = 10;
            if (reader.hasAttribute(pValueGroup, "maxScore")) {
                maxScore = reader.getFloatAttribute(pValueGroup, "maxScore");
            } else if (reader.hasAttribute(pValueGroup, "max_score")) {
                maxScore = reader.getFloatAttribute(pValueGroup, "max_score");
            }
            return new GWASStats(numberOfSNPs,bonferroniScore,maxScore);
        }

        private static long getNumberOfSNPsFromDatasets(IHDF5Reader reader) {
            ImmutableSet<String> chromosomes = ImmutableSet.<String>builder().add("chr1","chr2","chr3","chr4","chr5").build();
            long numberOfSNPs = 0;
            for (String chr : chromosomes)   {
                HDF5DataSetInformation info = reader.getDataSetInformation( pValueGroup+chr + "/positions");
                numberOfSNPs = numberOfSNPs + info.getNumberOfElements();
            }
            return numberOfSNPs;
        }
    }

    public HDF5GWASReader(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public ChrGWAData readForChr(String file, String chr, Double limit) {
        IHDF5Reader reader = getReader(file);
        ChrGWAData data = getForChr(reader, chr, limit);
        reader.close();
        return data;
    }

    @Override
    public GWASData readAll(String file, Double limit) {
        IHDF5Reader reader = getReader(file);
        GWASData gwasData;
        Map<String, ChrGWAData> data = readAll(reader, limit);

        GWASStats stats = GWASStats.read(reader);

        gwasData = new GWASData(data, stats.numberOfSNPs, stats.bonferroniScore, stats.maxScore);
        reader.close();
        return gwasData;
    }


    protected Map<String, ChrGWAData> readAll(IHDF5Reader reader, Double limit) {
        List<String> members = reader.getGroupMembers(pValueGroup);
        Map<String, ChrGWAData> data = new LinkedHashMap<String, ChrGWAData>();
        for (String chr : members) {
            data.put(chr, getForChr(reader, chr, limit));
        }
        return data;
    }

    @Override
    public void isValidGWASFile(File file) throws Exception {
        IHDF5Reader reader = null;
        try {
            boolean isValidHDF5 = HDF5Factory.isHDF5File(file);
            if (!isValidHDF5)
                throw new Exception("Not a valid HDF5 File");
            reader = getReader(file);
            if (!reader.isGroup(pValueGroup))
                throw new Exception("No group named" + pValueGroup);
            List<String> groupMembers = reader.getGroupMembers(pValueGroup);
            if (groupMembers.size() != 5)
                throw new Exception("pValue group must have exactly 5 sub-groups");
            for (String chr : groupMembers) {
                String positionDataSet = pValueGroup + chr + "/positions";
                String scoresDataSet = pValueGroup + chr + "/scores";
                if (!reader.isDataSet(positionDataSet))
                    throw new Exception("no positions dataset found under " + pValueGroup + chr);
                if (!reader.isDataSet(scoresDataSet))
                    throw new Exception("No scores dataset found under" + pValueGroup + chr);
                HDF5DataSetInformation infoPos = reader.getDataSetInformation(positionDataSet);
                HDF5DataSetInformation infoScores = reader.getDataSetInformation(scoresDataSet);
                if (infoPos.getNumberOfElements() == 0 && infoScores.getNumberOfElements() != infoPos.getNumberOfElements())
                    throw new Exception("Number of elements in scores and positions dataset differ or are 0");
                if (infoPos.getTypeInformation().getDataClass() != HDF5DataClass.INTEGER)
                    throw new Exception("Positions must be of type int");
                if (infoScores.getTypeInformation().getDataClass() != HDF5DataClass.FLOAT)
                    throw new Exception("Scores must be of type float");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    @Override
    public Map<String, ChrGWAData> parseGWASDataFromFile(File originalFile) throws Exception {
        IHDF5Reader reader = null;
        Map<String, ChrGWAData> newDataMap = Maps.newHashMap();
        try {
            reader = getReader(originalFile);
            Map<String, ChrGWAData> data = readAll(reader, null);
            for (Map.Entry<String, ChrGWAData> entry : data.entrySet()) {
                ChrGWAData newChrGWAData = ChrGWAData.sortAndConvertToScores(entry.getValue());
                newDataMap.put(entry.getKey(), newChrGWAData);
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (reader != null)
                reader.close();
        }
        return newDataMap;
    }

    @Override
    public void saveGWASDataToFile(Map<String, ChrGWAData> data, File destFile) throws Exception {
        IHDF5Writer writer = null;
        try {
            writer = HDF5Factory.open(destFile);
            writer.createGroup(pValueGroup);
            long numberOfSnps = 0;
            float maxScore = 0;
            for (Map.Entry<String, ChrGWAData> entry : data.entrySet()) {
                String chr = entry.getKey();
                ChrGWAData chrGWAData = entry.getValue();
                String positionDataSet = pValueGroup + chr + "/positions";
                String scoresDataSet = pValueGroup + chr + "/scores";
                String macsDataSet = pValueGroup + chr + "/macs";
                String mafsDataSet = pValueGroup + chr + "/mafs";
                String gveDataSet = pValueGroup + chr + "/GVEs";
                writer.createGroup(pValueGroup + chr.toLowerCase());
                writer.writeFloatArray(scoresDataSet, chrGWAData.getPvalues());
                writer.writeIntArray(positionDataSet, chrGWAData.getPositions());
                if (chrGWAData.getMacs() != null) {
                    writer.writeIntArray(macsDataSet, chrGWAData.getMacs());
                }
                if (chrGWAData.getMafs() != null) {
                    writer.writeFloatArray(mafsDataSet, chrGWAData.getMafs());
                }
                if (chrGWAData.getGVEs() != null) {
                    writer.writeFloatArray(gveDataSet, chrGWAData.getGVEs());
                }
                numberOfSnps += chrGWAData.getPositions().length;
                if (chrGWAData.getPvalues()[0] > maxScore)
                    maxScore = chrGWAData.getPvalues()[0];
            }
            writer.setLongAttribute(pValueGroup, "numberOfSNPs", numberOfSnps);
            //TODO calculate benjamini hochberg
            writer.setDoubleAttribute(pValueGroup, "bonferroniScore", -Math.log10(0.05 / numberOfSnps));
            writer.setFloatAttribute(pValueGroup, "maxScore", maxScore);
            writer.flush();

        } catch (Exception e) {
            throw e;
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    @Override
    public SNPGWASInfo readSingle(String file, Integer chromosome, Integer position) {
        SNPGWASInfo gwasInfo = null;
        //find position index
        IHDF5Reader reader = getReader(file);
        GWASStats stats = GWASStats.read(reader);

        String path = pValueGroup + "chr"+chromosome;
        final int[] positions = reader.readIntArray(path + "/positions");
        Integer[] idx = new Integer[positions.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                return Float.compare(positions[i1], positions[i2]);
            }
        });
        Arrays.sort(positions);
        int ix = idx[Arrays.binarySearch(positions, position)];
        float maf = 0;
        int mac = 0;
        float score =  reader.readFloatArrayBlockWithOffset(path+"/scores",1,ix)[0];
        float GVE = 0;
        if (reader.isDataSet(path + "/macs")) {
            mac = reader.readIntArrayBlockWithOffset(path + "/macs", 1, ix)[0];
        }
        if (reader.isDataSet(path + "/mafs")) {
            maf = reader.readFloatArrayBlockWithOffset(path + "/mafs", 1, ix)[0];
        }
        if (reader.isDataSet(path + "/GVEs")) {
            GVE = reader.readFloatArrayBlockWithOffset(path + "/GVEs", 1, ix)[0];
        }

        gwasInfo = new SNPGWASInfo("Chr"+chromosome,position,score,(int)stats.numberOfSNPs,mac,maf,GVE,stats.bonferroniScore,stats.maxScore);

        reader.close();

        return gwasInfo;

    }




    protected ChrGWAData getForChr(IHDF5Reader reader, String chr, Double limit) {
        int[] positions = null;
        float[] scores = null;
        int[] macs = null;
        float[] mafs = null;
        float[] GVEs = null;
        String path = pValueGroup + chr;
        HDF5DataSetInformation info = reader.getDataSetInformation(path + "/positions");
        Double fraction = null;
        if (limit != null)
            fraction = limit > info.getNumberOfElements() ? info.getNumberOfElements() : limit;
        if (fraction == null) {
            positions = reader.readIntArray(path + "/positions");
            scores = reader.readFloatArray(path + "/scores");
            if (reader.isDataSet(path + "/macs")) {
                macs = reader.readIntArray(path + "/macs");
            }
            if (reader.isDataSet(path + "/mafs")) {
                mafs = reader.readFloatArray(path + "/mafs");
            }
            if (reader.isDataSet(path + "/GVEs")) {
                GVEs = reader.readFloatArray(path + "/GVEs");
            }
        } else {
            positions = reader.readIntArrayBlock(path + "/positions", fraction.intValue(), 0);
            scores = reader.readFloatArrayBlock(path + "/scores", fraction.intValue(), 0);
            if (reader.isDataSet(path + "/macs")) {
                macs = reader.readIntArrayBlock(path + "/macs", fraction.intValue(), 0);
            }
            if (reader.isDataSet(path + "/mafs")) {
                mafs = reader.readFloatArrayBlock(path + "/mafs", fraction.intValue(), 0);
            }
            if (reader.isDataSet(path + "/GVEs")) {
                GVEs = reader.readFloatArrayBlock(path + "/GVEs", fraction.intValue(), 0);
            }
        }
        ChrGWAData chrData = new ChrGWAData(positions, scores, macs, mafs, GVEs, chr);
        return chrData;
    }

    protected IHDF5Reader getReader(String file) {
        IHDF5Reader reader = HDF5Factory.openForReading(dataFolder + "/" + file);
        return reader;
    }

    protected IHDF5Reader getReader(File file) {
        IHDF5Reader reader = HDF5Factory.openForReading(file);
        return reader;
    }
}
