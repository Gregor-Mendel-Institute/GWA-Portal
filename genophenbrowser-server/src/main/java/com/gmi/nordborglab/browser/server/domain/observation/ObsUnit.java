package com.gmi.nordborglab.browser.server.domain.observation;

import java.util.Date;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

@Entity
@Table(name="div_obs_unit",schema="observation")
@AttributeOverride(name="id", column=@Column(name="div_obs_unit_id"))
@SequenceGenerator(name="idSequence", sequenceName="observation.div_obs_unit_div_obs_unit_id_seq")
public class ObsUnit extends BaseEntity {

	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_experiment_id")
	private Experiment experiment;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_stock_id")
	private Stock stock;
	
	@OneToMany(mappedBy="obsUnit",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<Trait> traits;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_locality_id")
	private Locality locality;
	
	private String name;
	private Integer coord_x;
	private Integer coord_y;
	private String rep;
	private String block;
	private String plot;
	private String season;
	private String plant;
	@Temporal(TemporalType.TIMESTAMP)
	private Date planting_date;
	@Temporal(TemporalType.TIMESTAMP)
	private Date harvest_date;
	private String comments;
	
	public ObsUnit() {}
	
	
	public Experiment getExperiment() {
		return experiment;
	}
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}
	public Stock getStock() {
		return stock;
	}
	public void setStock(Stock stock) {
		this.stock = stock;
	}
	public Locality getLocality() {
		return locality;
	}
	public void setLocality(Locality locality) {
		this.locality = locality;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCoordX() {
		return coord_x;
	}
	public void setCoordX(Integer coord_x) {
		this.coord_x = coord_x;
	}
	public Integer getCoordY() {
		return coord_y;
	}
	public void setCoordY(Integer coord_y) {
		this.coord_y = coord_y;
	}
	public String getRep() {
		return rep;
	}
	public void setRep(String rep) {
		this.rep = rep;
	}
	public String getBlock() {
		return block;
	}
	public void setBlock(String block) {
		this.block = block;
	}
	public String getPlot() {
		return plot;
	}
	public void setPlot(String plot) {
		this.plot = plot;
	}
	public String getSeason() {
		return season;
	}
	public void setSeason(String season) {
		this.season = season;
	}
	public String getPlant() {
		return plant;
	}
	public void setPlant(String plant) {
		this.plant = plant;
	}
	public Date getPlantingDate() {
		return planting_date;
	}
	public void setPlantingDate(Date planting_date) {
		this.planting_date = planting_date;
	}
	public Date getHarvestDate() {
		return harvest_date;
	}
	public void setHarvestDate(Date harvest_date) {
		this.harvest_date = harvest_date;
	}
	
	public Set<Trait> getTraits() {
		return traits;
	}


	public String getComments() {
		return comments;
	}


	public void setComments(String comments) {
		this.comments = comments;
	}
	
}
