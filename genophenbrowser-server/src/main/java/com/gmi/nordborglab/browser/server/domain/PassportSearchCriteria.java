package com.gmi.nordborglab.browser.server.domain;

import java.util.List;

public class PassportSearchCriteria {
	
	public PassportSearchCriteria() {}
	private Long passportId;
	private Long sampStatId;
	private String accName;
	private List<String> countries;
	private List<Long> alleleAssayIds;
	private String collector;
	private String accNumber;
	private String source;

	public Long getPassportId() {
		return passportId;
	}
	public void setPassportId(Long passportId) {
		this.passportId = passportId;
	}
	public Long getSampStatId() {
		return sampStatId;
	}
	public void setSampStatId(Long sampStatId) {
		this.sampStatId = sampStatId;
	}
	public String getAccName() {
		return accName;
	}
	public void setAccName(String accName) {
		this.accName = accName;
	}
	public List<String> getCountries() {
		return countries;
	}
	public void setCountries(List<String> countries) {
		this.countries = countries;
	}
	public String getCollector() {
		return collector;
	}
	public void setCollector(String collector) {
		this.collector = collector;
	}
	public String getAccNumber() {
		return accNumber;
	}
	public void setAccNumber(String accNumber) {
		this.accNumber = accNumber;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public List<Long> getAlleleAssayIds() {
		return alleleAssayIds;
	}
	public void setAlleleAssayIds(List<Long> alleleAssayIds) {
		this.alleleAssayIds = alleleAssayIds;
	}
}
