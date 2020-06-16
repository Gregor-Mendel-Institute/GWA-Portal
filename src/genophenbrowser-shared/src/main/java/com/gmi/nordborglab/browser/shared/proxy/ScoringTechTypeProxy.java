package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.genotype.ScoringTechType", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface ScoringTechTypeProxy extends EntityProxy {
	
	public Long getId();
	
	public String getScoringTechGroup();
	
	public void setScoringTechGroup(String scoringTechGroup);
	
	public String getScoringTechType();
	
	public void setScoringTechType(String scoringTechType);

}
