package com.gmi.nordborglab.browser.server.domain;


public class TaxonomyStats {
	
	public String getStockGenerationData() {
		return stockGenerationData;
	}

	public void setStockGenerationData(String stockGenerationData) {
		this.stockGenerationData = stockGenerationData;
	}
	private String geoChartData;
	private String sampStatData;
	private String alleleAssayData;
	private String stockGenerationData;
	
	public TaxonomyStats() {}

	public String getGeoChartData() {
		return geoChartData;
	}
	public void setGeoChartData(String geoChartData) {
		this.geoChartData = geoChartData;
	}
	public String getSampStatData() {
		return sampStatData;
	}
	public void setSampStatData(String sampStatData) {
		this.sampStatData = sampStatData;
	}
	public String getAlleleAssayData() {
		return alleleAssayData;
	}
	public void setAlleleAssayData(String alleleAssayData) {
		this.alleleAssayData = alleleAssayData;
	}
	
}
