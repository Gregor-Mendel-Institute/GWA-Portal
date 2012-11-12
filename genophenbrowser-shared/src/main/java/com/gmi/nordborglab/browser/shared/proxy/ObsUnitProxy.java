package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Date;
import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.observation.ObsUnit", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface ObsUnitProxy extends EntityProxy {

	public Long getId();
	
	
	public String getName();
	public void setName(String name);
	
	public Integer getCoordX();
	public void setCoordX(Integer coordX);
	
	public Integer getCoordY();
	public void setCoordY(Integer coordY);
	
	public String getRep();
	public void setRep(String rep);
	
	public String getBlock();
	public void setBlock(String block);
	
	
	public String getPlot();
	public void setPlot(String plot);
	
	
	public String getSeason();
	public void setSeason(String season);
	
	public String getPlant();
	public void setPlant(String plant);
	
	public Date getPlantingDate();
	public void setPlantingDate(Date date);
	
	public String getComments();
	public void setComments(String comments);
	
	public Date getHarvestDate();
	public void setHarvestDate(Date date);
	
	public LocalityProxy getLocality();
	public void setLocality(LocalityProxy locality);
	
	public ExperimentProxy getExperiment();
	public void setExperiment(ExperimentProxy experiment);
	
	public Set<TraitProxy> getTraits();
	
	public StockProxy getStock();
	public void setStock(StockProxy stock);
	
}
