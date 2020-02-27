package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

@ProxyForName("com.gmi.nordborglab.browser.server.domain.PassportSearchCriteria")
public interface PassportSearchCriteriaProxy extends ValueProxy {

    public Long getPassportId();

    public void setPassportId(Long passportId);

    public Long getSampStatId();

    public void setSampStatId(Long sampStatId);

    public String getAccName();

    public void setAccName(String accName);

    public List<String> getCountries();

    public void setCountries(List<String> countries);

    public String getCollector();

    public void setCollector(String collector);

    public String getAccNumber();

    public void setAccNumber(String accNumber);

    public String getSource();

    public void setSource(String source);

    public List<Long> getAlleleAssayIds();

    public void setAlleleAssayIds(List<Long> alleleAssayIds);
}
