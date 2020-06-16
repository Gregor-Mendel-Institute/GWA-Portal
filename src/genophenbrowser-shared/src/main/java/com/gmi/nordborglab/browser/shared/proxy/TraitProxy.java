package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Date;


@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.phenotype.Trait", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface TraitProxy extends EntityProxy {
    public Long getId();

    public Date getDateMeasured();

    public void setDateMeasured(Date date);

    public String getValue();

    public void setValue(String value);

    public PhenotypeProxy getTraitUom();

    public void setTraitUom(PhenotypeProxy phentoype);

    public ObsUnitProxy getObsUnit();

    public void setObsUnit(ObsUnitProxy obsUnit);

    public StatisticTypeProxy getStatisticType();

    public void setStatisticType(StatisticTypeProxy statisticType);
}
