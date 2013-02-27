package com.gmi.nordborglab.browser.server.data.hdf5;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.hdf5.*;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.google.common.collect.Maps;
import com.google.common.primitives.Floats;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class HDF5GWASReader implements GWASReader{
	
	private final String dataFolder;
	private static String pValueGroup ="/pvalues/";
	
	public HDF5GWASReader(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	@Override
	public GWASData readForChr(String file,String chr, Double limit) {
		IHDF5Reader reader = getReader(file);
		GWASData data = getForChr(reader, chr, limit);
		reader.close();
		return data;
	}

	@Override
	public Map<String, GWASData> readAll(String file,Double limit) {
		
		IHDF5Reader reader = getReader(file);
		Map<String,GWASData> data = readAll(reader,limit);
		reader.close();
		return data;
	}

    protected Map<String,GWASData> readAll(IHDF5Reader reader,Double limit) {
        List<String> members = reader.getGroupMembers(pValueGroup);
        Map<String,GWASData> data = new LinkedHashMap<String, GWASData>();
        for (String chr:members) {
            data.put(chr,getForChr(reader,chr,limit));
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
            for (String chr:groupMembers) {
                String positionDataSet = pValueGroup + chr + "/positions";
                String scoresDataSet = pValueGroup + chr + "/scores";
                if (!reader.isDataSet(positionDataSet))
                    throw new Exception("no positions dataset found under "+pValueGroup+chr);
                if (!reader.isDataSet(scoresDataSet))
                    throw new Exception("No scores dataset found under" + pValueGroup +chr);
                HDF5DataSetInformation infoPos = reader.getDataSetInformation(positionDataSet);
                HDF5DataSetInformation infoScores = reader.getDataSetInformation(scoresDataSet);
                if (infoPos.getNumberOfElements() == 0 && infoScores.getNumberOfElements() != infoPos.getNumberOfElements())
                    throw new Exception("Number of elements in scores and positions dataset differ or are 0");
                if (infoPos.getTypeInformation().getDataClass() != HDF5DataClass.INTEGER)
                    throw new Exception("Positions must be of type int");
                if (infoScores.getTypeInformation().getDataClass() != HDF5DataClass.FLOAT)
                    throw new Exception("Scores must be of type float");
            }
        }catch (Exception e) {
            throw e;
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }

    @Override
    public Map<String,GWASData> parseGWASDataFromFile(File originalFile) throws Exception {
       IHDF5Reader reader = null;
       Map<String,GWASData> newDataMap = Maps.newHashMap();
       try {
           reader = getReader(originalFile);
           Map<String,GWASData> data = readAll(reader,1.0);
           for (Map.Entry<String,GWASData> entry:data.entrySet()) {
               GWASData newGWASData = GWASData.sortAndConvertToScores(entry.getValue());
               newDataMap.put(entry.getKey(),newGWASData);
           }

       }catch (Exception e) {
           throw e;
       }
       finally {
           if (reader != null)
               reader.close();
       }
       return newDataMap;
    }

    @Override
    public void saveGWASDataToFile(Map<String, GWASData> data, File destFile) throws Exception {
        IHDF5Writer writer = null;
        try {
            writer = HDF5Factory.open(destFile);
            writer.createGroup(pValueGroup);
            for (Map.Entry<String,GWASData> entry:data.entrySet()) {
                String chr = entry.getKey();
                GWASData gwasData = entry.getValue();
                String positionDataSet = pValueGroup + chr + "/positions";
                String scoresDataSet = pValueGroup + chr + "/scores";
                writer.createGroup(pValueGroup+chr);
                writer.writeFloatArray(scoresDataSet,gwasData.getPvalues());
                writer.writeIntArray(positionDataSet,gwasData.getPositions());
            }
            writer.flush();

        }catch (Exception e) {
            throw e;
        }
        finally {
            if (writer != null)
                writer.close();
        }
    }


    protected GWASData getForChr(IHDF5Reader reader,String chr,Double limit) {
		int[] positions = null;
		float[] scores = null;
		String path = pValueGroup+chr;
		HDF5DataSetInformation info = reader.getDataSetInformation(path+"/positions");
		Double fraction = null;
		if (limit != null) 
			  fraction = new Double(info.getNumberOfElements()*limit);
		if (fraction == null) {
			positions = reader.readIntArray(path+"/positions");
			scores = reader.readFloatArray(path+"/scores");
		}
		else
		{
			positions  = reader.readIntArrayBlock(path+"/positions",fraction.intValue(),0);
			scores = reader.readFloatArrayBlock(path+"/scores",fraction.intValue(),0);
		}
		GWASData chrData = new GWASData(positions, scores, chr);
		return chrData;
	}
	
	protected IHDF5Reader getReader(String file) {
		IHDF5Reader reader = HDF5Factory.openForReading(dataFolder+"/"+file);
		return reader;
	}

    protected IHDF5Reader getReader(File file) {
        IHDF5Reader reader = HDF5Factory.openForReading(file);
        return reader;
    }
}
