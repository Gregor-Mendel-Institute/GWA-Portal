package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.observation.Experiment", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface ExperimentProxy extends EntityProxy{
	
	Long getId();
	
	AclExperimentIdentityProxy getAcl();
	
	//Set<ObsUnitProxy> getObsUnits();
	
	@NotNull
    @Size(min=2)
	String getName();
	void setName(String name);
	
	String getDesign();
	void setDesign(String design);
	
	void setOriginator(String originator);
	String getOriginator();
	
	void setComments(String comments);
	String getComments();
	
	boolean isOwner();
	
	AccessControlEntryProxy getUserPermission();
	
	int getNumberOfPhenotypes();
}
