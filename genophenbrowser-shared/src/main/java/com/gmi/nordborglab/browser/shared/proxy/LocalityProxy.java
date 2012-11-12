package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.observation.Locality", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface LocalityProxy extends EntityProxy {
	
	public Long getId();
	
	public Integer getElevation();
	public void setElevation(Integer elevation);
	
	public String getCity();
	public void setCity(String city);
	
	public String getCountry();
	public void setCountry(String country);
	
	public String getOrigcty();
	public void setOrigcty(String origcty);
	
	public Double getLatitude();
	public void setLatitude(Double latitude);
	
	public Double getLongitude();
	public void setLongitude(Double longitude);
	
	public String getLocalityName();
	public void setLocalityName(String localityName);
	
	public String getStateProvince();
	public void setStateProvince(String stateProvence);
	
	public String getLoAccession();
	public void setLoAccession(String loAccession);
}
