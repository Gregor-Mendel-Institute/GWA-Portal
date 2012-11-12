package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.cdv.Source", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface SourceProxy extends EntityProxy {
	
	public Long getId();
	
	public String getSource();
	public void setSource(String source);
	
	public String getContact();
	public void setContact(String contact) ;
	
	public String getInstitute();
	public void setInstitute(String institute);
	
	public String getDepartment();
	public void setDepartment(String department);
	
	public String getAddress();
	public void setAddress(String address);
	
	public String getCity();
	public void setCity(String city);
	
	public String getStateProvince();
	public void setStateProvince(String stateProvince);
	
	public String getCountry();
	public void setCountry(String country);
	
	public String getPhone();
	public void setPhone(String phone);
	
	public String getFax();
	public void setFax(String fax);
	
	public String getEmail();
	public void setEmail(String email);
	
	public String getUrl();
	public void setUrl(String url);
	
	public String getComments();
	public void setComments(String comments);

}
