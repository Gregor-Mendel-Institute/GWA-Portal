package com.gmi.nordborglab.browser.server.data.hdf5;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;

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
		List<String> members = reader.getGroupMembers("/pvalues/");
		Map<String,GWASData> data = new LinkedHashMap<String, GWASData>();
		for (String chr:members) {
			data.put(chr,getForChr(reader,chr,limit));
		}
		reader.close();
		return data;
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
}
