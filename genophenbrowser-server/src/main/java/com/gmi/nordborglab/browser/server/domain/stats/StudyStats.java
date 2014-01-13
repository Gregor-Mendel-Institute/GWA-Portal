package com.gmi.nordborglab.browser.server.domain.stats;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;

import java.util.List;

public class StudyStats {

    public StudyStats() {
    }

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
