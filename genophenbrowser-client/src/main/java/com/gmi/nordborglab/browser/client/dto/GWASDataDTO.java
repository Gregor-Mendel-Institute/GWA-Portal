package com.gmi.nordborglab.browser.client.dto;

import java.util.ArrayList;
import java.util.List;

import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.google.gwt.visualization.client.DataTable;

public class GWASDataDTO {

	protected final double maxScore;
	protected final double bonferroniThreshold;
	protected final List<DataTable> gwasDataTables = new ArrayList<DataTable>();
	protected final List<Integer> chrLengths = new ArrayList<Integer>();

	
	public GWASDataDTO(GWASData data) {
		this.maxScore = data.getMaxScore();
		this.bonferroniThreshold = data.getBonferroniThreshold();
		for (int i =0;i<data.getChrLengths().length();i++) {
			chrLengths.add(data.getChrLengths().get(i));
		}
		
		for (int i =0;i<data.getGWASDataTablesJSON().length();i++) {
			gwasDataTables.add(DataTableUtils.createDataTableFromString(data.getGWASDataTablesJSON().get(i)));
		}
	}


	public double getMaxScore() {
		return maxScore;
	}


	public double getBonferroniThreshold() {
		return bonferroniThreshold;
	}


	public List<DataTable> getGwasDataTables() {
		return gwasDataTables;
	}


	public List<Integer> getChrLengths() {
		return chrLengths;
	}
}
