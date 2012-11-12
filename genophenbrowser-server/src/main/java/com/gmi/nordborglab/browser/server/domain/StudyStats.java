package com.gmi.nordborglab.browser.server.domain;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;

public class StudyStats {
	
	public StudyStats() {}
	
	private Integer numberOfObsUnitWithGenotype;
	private List<ObsUnit> obsUnitsWithNoGenotype;

	public Integer getNumberOfObsUnitWithGenotype() {
		return numberOfObsUnitWithGenotype;
	}

	public void setNumberOfObsUnitWithGenotype(Integer numberOfObsUnitWithGenotype) {
		this.numberOfObsUnitWithGenotype = numberOfObsUnitWithGenotype;
	}

	public List<ObsUnit> getObsUnitsWithNoGenotype() {
		return obsUnitsWithNoGenotype;
	}

	public void setObsUnitsWithNoGenotype(List<ObsUnit> obsUnitsWithNoGenotype) {
		this.obsUnitsWithNoGenotype = obsUnitsWithNoGenotype;
	}
}
