package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PhenotypeProxy extends EntityProxy{

	Long getId();
	
	UnitOfMeasureProxy getUnitOfMeasure();
	
	void setUnitOfMeasure(UnitOfMeasureProxy unitOfMeasure);
	
	Set<TraitProxy> getTraits();
	
	Set<StatisticTypeProxy> getStatisticTypes();

	String getLocalTraitName();
	
	void setLocalTraitName(String localTraitName);

	String getTraitProtocol();
	void setTraitProtocol(String traitProtocol);

	String getToAccession();
	void setToAccession(String toAccession);
	
	String getEoAccession();
	void setEoAccession(String eoAccession);

	boolean isOwner();
	
	AccessControlEntryProxy getUserPermission();

	Long getNumberOfObsUnits();

	Long getNumberOfStudies();
	
}
