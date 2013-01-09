package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.cdv.Study", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StudyProxy extends EntityProxy {
	
	public Long getId();
	
	@NotNull
	public StudyProtocolProxy getProtocol();
	public void setProtocol(StudyProtocolProxy protocol);
	
	@NotNull
	public AlleleAssayProxy getAlleleAssay();
	public void setAlleleAssay(AlleleAssayProxy alleleAssay);
	
	@NotNull
	public String getName();
	public void setName(String name);
	
	public String getProducer();
	public void setProducer(String producer);
	
	public Date getStudyDate();
	public void setStudyDate(Date date);
	
	public Boolean isDone();
	public void setIsDone(Boolean isDone);

	public AccessControlEntryProxy getUserPermission();
	public boolean isOwner();
	
	public void setTraits(Set<TraitProxy> traits);
	public Set<TraitProxy> getTraits();
	
	public PhenotypeProxy getPhenotype();
	
}
