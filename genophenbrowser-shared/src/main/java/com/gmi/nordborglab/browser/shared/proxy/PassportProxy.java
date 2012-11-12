package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.germplasm.Passport", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PassportProxy extends EntityProxy {

	public Long getId();
	
    public TaxonomyProxy getTaxonomy();
    public void setTaxonomy(TaxonomyProxy taxonomy);
    
    public AccessionCollectionProxy getCollection();
    public void setCollection(AccessionCollectionProxy collection);
    
    public String getAccename();
    public void setAccename(String accename);
    
    public SourceProxy getSource();
    public void setSource(SourceProxy source);
    
    public String getSourceText();
    public void setSourceText(String sourceText);
    
    public String getAccenumb();
    public void setAccenumb(String accenumb);
    
    
    public SampStatProxy getSampstat();
    public void setSampstat(SampStatProxy sampstat);
    
    
    public String getComments();
    public void setComments(String comments);
    
    public Set<AlleleAssayProxy> getAlleleAssays();
}
